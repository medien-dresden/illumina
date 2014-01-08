package de.medienDresden.illumina.impl;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.medienDresden.illumina.PilightService;
import de.medienDresden.illumina.communication.StreamingSocket;
import de.medienDresden.illumina.communication.impl.StreamingSocketImpl;
import de.medienDresden.illumina.pilight.Setting;

public class PilightServiceImpl extends Service implements PilightService {

    public static final String TAG = PilightServiceImpl.class.getSimpleName();

    private final IBinder mBinder = new LocalBinderImpl();

    private Setting mSetting;

    private ServiceHandler mServiceHandler;

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
                    onSocketMessage(msg.getData().getString(StreamingSocket.EXTRA_MESSAGE));
                    break;

                default:
                    Log.w(TAG, "unhandled message from socket");
                    break;
            }
        }
    };

    private final StreamingSocket mPilight = new StreamingSocketImpl(mPilightHandler);

    private void setServiceHandler(ServiceHandler handler) {
        mServiceHandler = handler;
    }

    private void sendSocketMessage(JSONObject json) {
        final String jsonString = json.toString();

        Log.i(TAG, "sending " + jsonString);
        mPilight.send(jsonString);
    }

    private void onSocketConnectionFailed() {
        Log.w(TAG, "pilight connection failed");
        mServiceHandler.onPilightError(Error.ConnectionFailed);
        mState = PilightState.Disconnected;
    }

    private void onSocketDisconnected() {
        Log.i(TAG, "pilight disconnected");

        if (mState != PilightState.Disconnecting) {
            Log.w(TAG, "- closed by remote");
            mServiceHandler.onPilightError(Error.RemoteClosedConnection);
        } else {
            mServiceHandler.onPilightDisconnected();
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

        sendSocketMessage(json);

        mState = PilightState.HandshakePending;
    }

    private void onSocketMessage(String message) {
        Log.i(TAG, "message received: " + message);

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
                onPilightMessage(json);
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

    private void onPilightMessage(JSONObject json) {
        Log.i(TAG, "pilight message received: " + json.toString());

        if (json.isNull("origin")) {
            Log.w(TAG, "- has no origin, ignored");
        } else if (!TextUtils.equals(json.optString("origin"), "config")) {
            Log.w(TAG, "- wrong origin, ignored");
        } else {
            mSetting.update(json);
        }
    }

    private void onPilightConfigResponse(JSONObject json) {
        Log.i(TAG, "pilight config response");

        if (!json.isNull("config")) {
            try {
                mSetting = Setting.create(json.getJSONObject("config"));
                mServiceHandler.onPilightConnected(mSetting);
                mState = PilightState.Connected;
                return;

            } catch (JSONException exception) {
                Log.i(TAG, "- error reading config " + exception.getMessage());
            }
        }

        mServiceHandler.onPilightError(Error.HandshakeFailed);
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

                    sendSocketMessage(request);
                    mState = PilightState.ConfigRequested;

                    return;

                } else {
                    Log.e(TAG, "- error with message: " + message);
                }

            } catch (JSONException exception) {
                Log.i(TAG, "- error reading message " + exception.getMessage());
            }
        }

        mServiceHandler.onPilightError(Error.HandshakeFailed);
        mState = PilightState.Error;
    }

    @Override
    public boolean isConnected(String host, int port) {
        final boolean isEndpointUnchanged = port == mPilight.getPort()
                && TextUtils.equals(host, mPilight.getHost());

        return isEndpointUnchanged && mPilight.isConnected();
    }

    @Override
    public Setting getSetting() {
        return mSetting;
    }

    @Override
    public void connect(String host, int port) {
        Log.i(TAG, "connect request");

        mPilight.connect(host, port);
        mState = PilightState.Connecting;
    }

    @Override
    public void disconnect() {
        Log.i(TAG, "disconnect request");

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
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class LocalBinderImpl extends Binder implements PilightServiceConnection.LocalBinder {

        @Override
        public PilightService getService(ServiceHandler handler) {
            setServiceHandler(handler);
            return PilightServiceImpl.this;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "service destroyed");
    }
}
