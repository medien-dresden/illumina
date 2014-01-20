package de.medienDresden.illumina.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import org.codechimp.apprater.AppRater;

import de.medienDresden.Illumina;
import de.medienDresden.illumina.R;
import de.medienDresden.illumina.fragment.SettingsFragment;
import de.psdev.licensesdialog.LicensesDialogFragment;

public class PreferenceActivity extends FragmentActivity implements
        SettingsFragment.SettingsListener {

    private static final String TAG = PreferenceActivity.class.getSimpleName();

    private String mCurrentTheme;

    private SharedPreferences getPreferences() {
        return ((Illumina) getApplication()).getSharedPreferences();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        mCurrentTheme = getPreferences().getString(
                Illumina.PREF_THEME, getString(R.string.theme_default));

        setTheme(getResources().getIdentifier(mCurrentTheme, "style", getPackageName()));
        super.onCreate(savedInstanceState);

        setContentView(R.layout.preference_fragment);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            assert getActionBar() != null;
            getActionBar().setDisplayHomeAsUpEnabled(true);
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

    @Override
    public void refreshTheme() {
        final String possiblyUpdatedTheme = getPreferences().getString(
                Illumina.PREF_THEME, getString(R.string.theme_default));

        if (!TextUtils.equals(mCurrentTheme, possiblyUpdatedTheme)) {
            finish();
            startActivity(new Intent(this, getClass()));
        }
    }

    @Override
    public void rateThisApp() {
        Log.i(TAG, "rateThisApp");
        AppRater.rateNow(this);
    }

    @Override
    public void showLicenses() {
        Log.i(TAG, "showLicenses");
        LicensesDialogFragment.newInstance(R.raw.licenses, true)
                .show(getSupportFragmentManager(), null);
    }

}
