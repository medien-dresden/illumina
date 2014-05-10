/*
 * illumina, a pilight remote
 *
 * Copyright (c) 2014 Peter Heisig <http://google.com/+PeterHeisig>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
