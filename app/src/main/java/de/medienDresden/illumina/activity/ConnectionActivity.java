package de.medienDresden.illumina.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.codechimp.apprater.AppRater;

import de.medienDresden.Illumina;
import de.medienDresden.illumina.R;
import de.medienDresden.illumina.communication.PilightSsdpLocator;
import de.medienDresden.illumina.communication.SsdpLocator;
import de.medienDresden.illumina.service.PilightService;

public class ConnectionActivity extends BaseActivity implements SsdpLocator.Consumer {

    public static final String TAG = ConnectionActivity.class.getSimpleName();

    private SsdpLocator pilightLocator = new PilightSsdpLocator(this);

    private boolean mIsManualDiscovery;

    // ------------------------------------------------------------------------
    //
    //      Service
    //
    // ------------------------------------------------------------------------

    @Override
    public void onPilightConnected() {
        super.onPilightConnected();
        setBusy(true);
        startActivity(new Intent(this, LocationListActivity.class));
    }

    // ------------------------------------------------------------------------
    //
    //      Lifecycle
    //
    // ------------------------------------------------------------------------

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

        @Override
        public void afterTextChanged(Editable editable) {
            supportInvalidateOptionsMenu();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_activity);

        mProgressBar = (ProgressBar) findViewById(android.R.id.progress);
        mEditTextHost = (EditText) findViewById(R.id.host);
        mEditTextPort = (EditText) findViewById(R.id.port);

        mEditTextHost.addTextChangedListener(mTextWatcher);
        mEditTextPort.addTextChangedListener(mTextWatcher);

        mProgressBar.setIndeterminate(true);

        AppRater.app_launched(this);

        setBusy(true);
        mIsManualDiscovery = false;
        pilightLocator.discover();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.connection_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connect:
                Log.i(TAG, "click on connect");
                savePreferences();
                setBusy(true);
                dispatch(Message.obtain(null, PilightService.Request.PILIGHT_CONNECT));
                return true;

            case R.id.action_search:
                Log.i(TAG, "click on search");
                setBusy(true);
                mIsManualDiscovery = true;
                pilightLocator.discover();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem connect = menu.findItem(R.id.action_connect);
        final MenuItem search = menu.findItem(R.id.action_search);

        if (connect != null && mEditTextHost != null && mEditTextPort != null) {
            final Editable host = mEditTextHost.getText();
            final Editable port = mEditTextPort.getText();

            final boolean hasHost = host != null && !TextUtils.isEmpty(host.toString());
            final boolean hasPort = port != null && !TextUtils.isEmpty(port.toString())
                    && Integer.parseInt(port.toString()) > 0;

            connect.setVisible(isDisconnected() && hasHost && hasPort && !mIsBusy);
        }

        if (search != null) {
            search.setVisible(isDisconnected() && !mIsBusy);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPreferences();
        reset();
    }

    // ------------------------------------------------------------------------
    //
    //      Members
    //
    // ------------------------------------------------------------------------

    private void loadPreferences() {
        final Context context = getApplicationContext();
        assert context != null;

        final SharedPreferences prefs = ((Illumina) getApplication()).getSharedPreferences();
        final String host = prefs.getString(Illumina.PREF_HOST, "");
        final int port = prefs.getInt(Illumina.PREF_PORT, 0);

        mEditTextHost.setText(host);

        if (port > 0) {
            mEditTextPort.setText(Integer.toString(port));
        }
    }

    private void savePreferences() {
        final Context context = getApplicationContext();
        assert context != null;

        final SharedPreferences.Editor prefs =
                ((Illumina) getApplication()).getSharedPreferences().edit();

        if (mEditTextHost.getText() != null) {
            prefs.putString(Illumina.PREF_HOST, mEditTextHost.getText().toString());
        }

        if (mEditTextPort.getText() != null) {
            prefs.putInt(Illumina.PREF_PORT, Integer.parseInt(mEditTextPort.getText().toString()));
        }

        prefs.commit();
    }

    @Override
    protected void reset() {
        super.reset();

        setBusy(false);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    // ------------------------------------------------------------------------
    //
    //      Ui
    //
    // ------------------------------------------------------------------------

    private ProgressBar mProgressBar;

    private EditText mEditTextHost;

    private EditText mEditTextPort;

    private boolean mIsBusy;

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

        mIsBusy = busy;
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onSsdpServiceFound(String address, int port) {
        mEditTextHost.setText(address);
        mEditTextPort.setText(String.valueOf(port));

        savePreferences();
        dispatch(Message.obtain(null, PilightService.Request.PILIGHT_CONNECT));
    }

    @Override
    public void onNoSsdpServiceFound() {
        if (mIsManualDiscovery) {
            showError(R.string.service_not_found);
        }

        setBusy(false);
    }

}
