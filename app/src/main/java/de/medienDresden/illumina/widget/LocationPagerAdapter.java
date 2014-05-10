/*
 * illumina, a pilight remote
 *
 * Copyright (c) 2014 Peter Heisig <http://google.com/+PeterHeisig>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
