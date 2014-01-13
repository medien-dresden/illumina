package de.medienDresden.illumina;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import de.medienDresden.illumina.pilight.Device;
import de.medienDresden.illumina.pilight.Location;

public class DeviceListFragment extends ListFragment implements DeviceAdapter.CheckHelper {

    private static final String TAG = DeviceListFragment.class.getSimpleName();

    private static final String ARG_LOCATION = "location";

    private Location mLocation;

    private LocalBroadcastManager mBroadcastManager;

    private ArrayAdapter<Device> mAdapter;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Device remoteDevice = intent.getParcelableExtra(PilightService.EXTRA_DEVICE);

            assert remoteDevice != null;

            if (TextUtils.equals(remoteDevice.getLocationId(), mLocation.getId())) {
                final Device localDevice = mLocation.get(remoteDevice.getId());

                localDevice.setValue(remoteDevice.getValue());
                localDevice.setDimLevel(remoteDevice.getDimLevel());

                mAdapter.notifyDataSetChanged();
            }
        }
    };

    public static DeviceListFragment newInstance(Location location) {
        final DeviceListFragment fragment = new DeviceListFragment();
        final Bundle args = new Bundle();

        args.putParcelable(ARG_LOCATION, location);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocation = getArguments().getParcelable(ARG_LOCATION);

        assert mLocation != null;
        Log.i(TAG, mLocation.getId() + ": onCreate()");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, mLocation.getId() + ": onActivityCreated()");

        mBroadcastManager = LocalBroadcastManager.getInstance(
                getActivity().getApplicationContext());

        if (mLocation.size() < 1) {
            Log.i(TAG, mLocation.getId() + " has no devices to show");
        }

        final View emptyView = LayoutInflater.from(getActivity())
                .inflate(R.layout.empty_data, null, false);

        assert getListView().getParent() != null;
        assert emptyView != null;
        ((ViewGroup) getListView().getParent()).addView(emptyView);
        getListView().setEmptyView(emptyView);

        mAdapter = new DeviceAdapter(getActivity(),
                R.layout.device_list_item, new ArrayList<>(mLocation.values()), this);

        setListAdapter(mAdapter);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Intent intent = new Intent(PilightService.ACTION_LOCAL_CHANGE);
        final Device device = (Device) getListAdapter().getItem(position);

        device.toggle();

        intent.putExtra(PilightService.EXTRA_DEVICE, device);
        mBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void setChecked(int position, boolean checked) {
        getListView().setItemChecked(position, checked);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, mLocation.getId() + ": onPause()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, mLocation.getId() + ": onResume()");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, mLocation.getId() + ": onStart()");

        mBroadcastManager.registerReceiver(mReceiver,
                new IntentFilter(PilightService.ACTION_REMOTE_CHANGE));
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, mLocation.getId() + ": onStop()");

        mBroadcastManager.unregisterReceiver(mReceiver);
    }

}
