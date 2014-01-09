package de.medienDresden.illumina;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Locale;

import de.medienDresden.illumina.pilight.Setting;

public class LocationPagerAdapter extends FragmentPagerAdapter {

    private final Setting mSetting;

    public LocationPagerAdapter(FragmentManager fragmentManager, Setting setting) {
        super(fragmentManager);

        mSetting = setting;
    }

    @Override
    public Fragment getItem(int position) {
        return DeviceListFragment.newInstance(mSetting.getByIndex(position));
    }

    @Override
    public int getCount() {
        return mSetting.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        final Locale locale = Locale.getDefault();
        return mSetting.getByIndex(position).getName().toUpperCase(locale);
    }

}
