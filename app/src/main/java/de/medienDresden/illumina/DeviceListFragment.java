package de.medienDresden.illumina;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

import de.medienDresden.illumina.pilight.Location;

public class DeviceListFragment extends ListFragment {

    private static final String ARG_LOCATION = "location";

    public static DeviceListFragment newInstance(Location location) {
        final DeviceListFragment fragment = new DeviceListFragment();
        final Bundle args = new Bundle();

        args.putParcelable(ARG_LOCATION, location);
        fragment.setArguments(args);
        return fragment;
    }

}
