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

package de.medienDresden.illumina.activity;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import de.medienDresden.illumina.R;
import de.medienDresden.illumina.pilight.Location;
import de.medienDresden.illumina.service.PilightService;
import de.medienDresden.illumina.widget.LocationPagerAdapter;

public class LocationListActivity extends BaseActivity {

    public static final Logger log = LoggerFactory.getLogger(LocationListActivity.class);

    // ------------------------------------------------------------------------
    //
    //      Service
    //
    // ------------------------------------------------------------------------

    @Override
    public void onPilightError(int cause) {
        super.onPilightError(cause);
        finish();
    }

    @Override
    public void onPilightDisconnected() {
        super.onPilightDisconnected();
        finish();
    }

    @Override
    public void onPilightConnected() {
        super.onPilightConnected();
        requestLocations();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        dispatch(Message.obtain(null, PilightService.Request.STATE));
    }

    @Override
    public void onLocationListResponse(ArrayList<Location> locations) {
        super.onLocationListResponse(locations);

        final ActionBar actionBar = getSupportActionBar();
        final FragmentPagerAdapter pagerAdapter = new LocationPagerAdapter(
                getSupportFragmentManager(), locations);

        mViewPager.setAdapter(pagerAdapter);

        for (Location location : locations) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(location.getName())
                            .setTabListener(mTabListener));
        }

        if (locations.size() > 1) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }

        if (locations.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        }

        if (mSelectedLocationIndex <= actionBar.getTabCount()
                && actionBar.getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
            actionBar.setSelectedNavigationItem(mSelectedLocationIndex);
        }
    }

    private void requestLocations() {
        log.info("requestLocations");
        dispatch(Message.obtain(null, PilightService.Request.LOCATION_LIST));
    }

    // ------------------------------------------------------------------------
    //
    //      Lifecycle
    //
    // ------------------------------------------------------------------------

    private ViewPager mViewPager;

    private ViewGroup mEmptyView;

    private int mSelectedLocationIndex;

    private ActionBar.TabListener mTabListener = new ActionBar.TabListener() {

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            mViewPager.setCurrentItem(tab.getPosition());
        }
    };

    private ViewPager.OnPageChangeListener mPageChangeListener
            = new ViewPager.SimpleOnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
                getSupportActionBar().setSelectedNavigationItem(position);
                mSelectedLocationIndex = position;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);

        mEmptyView = (ViewGroup) findViewById(android.R.id.empty);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(mPageChangeListener);

        if (savedInstanceState != null) {
            mSelectedLocationIndex = savedInstanceState.getInt("mSelectedLocationIndex", 0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("mSelectedLocationIndex", mSelectedLocationIndex);
    }

    @Override
    public void onBackPressed() {
        log.info("onBackPressed");
        dispatch(Message.obtain(null, PilightService.Request.PILIGHT_DISCONNECT));
        super.onBackPressed();
    }

    // ------------------------------------------------------------------------
    //
    //      Members
    //
    // ------------------------------------------------------------------------

    @Override
    protected void reset() {
        super.reset();

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.removeAllTabs();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }

        if (mViewPager != null) {
            mViewPager.setAdapter(null);
        }

        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
