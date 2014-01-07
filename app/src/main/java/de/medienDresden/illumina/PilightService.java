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
import de.medienDresden.illumina.pilight.Setting;

public class PilightService extends Service {

    public static final String TAG = PilightService.class.getSimpleName();

    private final IBinder mBinder = new Binder();

    private Setting mSetting;

    private enum PilightState {
        Connected,
        Connecting,
        Disconnected,
        Disconnecting,
        HandshakePending,
        ConfigRequested,
        Error
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
                case Illumina.ACTION_CONNECT_REQUEST:
                    onConnectRequest(intent);
                    break;

                case Illumina.ACTION_DISCONNECT:
                    onDisconnectRequest();
                    break;

                default:
                    Log.w(TAG, "unhandled " + action);
                    break;
            }
        }
    };

    private final StreamingSocket mPilight = new StreamingSocketImpl(mPilightHandler);

    private void broadcastError(int errorCode) {
        final Intent intent = new Intent(Illumina.ACTION_SERVICE_ERROR);
        intent.putExtra(Illumina.EXTRA_ERROR_CODE, errorCode);
        sendBroadcast(intent);
    }

    private void send(JSONObject json) {
        final String jsonString = json.toString();

        Log.i(TAG, "sending " + jsonString);
        mPilight.send(jsonString);
    }

    private void onSocketConnectionFailed() {
        Log.w(TAG, "pilight connection failed");
        broadcastError(Illumina.ServiceError.CONNECTION_FAILED);
        mState = PilightState.Disconnected;
    }

    private void onSocketDisconnected() {
        Log.i(TAG, "pilight disconnected");

        if (mState != PilightState.Disconnecting) {
            Log.w(TAG, "- closed by remote");
            broadcastError(Illumina.ServiceError.REMOTE_CLOSED_CONNECTION);
        }

        mState = PilightState.Disconnected;
    }

    private void onSocketConnected() {
        Log.i(TAG, "pilight connected, handshake initiated");

        final JSONObject json = new JSONObject();

        try {
            json.put("message", "client gui");
        } catch (JSONException exception) {
            Log.e(TAG, "- error creating handshake message", exception);
        }

        send(json);

        mState = PilightState.HandshakePending;
    }

    private void onPilightMessage(String message) {
        Log.i(TAG, "pilight message received: " + message);

        JSONObject json = new JSONObject();

        if (TextUtils.isEmpty(message)) {
            Log.i(TAG, "- message is empty");

        } else {
            try {
                json = new JSONObject(message);
            } catch (JSONException exception) {
                Log.i(TAG, "- decoding json failed with: " + exception.getMessage());
            }
        }

        switch (mState) {

            case ConfigRequested:
                onPilightConfigResponse(json);
                break;

            case HandshakePending:
                onPilightHandshakeResponse(json);
                break;

            case Connected:
                // TODO device change
                break;

            case Connecting:
                Log.w(TAG, "- impossible state 'Connecting'");
                break;

            case Disconnected:
                Log.w(TAG, "- impossible state 'Disconnected'");
                break;

            case Disconnecting:
                Log.w(TAG, "- impossible state 'Disconnecting'");
                break;

            default:
                // nothing
                break;
        }
    }

    private void onPilightConfigResponse(JSONObject json) {
        Log.i(TAG, "pilight config response");

        if (!json.isNull("config")) {
            try {
                mSetting = Setting.create(json.getJSONObject("config"));
                sendBroadcast(new Intent(Illumina.ACTION_CONNECTED));
                mState = PilightState.Connected;
                return;

            } catch (JSONException exception) {
                Log.i(TAG, "- error reading config " + exception.getMessage());
            }
        }

        broadcastError(Illumina.ServiceError.HANDSHAKE_FAILED);
        mState = PilightState.Error;
    }

    private void onPilightHandshakeResponse(JSONObject json) {
        Log.i(TAG, "pilight handshake response");

        if (!json.isNull("message")) {
            try {
                final String message = json.getString("message");

                if (TextUtils.equals("accept client", message)) {
                    final JSONObject request = new JSONObject();

                    try {
                        request.put("message", "request config");
                    } catch (JSONException exception) {
                        Log.e(TAG, "- error creating config request message", exception);
                    }

                    send(request);
                    mState = PilightState.ConfigRequested;

                    return;

                } else {
                    Log.e(TAG, "- error with message: " + message);
                }

            } catch (JSONException exception) {
                Log.i(TAG, "- error reading message " + exception.getMessage());
            }
        }

        broadcastError(Illumina.ServiceError.HANDSHAKE_FAILED);
        mState = PilightState.Error;
    }

    private void onConnectRequest(Intent intent) {
        Log.i(TAG, "consuming connect request");

        final String newHost = intent.getStringExtra(Illumina.EXTRA_HOST);
        final int newPort = intent.getIntExtra(Illumina.EXTRA_PORT, 0);
        final boolean isEndpointUnchanged = newPort == mPilight.getPort()
                && TextUtils.equals(newHost, mPilight.getHost());

        if (mPilight.isConnected() && isEndpointUnchanged) {
            Log.i(TAG, "- ignored, not in disconnected state");
            return;
        }

        mPilight.connect(newHost, newPort);
        mState = PilightState.Connecting;
    }

    private void onDisconnectRequest() {
        Log.i(TAG, "consuming disconnect request");

        if (mState == PilightState.Disconnected) {
            Log.i(TAG, "- ignored, already disconnected");
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
        filter.addAction(Illumina.ACTION_CONNECT_REQUEST);
        filter.addAction(Illumina.ACTION_DISCONNECT);

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
