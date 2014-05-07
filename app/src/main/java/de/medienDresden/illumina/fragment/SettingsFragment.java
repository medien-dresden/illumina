/*
 * Copyright 2014 Peter Heisig
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package de.medienDresden.illumina.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.text.TextUtils;
import android.view.View;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.medienDresden.Illumina;
import de.medienDresden.illumina.R;

/**
* Created by peter on 18.01.14.
*/
public class SettingsFragment extends PreferenceFragment {

    public static final Logger log = LoggerFactory.getLogger(SettingsFragment.class);

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final int padding = (int) getResources().getDimension(R.dimen.settings_margin);
        getListView().setPadding(padding, 0, padding, 0);
        getListView().setFooterDividersEnabled(false);
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
            log.error(activity.getClass().getSimpleName() + " should implement "
                    + SettingsListener.class.getSimpleName(), exception);
        }
    }

    private SharedPreferences getPreferences() {
        return getPreferenceManager().getSharedPreferences();
    }

}
