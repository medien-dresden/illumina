package de.medienDresden.illumina.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

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
        Log.i(getTag(), "onPilightError(" + cause + ")");
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
        Log.i(getTag(), "onPilightConnected");
        mIsPilightDisconnected = false;
        reset();
    }

    @Override
    public void onPilightDisconnected() {
        Log.i(getTag(), "onPilightDisconnected");
        mIsPilightDisconnected = true;
        reset();
    }

    @Override
    public void onPilightDeviceChange(Device device) {
        Log.i(getTag(), "onPilightDeviceChange(" + device.getId() + ")");
    }

    @Override
    public void onServiceConnected() {
        Log.i(getTag(), "onServiceConnected");
        mIsPilightDisconnected = true;
        reset();
    }

    @Override
    public void onServiceDisconnected() {
        Log.i(getTag(), "onServiceDisconnected");
        mIsPilightDisconnected = true;
        reset();
    }

    @Override
    public void onLocationListResponse(ArrayList<Location> locations) {
        Log.i(getTag(), "onLocationListResponse, #locations = " + locations.size());
    }

    @Override
    public void onLocationResponse(Location location) {
        Log.i(getTag(), "onLocationResponse(" + location.getId() + ")");
    }

    // ------------------------------------------------------------------------
    //
    //      Members
    //
    // ------------------------------------------------------------------------

    protected void dispatch(Message message) {
        Log.i(getTag(), "dispatch(" + message.what + ")");
        mBinder.send(message);
    }

    protected boolean isDisconnected() {
        return mIsPilightDisconnected;
    }

    protected void reset() {
        Log.i(getTag(), "reset");
    }

    // ------------------------------------------------------------------------
    //
    //      Lifecycle
    //
    // ------------------------------------------------------------------------

    private boolean mIsPaused;

    abstract protected String getTag();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isTaskRoot()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mBinder = new PilightBinder(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBinder.bindService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBinder.unbindService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPaused = false;
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
                Log.i(getTag(), "user clicked settings");
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
        Log.i(getTag(), "showError(" + errorString + ")");

        if (!mIsPaused) {
            Toast.makeText(this, errorString, Toast.LENGTH_LONG).show();
        }
    }

}
