package de.medienDresden.illumina.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.slf4j.Logger;

import java.util.ArrayList;

import de.medienDresden.Illumina;
import de.medienDresden.illumina.R;
import de.medienDresden.illumina.pilight.Device;
import de.medienDresden.illumina.pilight.Location;
import de.medienDresden.illumina.service.PilightBinder;
import de.medienDresden.illumina.service.PilightService;

public abstract class BaseActivity extends ActionBarActivity implements
        PilightBinder.ServiceListener {

    // ------------------------------------------------------------------------
    //
    //      Pilight service binding
    //
    // ------------------------------------------------------------------------

    private PilightBinder mBinder;

    private boolean mIsPilightDisconnected = true;

    @Override
    public void onPilightError(int cause) {
        getLogger().info("onPilightError(" + cause + ")");
        mIsPilightDisconnected = true;

        switch (cause) {
            case PilightService.Error.CONNECTION_FAILED:
                showError(R.string.wrong_settings_error);
                break;

            case PilightService.Error.REMOTE_CLOSED:
                showError(R.string.remote_closed_connection);
                break;

            case PilightService.Error.HANDSHAKE_FAILED:
                showError(R.string.problem_with_pi);
                break;

            case PilightService.Error.UNKNOWN:
                // break intentionally omitted
            default:
                showError(R.string.unknown_error);
                break;
        }

        reset();
    }

    @Override
    public void onPilightConnected() {
        getLogger().info("onPilightConnected");
        mIsPilightDisconnected = false;
        reset();
    }

    @Override
    public void onPilightDisconnected() {
        getLogger().info("onPilightDisconnected");
        mIsPilightDisconnected = true;
        reset();
    }

    @Override
    public void onPilightDeviceChange(Device device) {
        getLogger().info("onPilightDeviceChange(" + device.getId() + ")");
    }

    @Override
    public void onServiceConnected() {
        getLogger().info("onServiceConnected");
        mIsPilightDisconnected = true;
        reset();
    }

    @Override
    public void onServiceDisconnected() {
        getLogger().info("onServiceDisconnected");
        mIsPilightDisconnected = true;
        reset();
    }

    @Override
    public void onLocationListResponse(ArrayList<Location> locations) {
        getLogger().info("onLocationListResponse, #locations = " + locations.size());
    }

    @Override
    public void onLocationResponse(Location location) {
        getLogger().info("onLocationResponse(" + location.getId() + ")");
    }

    // ------------------------------------------------------------------------
    //
    //      Members
    //
    // ------------------------------------------------------------------------

    protected void dispatch(Message message) {
        getLogger().info("dispatch(" + message.what + ")");
        mBinder.send(message);
    }

    protected boolean isDisconnected() {
        return mIsPilightDisconnected;
    }

    protected void reset() {
        getLogger().info("reset");
    }

    // ------------------------------------------------------------------------
    //
    //      Lifecycle
    //
    // ------------------------------------------------------------------------

    private boolean mIsPaused;

    private String mCurrentTheme;

    abstract protected Logger getLogger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getLogger().info("onCreate");

        mCurrentTheme = ((Illumina) getApplication()).getSharedPreferences()
                .getString(Illumina.PREF_THEME, getString(R.string.theme_default));

        setTheme(getResources().getIdentifier(mCurrentTheme, "style", getPackageName()));

        super.onCreate(savedInstanceState);

        if (!isTaskRoot()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mBinder = new PilightBinder(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLogger().info("onStart");
        mBinder.bindService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getLogger().info("onStop");
        mBinder.unbindService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPaused = false;

        final String possiblyUpdatedTheme = ((Illumina) getApplication()).getSharedPreferences()
                .getString(Illumina.PREF_THEME, getString(R.string.theme_default));

        if (!TextUtils.equals(mCurrentTheme, possiblyUpdatedTheme)) {
            finish();
            startActivity(new Intent(this, getClass()));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // ------------------------------------------------------------------------
    //
    //      Ui
    //
    // ------------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                getLogger().info("user clicked settings");
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void showError(int stringResourceId) {
        final String errorString = getString(stringResourceId);
        getLogger().info("showError(" + errorString + ")");

        if (!mIsPaused) {
            Toast.makeText(this, errorString, Toast.LENGTH_LONG).show();
        }
    }

}
