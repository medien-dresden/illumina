/*
 * Copyright 2014 Peter Heisig
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package de.medienDresden.illumina.widget;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Locale;

import de.medienDresden.illumina.fragment.DeviceListFragment;
import de.medienDresden.illumina.pilight.Location;

public class LocationPagerAdapter extends FragmentPagerAdapter {

    private final ArrayList<Location> mLocations;

    public LocationPagerAdapter(FragmentManager fragmentManager, ArrayList<Location> locations) {
        super(fragmentManager);

        mLocations = locations;
    }

    @Override
    public Fragment getItem(int position) {
        return DeviceListFragment.newInstance(mLocations.get(position).getId());
    }

    @Override
    public int getCount() {
        return mLocations.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        final Locale locale = Locale.getDefault();
        return mLocations.get(position).getName().toUpperCase(locale);
    }

}
