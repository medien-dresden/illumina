package de.medienDresden.illumina;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;

import de.medienDresden.illumina.impl.PilightServiceConnection;
import de.medienDresden.illumina.pilight.Setting;

public class LocationListActivity extends ActionBarActivity implements ActionBar.TabListener,
        PilightService.ServiceHandler, PilightServiceConnection.ConnectionHandler {

    private static final String TAG = LocationListActivity.class.getSimpleName();

    private static final int FLIPPER_CHILD_SETTING = 0;

    private static final int FLIPPER_CHILD_VIEW_PAGER = 1;

    private PilightServiceConnection mServiceConnection;

    private ViewPager mViewPager;

    private ProgressBar mProgressBar;

    private ViewFlipper mViewFlipper;

    private EditText mEditTextHost;

    private EditText mEditTextPort;

    private boolean mIsConnectButtonVisible;

    private TextWatcher mPortListener = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable != null && !TextUtils.isEmpty(editable.toString())) {
                mPort = Integer.parseInt(editable.toString());
            }
        }
    };

    private TextWatcher mHostListener = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable != null && !TextUtils.isEmpty(editable.toString())) {
                mHost = editable.toString();
            }
        }
    };

    private String mHost;

    private int mPort;

    private int mSelectedLocationIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_actionbar);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mProgressBar = (ProgressBar) findViewById(android.R.id.progress);
        mViewFlipper = (ViewFlipper) findViewById(R.id.flipper);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
                mSelectedLocationIndex = position;
            }
        });

        mEditTextHost = (EditText) findViewById(R.id.host);
        mEditTextPort = (EditText) findViewById(R.id.port);

        mEditTextHost.addTextChangedListener(mHostListener);
        mEditTextPort.addTextChangedListener(mPortListener);

        mProgressBar.setIndeterminate(true);
        mServiceConnection = new PilightServiceConnection(this, this);
    }

    private void loadPreferences() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        mHost = prefs.getString("host", "");
        mPort = prefs.getInt("port", 0);
        mSelectedLocationIndex = prefs.getInt("selectedLocationIndex", 0);

        mEditTextHost.setText(mHost);

        if (mPort > 0) {
            mEditTextPort.setText(Integer.toString(mPort));
        }
    }

    private void savePreferences() {
        PreferenceManager
                .getDefaultSharedPreferences(this)
                .edit()
                .putString("host", mHost)
                .putInt("port", mPort)
                .putInt("selectedLocationIndex", mSelectedLocationIndex)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setBusy(true);
        loadPreferences();

        mServiceConnection.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        savePreferences();
        unbindService(mServiceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem connect = menu.findItem(R.id.action_connect);

        if (connect != null) {
            connect.setVisible(mIsConnectButtonVisible);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // TODO
                break;

            case R.id.action_connect:
                setBusy(true);
                connect();
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
        onDisconnected();
    }

    @Override
    public void onPilightConnected(Setting setting) {
        onConnected();
    }

    @Override
    public void onPilightDisconnected() {
        onDisconnected();
    }

    @Override
    public void onServiceBound() {
        connect();
    }

    private void connect() {
        onDisconnected();

        if (!mServiceConnection.getService().isConnected(mHost, mPort)) {
            mServiceConnection.getService().connect(mHost, mPort);
        } else {
            onConnected();
        }
    }

    private void onDisconnected() {
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.removeAllTabs();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }

        if (mViewPager != null) {
            mViewPager.setAdapter(null);
        }

        if (mViewFlipper != null) {
            mViewFlipper.setDisplayedChild(FLIPPER_CHILD_SETTING);
        }

        setConnectButtonVisibility(true);
        setBusy(false);
    }

    private void onConnected() {
        final ActionBar actionBar = getSupportActionBar();
        final FragmentPagerAdapter pagerAdapter =
                new LocationPagerAdapter(getSupportFragmentManager(),
                        mServiceConnection.getService().getSetting());

        final int locationCount = pagerAdapter.getCount();

        mViewPager.setAdapter(pagerAdapter);

        if (pagerAdapter.getCount() > 1) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }

        for (int i = 0; i < locationCount; i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(pagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        if (mSelectedLocationIndex <= locationCount) {
            actionBar.setSelectedNavigationItem(mSelectedLocationIndex);
        }

        mViewFlipper.setDisplayedChild(FLIPPER_CHILD_VIEW_PAGER);
        setConnectButtonVisibility(false);
        setBusy(false);
    }

    private void setConnectButtonVisibility(boolean isVisible) {
        mIsConnectButtonVisible = isVisible;
        supportInvalidateOptionsMenu();
    }

    private void setBusy(boolean busy) {
        final ActionBar actionBar = getSupportActionBar();

        if (busy) {
            mProgressBar.setVisibility(View.VISIBLE);
            actionBar.setBackgroundDrawable(null);

            mEditTextHost.setEnabled(false);
            mEditTextPort.setEnabled(false);

        } else {
            mProgressBar.setVisibility(View.GONE);
            actionBar.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.abc_ab_transparent_dark_holo));

            mEditTextHost.setEnabled(true);
            mEditTextPort.setEnabled(true);
        }
    }

}
