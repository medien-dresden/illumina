package de.medienDresden.illumina.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;

import org.codechimp.apprater.AppRater;

import java.util.Locale;

import de.medienDresden.Illumina;
import de.medienDresden.illumina.BuildConfig;
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
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        mCurrentTheme = getPreferences().getString(
                Illumina.PREF_THEME, getString(R.string.theme_default));

        setTheme(getResources().getIdentifier(mCurrentTheme, "style", getPackageName()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            requestWindowFeature(Window.FEATURE_ACTION_BAR);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_fragment);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (getActionBar() != null) {
                getActionBar().setDisplayHomeAsUpEnabled(true);
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

        // AppRater.rateNow(this);
        // opens the market with the same task ...

        // ... so we make our own intent
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(AppRater.getMarket().getMarketURI(this));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void showLicenses() {
        Log.i(TAG, "showLicenses");
        LicensesDialogFragment.newInstance(R.raw.licenses, true)
                .show(getSupportFragmentManager(), null);
    }

    @Override
    public void contactDeveloper() {
        Log.i(TAG, "contactDeveloper");

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getResources().getString(R.string.developer_url)));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setMessage(String.format(Locale.getDefault(),
                            "Version: %s\nVariant: %s\nBuild: %s",
                            BuildConfig.VERSION_NAME,
                            BuildConfig.FLAVOR,
                            BuildConfig.BUILD_TYPE))
                    .setCancelable(true)
                    .create()
                    .show();
        }

        return super.onKeyLongPress(keyCode, event);
    }

}
