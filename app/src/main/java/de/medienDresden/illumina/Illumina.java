package de.medienDresden.illumina;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import de.medienDresden.illumina.impl.PilightServiceImpl;

@ReportsCrashes(
        formKey = "",
        formUriBasicAuthLogin = "mddapp",
        formUriBasicAuthPassword = "QqPJkss6",

        // there should be a better way
        logcatArguments = {
                "-t", "800",
                "-v", "tag",
                "DeviceListFragment:D",
                "Illumina:D",
                "LocationListActivity:D",
                "ReaderThread:D",
                "WriterThread:D",
                "StreamingSocketImpl:D",
                "PilightServiceImpl:D",
                "Setting:D",
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

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Illumina started");

        ACRA.init(this);

        try {
            ACRA.getErrorReporter().setReportSender(new BitbucketReportSender("phdd", "illumina"));
        } catch (Exception exception) {
            Log.e(TAG, "illumina won't be able to send error reports", exception);
        }

        /* If this service isn't started explicitly, it would be
         * destroyed if no more clients are bound */
        startService(new Intent(this, PilightServiceImpl.class));
    }

}
