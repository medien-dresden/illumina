package de.medienDresden.illumina.activity;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.medienDresden.illumina.widget.LocationPagerAdapter;
import de.medienDresden.illumina.service.PilightService;
import de.medienDresden.illumina.R;
import de.medienDresden.illumina.pilight.Location;

public class LocationListActivity extends BaseActivity {

    private static final String TAG = LocationListActivity.class.getSimpleName();

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
    public void onServiceConnected() {
        super.onServiceConnected();
        requestLocations();
    }

    @Override
    public void onLocationListResponse(ArrayList<Location> locations) {
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

}
