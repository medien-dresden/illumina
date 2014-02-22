package de.medienDresden.illumina.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import de.medienDresden.Illumina;
import de.medienDresden.illumina.communication.StreamingSocket;
import de.medienDresden.illumina.communication.StreamingSocketImpl;
import de.medienDresden.illumina.pilight.Device;
import de.medienDresden.illumina.pilight.Location;
import de.medienDresden.illumina.pilight.Setting;

public class PilightServiceImpl extends Service implements PilightService, Setting.RemoteChangeHandler {

    public static final Logger log = LoggerFactory.getLogger(PilightServiceImpl.class);

    private Setting mSetting;

    private boolean mCurrentlyTriesReconnecting;

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
                    if (mState != PilightState.Disconnected) {
                        onSocketDisconnected();
                    }
                    break;

                case StreamingSocket.MSG_ERROR:
                    if (mState == PilightState.Connecting && !mCurrentlyTriesReconnecting) {
                        onSocketConnectionFailed();
                    } else {
                        onSocketError();
                    }
                    break;

                case StreamingSocket.MSG_MESSAGE_RECEIVED:
                    assert data != null;
                    onSocketMessage(data.getString(StreamingSocket.EXTRA_MESSAGE));
                    break;

                default:
                    log.warn("unhandled message from socket");
                    break;
            }
        }
    };

    private final StreamingSocket mPilight = new StreamingSocketImpl(mPilightHandler);

    private void sendSocketMessage(JSONObject json) {
        final String jsonString = json.toString();

        log.info("sending " + jsonString);
        mPilight.send(jsonString);
    }

    @Override
    public void onRemoteChange(Device device) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(Extra.DEVICE, device);
        sendBroadcast(News.DEVICE_CHANGE, bundle);
    }

    private void onSocketConnectionFailed() {
        log.warn("pilight connection failed");
        sendBroadcast(News.ERROR, Error.CONNECTION_FAILED);
        mState = PilightState.Disconnected;
    }

    private void onSocketDisconnected() {
        log.info("pilight disconnected");
        sendBroadcast(News.DISCONNECTED);
        mState = PilightState.Disconnected;
    }

    private void onSocketError() {
        log.info("pilight socket error");
        if (!mCurrentlyTriesReconnecting && mState != PilightState.Disconnected) {
            mCurrentlyTriesReconnecting = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    log.info("reconnecting");
                    connect();
                }
            }, 100);

        } else if (mState != PilightState.Disconnected) {
            sendBroadcast(News.ERROR, Error.REMOTE_CLOSED);
        }

        mState = PilightState.Disconnected;
    }

    private void onSocketConnected() {
        log.info("pilight connected, handshake initiated");

        final JSONObject json = new JSONObject();

        try {
            json.put("message", "client gui");
        } catch (JSONException exception) {
            log.error("- error creating handshake message", exception);
        }

        sendSocketMessage(json);

        mState = PilightState.HandshakePending;
    }

    private void onSocketMessage(String message) {
        JSONObject json = new JSONObject();

        if (TextUtils.isEmpty(message)) {
            log.info("received message is empty");

        } else {
            try {
                json = new JSONObject(message);
            } catch (JSONException exception) {
                log.info("decoding json failed with: " + exception.getMessage());
                sendBroadcast(News.ERROR, Error.UNKNOWN);
                mState = PilightState.Disconnected;
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
                log.warn("impossible state 'Connecting'");
                break;

            case Disconnected:
                log.warn("impossible state 'Disconnected'");
                break;

            case Disconnecting:
                log.warn("impossible state 'Disconnecting'");
                break;

            default:
                // nothing
                break;
        }
    }

    private void onPilightMessage(JSONObject json) {
        log.info("pilight message received: " + json.toString());

        if (json.isNull("origin")) {
            log.warn("- has no origin, ignored");
        } else if (!TextUtils.equals(json.optString("origin"), "config")) {
            log.warn("- wrong origin, ignored");
        } else {
            mSetting.update(json);
        }
    }

    private void onPilightConfigResponse(JSONObject json) {
        log.info("pilight config response");

        if (!json.isNull("config")) {
            try {
                mPilight.startHeartBeat();
                mSetting = Setting.create(this, json.getJSONObject("config"));

                if (!mCurrentlyTriesReconnecting) {
                    sendBroadcast(News.CONNECTED);
                }

                mCurrentlyTriesReconnecting = false;
                mState = PilightState.Connected;
                return;

            } catch (JSONException exception) {
                log.info("- error reading config " + exception.getMessage());
            }
        }

        sendBroadcast(News.ERROR, Error.HANDSHAKE_FAILED);
        mState = PilightState.Error;
    }

    private void onPilightHandshakeResponse(JSONObject json) {
        log.info("pilight handshake response");

        if (!json.isNull("message")) {
            try {
                final String message = json.getString("message");

                if (TextUtils.equals("accept client", message)) {
                    final JSONObject request = new JSONObject();

                    try {
                        request.put("message", "request config");
                    } catch (JSONException exception) {
                        log.error("- error creating config request message", exception);
                    }

                    sendSocketMessage(request);
                    mState = PilightState.ConfigRequested;

                    return;

                } else {
                    log.error("- error with message: " + message);
                }

            } catch (JSONException exception) {
                log.info("- error reading message " + exception.getMessage());
            }
        }

        sendBroadcast(News.ERROR, Error.HANDSHAKE_FAILED);
        mState = PilightState.Error;
    }

    public boolean isConnected() {
        final boolean isEndpointUnchanged =
                   getPortFromPreferences() == mPilight.getPort()
                && TextUtils.equals(getHostFromPreferences(), mPilight.getHost());

        return isEndpointUnchanged && mPilight.isConnected();
    }

    public void connect() {
        log.info("connect request");

        mPilight.connect(getHostFromPreferences(), getPortFromPreferences());
        mState = PilightState.Connecting;
    }

    public void disconnect() {
        log.info("disconnect request");

        if (mState == PilightState.Disconnected) {
            log.info("- ignored, already disconnected");
            return;
        }

        mState = PilightState.Disconnecting;
        mPilight.disconnect();
    }

    private String getHostFromPreferences() {
        assert getApplication() != null;
        return ((Illumina) getApplication())
                .getSharedPreferences()
                .getString(Illumina.PREF_HOST, "");
    }

    private int getPortFromPreferences() {
        assert getApplication() != null;
        return ((Illumina) getApplication())
                .getSharedPreferences()
                .getInt(Illumina.PREF_PORT, 0);
    }

    public void sendDeviceChange(Device device, int changedProperty) {
        try {
            final JSONObject json = new JSONObject();
            final JSONObject code = new JSONObject();
            final JSONObject values = new JSONObject();

            json.put("message", "send");
            json.put("code", code);

            code.put("location", device.getLocationId());
            code.put("device", device.getId());

            if (changedProperty == Device.PROPERTY_DIM_LEVEL) {
                code.put("values", values);
                values.put("dimlevel", device.getDimLevel());
            }

            if (changedProperty == Device.PROPERTY_VALUE) {
                code.put("state", device.getValue());
            }

            sendSocketMessage(json);

        } catch (JSONException exception) {
            log.error("sending change failed with " + exception.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    //
    //      Lifecycle
    //
    // ------------------------------------------------------------------------

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return Service.START_STICKY;
    }

    // ------------------------------------------------------------------------
    //
    //      Binding
    //
    // ------------------------------------------------------------------------

    /** Target we publish for clients to send messages to IncomingHandler. */
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    /** Keeps track of all current registered clients. */
    private final ArrayList<Messenger> mClients = new ArrayList<>();

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle data = msg.getData();

            if (data != null) {
                data.setClassLoader(Location.class.getClassLoader());
            }

            switch (msg.what) {
                case Request.REGISTER:
                    mClients.add(msg.replyTo);
                    break;

                case Request.STATE:
                    sendState(msg.replyTo);
                    break;

                case Request.UNREGISTER:
                    if (mClients.contains(msg.replyTo)) { // FIXME dirty hack! (see #36)
                        mClients.remove(msg.replyTo);
                    }
                    break;

                case Request.PILIGHT_CONNECT:
                    if (!isConnected()) {
                        mCurrentlyTriesReconnecting = false;
                        connect();
                    } else {
                        sendBroadcast(News.CONNECTED);
                    }
                    break;

                case Request.PILIGHT_DISCONNECT:
                    disconnect();
                    break;

                case Request.LOCATION_LIST:
                    if (!mClients.contains(msg.replyTo)) { // FIXME dirty hack! (see #36)
                        mClients.add(msg.replyTo);
                    }

                    sendLocationList(mClients.get(mClients.indexOf(msg.replyTo)));
                    break;

                case Request.LOCATION:
                    if (!mClients.contains(msg.replyTo)) { // FIXME dirty hack! (see #36)
                        mClients.add(msg.replyTo);
                    }

                    assert data != null;
                    sendLocation(data.getString(Extra.LOCATION_ID),
                            mClients.get(mClients.indexOf(msg.replyTo)));
                    break;

                case Request.DEVICE_CHANGE:
                    assert data != null;
                    sendDeviceChange((Device) data.getParcelable(Extra.DEVICE),
                            data.getInt(Extra.CHANGED_PROPERTY));
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendState(Messenger receiver) {
        try {
            if (isConnected()) {
                receiver.send(Message.obtain(null, News.CONNECTED));
            } else {
                receiver.send(Message.obtain(null, News.DISCONNECTED));
            }
        } catch (RemoteException exception) {
            log.error("sending disconnected state failed", exception);
        }
    }

    private void sendLocation(String locationId, Messenger receiver) {
        final Message message = Message.obtain(null, News.LOCATION);
        final Bundle data = new Bundle();

        data.putParcelable(Extra.LOCATION, mSetting.get(locationId));

        assert message != null;
        message.setData(data);

        try {
            receiver.send(message);
        } catch (RemoteException exception) {
            log.error("sending location failed", exception);
        }
    }

    private void sendLocationList(Messenger receiver) {
        // fix due to issue #34
        if (mSetting == null) {
            sendBroadcast(News.ERROR, Error.HANDSHAKE_FAILED);
            mState = PilightState.Disconnected;
            return;
        }

        final Message message = Message.obtain(null, News.LOCATION_LIST);
        final Bundle data = new Bundle();

        data.putParcelableArrayList(Extra.LOCATION_LIST,
                new ArrayList<>(mSetting.values()));

        assert message != null;
        message.setData(data);

        try {
            receiver.send(message);
        } catch (RemoteException exception) {
            log.error("sending location list failed", exception);
        }
    }

    private void sendBroadcast(final int what) {
        sendBroadcast(what, null, 0);
    }

    private void sendBroadcast(final int what, int arg1) {
        sendBroadcast(what, null, arg1);
    }

    private void sendBroadcast(final int what, Bundle data) {
        sendBroadcast(what, data, 0);
    }

    private void sendBroadcast(final int what, Bundle data, int arg1) {
        final ArrayList<Messenger> deadClients = new ArrayList<>();

        for (Messenger client : mClients) {
            final Message message = Message.obtain(null, what, arg1, 0);

            if (data != null && message != null) {
                message.setData(data);
            }

            try {
                client.send(message);
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                deadClients.add(client);
            }
        }

        mClients.removeAll(deadClients);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

}
