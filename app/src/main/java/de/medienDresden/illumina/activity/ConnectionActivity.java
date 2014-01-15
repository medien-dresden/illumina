package de.medienDresden.illumina.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import de.medienDresden.Illumina;
import de.medienDresden.illumina.R;
import de.medienDresden.illumina.service.PilightService;

public class ConnectionActivity extends BaseActivity {

    public static final String TAG = ConnectionActivity.class.getSimpleName();

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

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem connect = menu.findItem(R.id.action_connect);

        if (connect != null && mEditTextHost != null && mEditTextPort != null) {
            final Editable host = mEditTextHost.getText();
            final Editable port = mEditTextPort.getText();

            final boolean hasHost = host != null && !TextUtils.isEmpty(host.toString());
            final boolean hasPort = port != null && !TextUtils.isEmpty(port.toString())
                    && Integer.parseInt(port.toString()) > 0;

            connect.setVisible(isDisconnected() && hasHost && hasPort);
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
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        String host = prefs.getString(Illumina.PREF_HOST, "");
        int port = prefs.getInt(Illumina.PREF_PORT, 0);

        mEditTextHost.setText(host);

        if (port > 0) {
            mEditTextPort.setText(Integer.toString(port));
        }
    }

    private void savePreferences() {
        final SharedPreferences.Editor prefs = PreferenceManager
                .getDefaultSharedPreferences(this)
                .edit();

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

        supportInvalidateOptionsMenu();
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

}
