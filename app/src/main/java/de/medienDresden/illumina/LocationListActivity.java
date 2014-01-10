package de.medienDresden.illumina;

import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;

import de.medienDresden.illumina.impl.PilightServiceConnection;
import de.medienDresden.illumina.pilight.Setting;

public class LocationListActivity extends ActionBarActivity implements ActionBar.TabListener,
        PilightService.ServiceHandler, PilightServiceConnection.ConnectionHandler {

    private static final String TAG = LocationListActivity.class.getSimpleName();

    private PilightServiceConnection mServiceConnection;

    private ViewPager mViewPager;

    private ProgressBar mProgressBar;

    private ViewFlipper mViewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        final ActionBar actionBar = getSupportActionBar();

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mProgressBar = (ProgressBar) findViewById(android.R.id.progress);
        mViewFlipper = (ViewFlipper) findViewById(R.id.flipper);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        mProgressBar.setIndeterminate(true);

        actionBar.setIcon(R.drawable.ic_actionbar);

        mServiceConnection = new PilightServiceConnection(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setBusy(true);
        mServiceConnection.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(mServiceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // TODO
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    @Override
    public void onPilightError(PilightService.Error type) {
        // TODO check error type
        clearUi();
    }

    @Override
    public void onPilightConnected(Setting setting) {
        refreshUi();
    }

    @Override
    public void onPilightDisconnected() {
        clearUi();
    }

    @Override
    public void onServiceBound() {
        final String host = "192.168.2.4";
        final int port = 5000;

        clearUi();

        if (!mServiceConnection.getService().isConnected(host, port)) {
            mServiceConnection.getService().connect(host, port);
        } else {
            refreshUi();
        }
    }

    private void clearUi() {
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.removeAllTabs();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }

        if (mViewPager != null) {
            mViewPager.setAdapter(null);
        }
    }

    private void refreshUi() {
        final ActionBar actionBar = getSupportActionBar();
        final FragmentPagerAdapter pagerAdapter =
                new LocationPagerAdapter(getSupportFragmentManager(),
                        mServiceConnection.getService().getSetting());

        mViewPager.setAdapter(pagerAdapter);

        if (pagerAdapter.getCount() > 1) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            for (int i = 0; i < pagerAdapter.getCount(); i++) {
                actionBar.addTab(
                        actionBar.newTab()
                                .setText(pagerAdapter.getPageTitle(i))
                                .setTabListener(this));
            }
        }

        mViewFlipper.setDisplayedChild(1); // TODO there's more
        setBusy(false);
    }

    private void setBusy(boolean busy) {
        final ActionBar actionBar = getSupportActionBar();

        if (busy) {
            mProgressBar.setVisibility(View.VISIBLE);
            actionBar.setBackgroundDrawable(null);
        } else {
            mProgressBar.setVisibility(View.GONE);
            actionBar.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.abc_ab_transparent_dark_holo));
        }
    }

}
