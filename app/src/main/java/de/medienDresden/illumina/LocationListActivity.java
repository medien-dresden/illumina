package de.medienDresden.illumina;

import android.content.Intent;
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
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.codechimp.apprater.AppRater;

import de.medienDresden.illumina.impl.PilightServiceConnection;
import de.medienDresden.illumina.pilight.Setting;

public class LocationListActivity extends ActionBarActivity implements ActionBar.TabListener,
        PilightService.ServiceHandler, PilightServiceConnection.ConnectionHandler {

    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = LocationListActivity.class.getSimpleName();

    private static final int FLIPPER_CHILD_SETTING = 0;

    private static final int FLIPPER_CHILD_VIEW_PAGER = 1;

    private static final int FLIPPER_CHILD_EMPTY = 2;

    private PilightServiceConnection mServiceConnection;

    private ViewPager mViewPager;

    private ProgressBar mProgressBar;

    private ViewFlipper mViewFlipper;

    private EditText mEditTextHost;

    private EditText mEditTextPort;

    private boolean mIsConnectButtonVisible;

    private boolean mIsConnected;

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

    private boolean mIsPaused;

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
                mSelectedLocationIndex = position;
            }
        });

        mEditTextHost = (EditText) findViewById(R.id.host);
        mEditTextPort = (EditText) findViewById(R.id.port);

        mEditTextHost.addTextChangedListener(mHostListener);
        mEditTextPort.addTextChangedListener(mPortListener);

        mProgressBar.setIndeterminate(true);
        mServiceConnection = new PilightServiceConnection(this, this);
        mIsConnectButtonVisible = true;

        AppRater.app_launched(this);
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
        mIsPaused = false;

        loadPreferences();
        mServiceConnection.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;

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
                startActivity(new Intent(this, IlluminaPreferenceActivity.class));
                break;

            case R.id.action_connect:
                connect();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (mIsConnected) {
            mServiceConnection.getService().disconnect();
        } else {
            super.onBackPressed();
        }
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
        switch (type) {
            case ConnectionFailed:
                showError(R.string.wrong_settings_error);
                break;

            case RemoteClosedConnection:
                showError(R.string.remote_closed_connection);
                break;

            case HandshakeFailed:
                showError(R.string.problem_with_pi);
                break;

            case Unknown:
                // break intentionally omitted
            default:
                showError(R.string.unknown_error);
                break;
        }

        onDisconnect();
    }

    @Override
    public void onPilightConnected(Setting setting) {
        onConnect();
    }

    @Override
    public void onPilightDisconnected() {
        onDisconnect();
    }

    @Override
    public void onServiceBound() {
        if (!TextUtils.isEmpty(mHost) && mPort > 1) {
            connect();
        }
    }

    private void connect() {
        onDisconnect();
        setBusy(true);

        if (!mServiceConnection.getService().isConnected(mHost, mPort)) {
            mServiceConnection.getService().connect(mHost, mPort);
        } else {
            onConnect();
        }
    }

    private void onDisconnect() {
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

        mIsConnected = false;
    }

    private void onConnect() {
        final ActionBar actionBar = getSupportActionBar();
        final FragmentPagerAdapter pagerAdapter =
                new LocationPagerAdapter(getSupportFragmentManager(),
                        mServiceConnection.getService().getSetting());

        final int locationCount = pagerAdapter.getCount();

        mViewPager.setAdapter(pagerAdapter);

        if (locationCount > 1) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }

        for (int i = 0; i < locationCount; i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(pagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        if (mSelectedLocationIndex <= actionBar.getTabCount()
                && actionBar.getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
            actionBar.setSelectedNavigationItem(mSelectedLocationIndex);
        }

        if (locationCount < 1) {
            mViewFlipper.setDisplayedChild(FLIPPER_CHILD_EMPTY);
        } else {
            mViewFlipper.setDisplayedChild(FLIPPER_CHILD_VIEW_PAGER);
        }

        setConnectButtonVisibility(false);
        setBusy(false);

        mIsConnected = true;
    }

    private void setConnectButtonVisibility(boolean isVisible) {
        mIsConnectButtonVisible = isVisible;
        supportInvalidateOptionsMenu();
    }

    private void setBusy(boolean busy) {
        if (busy) {
            mProgressBar.setVisibility(View.VISIBLE);
            mEditTextHost.setEnabled(false);
            mEditTextPort.setEnabled(false);

        } else {
            mProgressBar.setVisibility(View.GONE);
            mEditTextHost.setEnabled(true);
            mEditTextPort.setEnabled(true);
        }
    }

    private void showError(int stringResourceId) {
        if (!mIsPaused) {
            Toast.makeText(this, getString(stringResourceId), Toast.LENGTH_LONG).show();
        }
    }

}
