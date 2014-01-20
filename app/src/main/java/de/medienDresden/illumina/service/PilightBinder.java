package de.medienDresden.illumina.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import de.medienDresden.illumina.pilight.Device;
import de.medienDresden.illumina.pilight.Location;

public class PilightBinder {

    private static final String TAG = PilightBinder.class.getSimpleName();

    private final ServiceListener mListener;

    public interface ServiceListener {

        void onPilightError(int cause);

        void onPilightConnected();

        void onPilightDisconnected();

        void onPilightDeviceChange(Device device);

        void onServiceConnected();

        void onServiceDisconnected();

        void onLocationListResponse(ArrayList<Location> locations);

        void onLocationResponse(Location location);

    }

    public PilightBinder(ServiceListener listener) {
        mListener = listener;
    }

    /** Messenger for communicating with service. */
    private Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    private boolean mIsBound;

    /**
     * Handler of incoming messages from service.
     */
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            final Bundle data = msg.getData();

            if (data != null) {
                data.setClassLoader(Location.class.getClassLoader());
            }

            switch (msg.what) {
                case PilightService.News.CONNECTED:
                    mListener.onPilightConnected();
                    break;

                case PilightService.News.DISCONNECTED:
                    mListener.onPilightDisconnected();
                    break;

                case PilightService.News.ERROR:
                    mListener.onPilightError(msg.arg1);
                    break;

                case PilightService.News.LOCATION_LIST:
                    assert data != null;
                    mListener.onLocationListResponse(
                            data.<Location>getParcelableArrayList(
                                    PilightService.Extra.LOCATION_LIST));
                    break;

                case PilightService.News.LOCATION:
                    assert data != null;
                    mListener.onLocationResponse(
                            data.<Location>getParcelable(PilightService.Extra.LOCATION));
                    break;

                case PilightService.News.DEVICE_CHANGE:
                    assert data != null;
                    mListener.onPilightDeviceChange(
                            data.<Device>getParcelable(PilightService.Extra.DEVICE));
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                final Message msg = Message.obtain(null, PilightService.Request.REGISTER);

                assert msg != null;
                msg.replyTo = mMessenger;
                mService.send(msg);

                mListener.onServiceConnected();

            } catch (RemoteException exception) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
                Log.w(TAG, "binding the service failed", exception);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;

            mListener.onServiceDisconnected();
        }
    };

    public void bindService(final ContextWrapper contextWrapper) {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        contextWrapper.bindService(
                new Intent(contextWrapper.getApplicationContext(), PilightServiceImpl.class),
                mConnection, Context.BIND_AUTO_CREATE);

        mIsBound = true;
    }

    public void unbindService(final ContextWrapper contextWrapper) {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, PilightService.Request.UNREGISTER);

                    assert msg != null;
                    msg.replyTo = mMessenger;
                    mService.send(msg);

                } catch (RemoteException exception) {
                    // There is nothing special we need to do if the service has crashed.
                    Log.w(TAG, "unbinding the service failed", exception);
                }
            }

            // Detach our existing connection.
            contextWrapper.unbindService(mConnection);
            mIsBound = false;
        }
    }

    public void send(Message message) {
        try {
            message.replyTo = mMessenger;
            mService.send(message);
        } catch (RemoteException exception) {
            Log.w(TAG, "sending failed", exception);
        }
    }

}
