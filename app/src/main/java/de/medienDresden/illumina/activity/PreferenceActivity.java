/*
 * Copyright (c) 2014 Peter Heisig.
 *
 * This work is licensed under a Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 */

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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;

import org.codechimp.apprater.AppRater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import de.medienDresden.Illumina;
import de.medienDresden.illumina.BuildConfig;
import de.medienDresden.illumina.R;
import de.medienDresden.illumina.fragment.SettingsFragment;
import de.psdev.licensesdialog.LicensesDialogFragment;

public class PreferenceActivity extends FragmentActivity implements
        SettingsFragment.SettingsListener {

    public static final Logger log = LoggerFactory.getLogger(PreferenceActivity.class);

    private String mCurrentTheme;

    private SharedPreferences getPreferences() {
        return ((Illumina) getApplication()).getSharedPreferences();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void onCreate(Bundle savedInstanceState) {
        log.info("onCreate");

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
        log.info("rateThisApp");

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
        log.info("showLicenses");
        LicensesDialogFragment.newInstance(R.raw.licenses, true)
                .show(getSupportFragmentManager(), null);
    }

    @Override
    public void contactDeveloper() {
        log.info("contactDeveloper");

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
