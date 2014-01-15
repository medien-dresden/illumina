package de.medienDresden.illumina.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ListFragment;

import java.util.ArrayList;

import de.medienDresden.illumina.service.PilightBinder;
import de.medienDresden.illumina.pilight.Device;
import de.medienDresden.illumina.pilight.Location;

public abstract class BaseListFragment extends ListFragment implements
        PilightBinder.ServiceListener {

    private PilightBinder mBinder;

    @Override
    public void onPilightError(int cause) {}

    @Override
    public void onPilightConnected() {}

    @Override
    public void onPilightDisconnected() {}

    @Override
    public void onPilightDeviceChange(Device device) {}

    @Override
    public void onServiceConnected() {}

    @Override
    public void onServiceDisconnected() {}

    @Override
    public void onLocationListResponse(ArrayList<Location> locations) {}

    @Override
    public void onLocationResponse(Location location) {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinder = new PilightBinder(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBinder.bindService(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        mBinder.unbindService(getActivity());
    }

    protected void dispatch(Message message) {
        mBinder.send(message);
    }

}
