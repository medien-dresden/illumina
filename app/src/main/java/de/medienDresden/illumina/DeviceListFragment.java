package de.medienDresden.illumina;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import de.medienDresden.illumina.pilight.Device;
import de.medienDresden.illumina.pilight.Location;

public class DeviceListFragment extends ListFragment {

    private static final String TAG = DeviceListFragment.class.getSimpleName();

    private static final String ARG_LOCATION = "location";

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

        final Location location = getArguments().getParcelable(ARG_LOCATION);

        if (location == null) {
            Log.e(TAG, "location is null");
            return;
        }

        if (location.size() < 1) {
            Log.i(TAG, location.getName() + " has no devices to show");
            setListShownNoAnimation(false);
            // TODO indicate
            return;
        }

        final ArrayAdapter<Device> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.device_list_item, new ArrayList<>(location.values()));

        setListAdapter(adapter);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
    }

}
