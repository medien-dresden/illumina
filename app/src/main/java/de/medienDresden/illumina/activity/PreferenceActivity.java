package de.medienDresden.illumina.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import org.codechimp.apprater.AppRater;

import de.medienDresden.illumina.R;
import de.psdev.licensesdialog.LicensesDialogFragment;

public class PreferenceActivity extends FragmentActivity {

    private static final String TAG = PreferenceActivity.class.getSimpleName();

    public static final String ACTION_RATE
            = "de.medienDresden.illumina.ACTION_RATE";

    public static final String ACTION_LICENSES
            = "de.medienDresden.illumina.ACTION_LICENSES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.preference_fragment);

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

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
        }
    }

}
