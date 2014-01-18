package de.medienDresden.illumina.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.util.Log;

import java.util.ArrayList;

import de.medienDresden.illumina.service.PilightBinder;
import de.medienDresden.illumina.pilight.Device;
import de.medienDresden.illumina.pilight.Location;

public abstract class BaseListFragment extends ListFragment implements
        PilightBinder.ServiceListener {

    private PilightBinder mBinder;

    @Override
    public void onPilightError(int cause) {
        Log.i(getLogTag(), "onPilightError(" + cause + ")");
    }

    @Override
    public void onPilightConnected() {
        Log.i(getLogTag(), "onPilightConnected");
    }

    @Override
    public void onPilightDisconnected() {
        Log.i(getLogTag(), "onPilightDisconnected");
    }

    @Override
    public void onPilightDeviceChange(Device device) {
        Log.i(getLogTag(), "onPilightDeviceChange(" + device.getId() + ")");
    }

    @Override
    public void onServiceConnected() {
        Log.i(getLogTag(), "onServiceConnected");
    }

    @Override
    public void onServiceDisconnected() {
        Log.i(getLogTag(), "onServiceDisconnected");
    }

    @Override
    public void onLocationListResponse(ArrayList<Location> locations) {
        Log.i(getLogTag(), "onLocationListResponse, #locations = " + locations.size());
    }

    @Override
    public void onLocationResponse(Location location) {
        Log.i(getLogTag(), "onLocationResponse(" + location.getId() + ")");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinder = new PilightBinder(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBinder.bindService(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        mBinder.unbindService(getActivity());
    }

    protected void dispatch(Message message) {
        Log.i(getLogTag(), "dispatch(" + message.what + ")");
        mBinder.send(message);
    }

    abstract protected String getLogTag();

}
