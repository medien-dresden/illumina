package de.medienDresden.illumina;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import de.medienDresden.illumina.pilight.Device;
import de.medienDresden.illumina.pilight.Location;

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
        mIsPilightDisconnected = false;
        reset();
    }

    @Override
    public void onPilightDisconnected() {
        mIsPilightDisconnected = true;
        reset();
    }

    @Override
    public void onPilightDeviceChange(Device device) {
        // nothing by default
    }

    @Override
    public void onServiceConnected() {
        mIsPilightDisconnected = true;
        reset();
    }

    @Override
    public void onServiceDisconnected() {
        mIsPilightDisconnected = true;
        reset();
    }

    @Override
    public void onLocationListResponse(ArrayList<Location> locations) {
        // nothing by default
    }

    @Override
    public void onLocationResponse(Location location) {
        // nothing by default
    }

    // ------------------------------------------------------------------------
    //
    //      Members
    //
    // ------------------------------------------------------------------------

    protected void dispatch(Message message) {
        mBinder.send(message);
    }

    protected boolean isDisconnected() {
        return mIsPilightDisconnected;
    }

    protected void reset() {
        // nothing by default
    }

    // ------------------------------------------------------------------------
    //
    //      Lifecycle
    //
    // ------------------------------------------------------------------------

    private boolean mIsPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                startActivity(new Intent(this, IlluminaPreferenceActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void showError(int stringResourceId) {
        if (!mIsPaused) {
            Toast.makeText(this, getString(stringResourceId), Toast.LENGTH_LONG).show();
        }
    }

}
