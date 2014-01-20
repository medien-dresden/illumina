package de.medienDresden;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import de.medienDresden.acra.BitbucketReportSender;
import de.medienDresden.illumina.BuildConfig;
import de.medienDresden.illumina.R;
import de.medienDresden.illumina.service.PilightServiceImpl;



@ReportsCrashes(
        formKey = "",
        formUriBasicAuthLogin = "mddapp",
        formUriBasicAuthPassword = BuildConfig.BITBUCKET_PASSWORD,

        // there should be a better way
        logcatArguments = {
                "-t", "1000",
                "-v", "tag",
                "BaseActivity:D",
                "ConnectionActivity:D",
                "LocationListActivity:D",
                "PreferenceActivity:D",
                "ReaderThread:D",
                "StreamingSocketImpl:D",
                "WriterThread:D",
                "BaseListFragment:D",
                "DeviceListFragment:D",
                "SettingsFragment:D",
                "Setting:D",
                "PilightBinder:D",
                "PilightServiceImpl:D",
                "DeviceAdapter:D",
                "LocationPagerAdapter:D",
                "*:E"
        },

        mode = ReportingInteractionMode.DIALOG,
        resToastText = R.string.crash_toast_text,
        resDialogText = R.string.crash_dialog_text,
        resDialogTitle = R.string.crash_dialog_title,
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
        resDialogOkToast = R.string.crash_dialog_ok_toast
)
public class Illumina extends Application {

    private static final String TAG = Illumina.class.getSimpleName();

    public static final String PREF_HOST = "illumina.host";

    public static final String PREF_PORT = "illumina.port";

    public static final String PREF_THEME = "illumina.theme";

    public static final String PREFERENCES_NAME = BuildConfig.PACKAGE_NAME + "_preferences";

    @Override
    public void onCreate() {
        super.onCreate();

        initErrorReporting();

        /* If this service isn't started explicitly, it would be
         * destroyed if no more clients are bound */
        startService(new Intent(this, PilightServiceImpl.class));
    }

    private void initErrorReporting() {
        ACRA.init(this);

        try {
            ACRA.getErrorReporter().setReportSender(
                    new BitbucketReportSender("phdd", "illumina"));

        } catch (Exception exception) {
            Log.e(TAG, "illumina won't be able to send error reports", exception);
        }
    }

    public SharedPreferences getSharedPreferences() {
        int flags = Context.MODE_PRIVATE;

        if (Build.VERSION.SDK_INT >= 11) {
            flags |= Context.MODE_MULTI_PROCESS;
        }

        return getSharedPreferences(Illumina.PREFERENCES_NAME, flags);
    }

}
