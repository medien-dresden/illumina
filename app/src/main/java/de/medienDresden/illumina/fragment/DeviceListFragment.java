package de.medienDresden.illumina.fragment;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import de.medienDresden.illumina.widget.DeviceAdapter;
import de.medienDresden.illumina.service.PilightService;
import de.medienDresden.illumina.R;
import de.medienDresden.illumina.pilight.Device;
import de.medienDresden.illumina.pilight.Location;

public class DeviceListFragment extends BaseListFragment implements DeviceAdapter.DimLevelListener {

    private static final String TAG = DeviceListFragment.class.getSimpleName();

    public static final String ARG_LOCATION_ID = "locationId";

    private String mLocationId;

    public static DeviceListFragment newInstance(String locationId) {
        final DeviceListFragment fragment = new DeviceListFragment();
        final Bundle args = new Bundle();

        args.putString(ARG_LOCATION_ID, locationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationId = getArguments().getString(ARG_LOCATION_ID);

        assert mLocationId != null;
        Log.i(TAG, mLocationId + ": onCreate()");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyView(R.layout.empty_data);
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        requestLocation();
    }

    @Override
    public void onLocationResponse(Location location) {
        if (location.size() < 1) {
            Log.i(TAG, mLocationId + " has no devices to show");
        }

        setListAdapter(new DeviceAdapter(getActivity(), new ArrayList<>(location.values()), this));
    }

    @Override
    public void onPilightDeviceChange(Device remoteDevice) {
        if (TextUtils.equals(remoteDevice.getLocationId(), mLocationId)) {
            requestLocation();
        }
    }

    @Override
    public void onDimLevelChanged(Device device) {
        sendDeviceChange(device, Device.PROPERTY_DIM_LEVEL);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        final Device device = (Device) getListAdapter().getItem(position);

        if (device.isOn()) {
            device.setValue(Device.VALUE_OFF);
        } else {
            device.setValue(Device.VALUE_ON);
        }

        sendDeviceChange(device, Device.PROPERTY_VALUE);
    }

    private void requestLocation() {
        final Message msg = Message.obtain(null, PilightService.Request.LOCATION);
        final Bundle bundle = new Bundle();

        assert msg != null;
        bundle.putString(PilightService.Extra.LOCATION_ID, mLocationId);
        msg.setData(bundle);

        dispatch(msg);
    }

    private void sendDeviceChange(Device device, int property) {
        final Message msg = Message.obtain(null, PilightService.Request.DEVICE_CHANGE);
        final Bundle bundle = new Bundle();

        assert msg != null;
        bundle.putInt(PilightService.Extra.CHANGED_PROPERTY, property);
        bundle.putParcelable(PilightService.Extra.DEVICE, device);
        msg.setData(bundle);

        dispatch(msg);
    }

    /* FIXME parent inflation hack
     * fragment should have its own
     * layout with empty view */
    private void setEmptyView(int layoutRes) {
        final View emptyView = LayoutInflater.from(getActivity()).inflate(layoutRes, null, false);

        assert getListView().getParent() != null;
        assert emptyView != null;

        ((ViewGroup) getListView().getParent()).addView(emptyView);
        getListView().setEmptyView(emptyView);
    }

}
