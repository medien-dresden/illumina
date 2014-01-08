package de.medienDresden.illumina.impl;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import de.medienDresden.illumina.PilightService;

public class PilightServiceConnection implements ServiceConnection {

    private PilightService mService;

    private final PilightService.ServiceHandler mServiceHandler;

    private final ConnectionHandler mConnectionHandler;

    public interface ConnectionHandler {

        void onServiceBound();

    }

    interface LocalBinder {

        PilightService getService(PilightService.ServiceHandler handler);

    }

    public PilightServiceConnection(ConnectionHandler connectionHandler,
                                    PilightService.ServiceHandler serviceHandler) {

        mConnectionHandler = connectionHandler;
        mServiceHandler = serviceHandler;
    }

    public void bind(Activity activity) {
        activity.bindService(new Intent(activity, PilightServiceImpl.class),
                this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        LocalBinder binder = (LocalBinder) iBinder;
        mService = binder.getService(mServiceHandler);
        mConnectionHandler.onServiceBound();
    }

    public PilightService getService() {
        return mService;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mService = null;
    }

}
