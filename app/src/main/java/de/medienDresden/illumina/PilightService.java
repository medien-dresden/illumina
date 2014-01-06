package de.medienDresden.illumina;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import de.medienDresden.Illumina;
import de.medienDresden.illumina.communication.StreamingSocket;
import de.medienDresden.illumina.communication.impl.StreamingSocketImpl;

public class PilightService extends Service {

    public static final String TAG = PilightService.class.getSimpleName();

    private final IBinder mBinder = new Binder();

    private enum PilightState {
        Disconnected,
        Connecting,
        HandshakePending,
        ConfigRequested,
        Connected
    }

    private PilightState mState = PilightState.Disconnected;

    private final Handler mPilightHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case StreamingSocket.MSG_CONNECTED:
                    mState = PilightState.HandshakePending;
                    break;

                case StreamingSocket.MSG_DISCONNECTED:
                    mState = PilightState.Disconnected;
                    break;

                case StreamingSocket.MSG_MESSAGE_RECEIVED:
                    assert msg.getData() != null;
                    Log.e(TAG, msg.getData().getString(StreamingSocket.EXTRA_MESSAGE));
                    break;

                default:
                    Log.w(TAG, "unhandled message from socket");
                    break;
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case Illumina.ACTION_CONNECT:
                    mState = PilightState.Connecting;

                    mPilight.connect(
                            intent.getStringExtra(Illumina.EXTRA_HOST),
                            intent.getIntExtra(Illumina.EXTRA_PORT, 0));

                    break;

                case Illumina.ACTION_DISCONNECT:
                    mPilight.disconnect();
                    break;

                default:
                    Log.w(TAG, "unhandled " + action);
                    break;
            }
        }
    };

    private final StreamingSocket mPilight = new StreamingSocketImpl(mPilightHandler);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(Illumina.ACTION_CONNECT);
        filter.addAction(Illumina.ACTION_DISCONNECT);

        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}
