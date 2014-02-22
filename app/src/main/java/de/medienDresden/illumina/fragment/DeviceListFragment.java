package de.medienDresden.illumina.fragment;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;

import de.medienDresden.illumina.R;
import de.medienDresden.illumina.pilight.Device;
import de.medienDresden.illumina.pilight.Location;
import de.medienDresden.illumina.service.PilightService;
import de.medienDresden.illumina.widget.DeviceAdapter;

public class DeviceListFragment extends BaseListFragment implements DeviceAdapter.DeviceChangeListener {

    public static final Logger log = LoggerFactory.getLogger(DeviceListFragment.class);

    public static final String ARG_LOCATION_ID = "locationId";

    private String mLocationId;

    private Comparator<? super Device> mDeviceOrderComparator = new Comparator<Device>() {
        @Override
        public int compare(Device device, Device device2) {
            final int o1 = device.getOrder();
            final int o2 = device2.getOrder();

            if (o1 > o2) {
                return 1;
            } else if (o1 < o2) {
                return -1;
            } else {
                return 0;
            }
        }
    };

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
        log.info(mLocationId + ": onCreate()");
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyView(R.layout.empty_data);
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        dispatch(Message.obtain(null, PilightService.Request.STATE));
    }

    @Override
    public void onPilightConnected() {
        super.onPilightConnected();
        requestLocation();
    }

    @Override
    public void onLocationResponse(Location location) {
        super.onLocationResponse(location);

        if (location.size() < 1) {
            log.info(mLocationId + " has no devices to show");
        }

        final DeviceAdapter adapter = new DeviceAdapter(
                getActivity(), new ArrayList<>(location.values()), this);

        setListAdapter(adapter);

        adapter.sort(mDeviceOrderComparator);
        // adapter.getFilter().filter(""); // FIXME resets scroll position
    }

    @Override
    public void onPilightDeviceChange(Device remoteDevice) {
        super.onPilightDeviceChange(remoteDevice);

        if (TextUtils.equals(remoteDevice.getLocationId(), mLocationId)) {
            final DeviceAdapter adapter = (DeviceAdapter) getListAdapter();
            adapter.remove(remoteDevice);
            adapter.add(remoteDevice);
            adapter.sort(mDeviceOrderComparator);
            // adapter.getFilter().filter(""); // FIXME resets scroll position
        }
    }

    @Override
    public void onDeviceChange(Device device, int property) {
        sendDeviceChange(device, property);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        final Device device = (Device) getListAdapter().getItem(position);

        switch (device.getType()) {
            case Device.TYPE_SCREEN:
                device.setValue(device.isUp() ? Device.VALUE_DOWN : Device.VALUE_UP);
                break;

            case Device.TYPE_SWITCH:
            case Device.TYPE_DIMMER:
                device.setValue(device.isOn() ? Device.VALUE_OFF : Device.VALUE_ON);
                break;

            case Device.TYPE_WEATHER:
                return;
        }

        sendDeviceChange(device, Device.PROPERTY_VALUE);
    }

    private void requestLocation() {
        log.info("requestLocation: " + mLocationId);

        final Message msg = Message.obtain(null, PilightService.Request.LOCATION);
        final Bundle bundle = new Bundle();

        assert msg != null;
        bundle.putString(PilightService.Extra.LOCATION_ID, mLocationId);
        msg.setData(bundle);

        dispatch(msg);
    }

    private void sendDeviceChange(Device device, int property) {
        log.info("sendDeviceChange: " + device.getId());

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
