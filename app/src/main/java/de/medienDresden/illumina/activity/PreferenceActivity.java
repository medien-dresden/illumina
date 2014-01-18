package de.medienDresden.illumina.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import org.codechimp.apprater.AppRater;

import de.medienDresden.Illumina;
import de.medienDresden.illumina.R;
import de.psdev.licensesdialog.LicensesDialogFragment;

public class PreferenceActivity extends FragmentActivity {

    private static final String TAG = PreferenceActivity.class.getSimpleName();

    public static final String ACTION_RATE
            = "de.medienDresden.illumina.ACTION_RATE";

    public static final String ACTION_LICENSES
            = "de.medienDresden.illumina.ACTION_LICENSES";

    private String mCurrentTheme;

    private SharedPreferences getPreferences() {
        return ((Illumina) getApplication()).getSharedPreferences();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        mCurrentTheme = getPreferences().getString(
                Illumina.PREF_THEME, getString(R.string.theme_default));

        setTheme(getResources().getIdentifier(mCurrentTheme, "style", getPackageName()));
        setContentView(R.layout.preference_fragment);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            assert getActionBar() != null;
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final String action = getIntent().getAction();

        if (action != null) {
            switch (action) {
                case ACTION_RATE:
                    onActionRate();
                    break;

                case ACTION_LICENSES:
                    onActionLicenses();
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onActionRate() {
        Log.i(TAG, "onActionRate");

        AppRater.rateNow(this);
        finish();
    }

    private void onActionLicenses() {
        Log.i(TAG, "onActionLicense");

        final LicensesDialogFragment fragment
                = LicensesDialogFragment.newInstance(R.raw.licenses, true);

        fragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });

        fragment.show(getSupportFragmentManager(), null);
    }

    public void refreshTheme() {
        final String possiblyUpdatedTheme = getPreferences().getString(
                Illumina.PREF_THEME, getString(R.string.theme_default));

        if (!TextUtils.equals(mCurrentTheme, possiblyUpdatedTheme)) {
            finish();
            startActivity(new Intent(this, getClass()));
        }
    }

    public static class SettingsFragment extends PreferenceFragment {

        private SharedPreferences.OnSharedPreferenceChangeListener mListener
                = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (TextUtils.equals(key, Illumina.PREF_THEME) && getActivity() != null) {
                    ((PreferenceActivity) getActivity()).refreshTheme();
                }
            }
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onResume() {
            super.onResume();

            assert getPreferenceManager().getSharedPreferences() != null;
            getPreferenceManager().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(mListener);
        }

        @Override
        public void onPause() {
            super.onPause();

            assert getPreferenceManager().getSharedPreferences() != null;
            getPreferenceManager().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(mListener);
        }

    }

}
