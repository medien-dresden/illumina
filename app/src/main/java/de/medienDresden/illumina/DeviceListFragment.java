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
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLocation = getArguments().getParcelable(ARG_LOCATION);
        mBroadcastManager = LocalBroadcastManager.getInstance(
                getActivity().getApplicationContext());

        if (mLocation == null) {
            Log.e(TAG, "location is null");
            return;
        }

        if (mLocation.size() < 1) {
            Log.i(TAG, mLocation.getName() + " has no devices to show");
            // TODO indicate
            return;
        }

        mAdapter = new DeviceAdapter(getActivity(),
                R.layout.device_list_item, new ArrayList<>(mLocation.values()), this);

        setListAdapter(mAdapter);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    public void onStart() {
        super.onStart();

        mBroadcastManager.registerReceiver(mReceiver,
                new IntentFilter(PilightService.ACTION_REMOTE_CHANGE));
    }

    @Override
    public void onStop() {
        super.onStop();

        mBroadcastManager.unregisterReceiver(mReceiver);
    }

    @Override
    public void setChecked(int position, boolean checked) {
        getListView().setItemChecked(position, checked);
    }

}
