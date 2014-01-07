package de.medienDresden.illumina;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.medienDresden.Illumina;
import de.medienDresden.illumina.communication.StreamingSocket;
import de.medienDresden.illumina.communication.impl.StreamingSocketImpl;

public class PilightService extends Service {

    public static final String TAG = PilightService.class.getSimpleName();

    public static final String ACTION_CONNECT = Illumina.PACKAGE_NAME + ".CONNECT";

    public static final String ACTION_DISCONNECT = Illumina.PACKAGE_NAME + ".DISCONNECT";

    public static final String EXTRA_HOST = Illumina.PACKAGE_NAME + ".EXTRA_HOST";

    public static final String EXTRA_PORT = Illumina.PACKAGE_NAME + ".EXTRA_PORT";

    private final IBinder mBinder = new Binder();

    private enum PilightState {
        Connected,
        Connecting,
        Disconnected,
        Disconnecting,
        HandshakePending,
        ConfigRequested,
    }

    private PilightState mState = PilightState.Disconnected;

    private final Handler mPilightHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final Bundle data = msg.getData();

            switch (msg.what) {
                case StreamingSocket.MSG_CONNECTED:
                    onSocketConnected();
                    break;

                case StreamingSocket.MSG_DISCONNECTED:
                    assert data != null;
                    final boolean isError = data.getBoolean(StreamingSocket.EXTRA_ERROR, false);

                    if (isError) {
                        onSocketConnectionFailed();
                    } else {
                        onSocketDisconnected();
                    }

                    break;

                case StreamingSocket.MSG_MESSAGE_RECEIVED:
                    assert msg.getData() != null;
                    onPilightMessage(msg.getData().getString(StreamingSocket.EXTRA_MESSAGE));
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
                case ACTION_CONNECT:
                    onConnectRequest(intent);
                    break;

                case ACTION_DISCONNECT:
                    onDisconnectRequest();
                    break;

                default:
                    Log.w(TAG, "unhandled " + action);
                    break;
            }
        }
    };

    private final StreamingSocket mPilight = new StreamingSocketImpl(mPilightHandler);

    private void send(JSONObject json) {
        final String jsonString = json.toString();
        Log.i(TAG, "sending " + jsonString);

        mPilight.send(jsonString);
    }

    private void onSocketConnectionFailed() {
        Log.w(TAG, "pilight connection failed");

        // TODO send disconnect message

        mState = PilightState.Disconnected;
    }

    private void onSocketDisconnected() {
        Log.i(TAG, "pilight disconnected");

        if (mState != PilightState.Disconnecting) {
            Log.w(TAG, "pilight connection closed by remote");
            // TODO send disconnect message
        }

        mState = PilightState.Disconnected;
    }

    private void onSocketConnected() {
        Log.i(TAG, "pilight connected, handshake initiated");

        final JSONObject json = new JSONObject();

        try {
            json.put("message", "client gui");
        } catch (JSONException exception) {
            Log.e(TAG, "error creating handshake message", exception);
        }

        send(json);

        mState = PilightState.HandshakePending;
    }

    private void onPilightMessage(String message) {
        Log.i(TAG, "pilight message received: " + message);
    }

    private void onConnectRequest(Intent intent) {
        Log.i(TAG, "consuming connect request");

        final String newHost = intent.getStringExtra(EXTRA_HOST);
        final int newPort = intent.getIntExtra(EXTRA_PORT, 0);
        final boolean isEndpointUnchanged = newPort == mPilight.getPort()
                && TextUtils.equals(newHost, mPilight.getHost());

        if (mPilight.isConnected() && isEndpointUnchanged) {
            Log.i(TAG, "connect request ignored - not in disconnected state");
            return;
        }

        mPilight.connect(newHost, newPort);
        mState = PilightState.Connecting;
    }

    private void onDisconnectRequest() {
        Log.i(TAG, "consuming disconnect request");

        if (mState == PilightState.Disconnected) {
            Log.i(TAG, "disconnect request ignored - already disconnected");
            return;
        }

        mPilight.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CONNECT);
        filter.addAction(ACTION_DISCONNECT);

        registerReceiver(mReceiver, filter);
        sendBroadcast(new Intent(Illumina.ACTION_SERVICE_AVAILABLE));
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
