package de.medienDresden.illumina.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.Log;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import de.medienDresden.Illumina;
import de.medienDresden.illumina.R;

/**
* Created by peter on 18.01.14.
*/
public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    public static final String RATE = "illumina.rate";

    public static final String LICENSES = "illumina.licenses";

    public static final String CONTACT = "illumina.contact";

    public interface SettingsListener {

        void refreshTheme();

        void rateThisApp();

        void showLicenses();

        void contactDeveloper();

    }

    private final Preference.OnPreferenceClickListener mPreferenceClickedListener
            = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case RATE:
                    mSettingsListener.rateThisApp();
                    return true;

                case LICENSES:
                    mSettingsListener.showLicenses();
                    return true;

                case CONTACT:
                    mSettingsListener.contactDeveloper();
                    return true;

                default:
                    return false;
            }
        }
    };

    private SettingsListener mSettingsListener;

    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (TextUtils.equals(key, Illumina.PREF_THEME) && getActivity() != null) {
                mSettingsListener.refreshTheme();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        final Preference ratePref = getPreferenceScreen().findPreference(RATE);
        final Preference licensesPref = getPreferenceScreen().findPreference(LICENSES);
        final Preference contactPref = getPreferenceScreen().findPreference(CONTACT);

        assert ratePref != null;
        assert licensesPref != null;
        assert contactPref != null;

        ratePref.setOnPreferenceClickListener(mPreferenceClickedListener);
        licensesPref.setOnPreferenceClickListener(mPreferenceClickedListener);
        contactPref.setOnPreferenceClickListener(mPreferenceClickedListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferences().registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        assert getPreferenceManager().getSharedPreferences() != null;
        getPreferences().unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mSettingsListener = (SettingsListener) activity;
        } catch (ClassCastException exception) {
            Log.e(TAG, activity.getClass().getSimpleName() + " should implement "
                    + getClass().getSimpleName(), exception);
        }
    }

    private SharedPreferences getPreferences() {
        return getPreferenceManager().getSharedPreferences();
    }

}
