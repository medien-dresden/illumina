package de.medienDresden;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.codechimp.apprater.AppRater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import de.medienDresden.acra.BitbucketReportSender;
import de.medienDresden.illumina.BuildConfig;
import de.medienDresden.illumina.R;
import de.medienDresden.illumina.pilight.Setting;
import de.medienDresden.illumina.service.PilightServiceImpl;
import de.psdev.licensesdialog.LicenseResolver;
import de.psdev.licensesdialog.licenses.CreativeCommonsAttributionNoDerivs30Unported;

@ReportsCrashes(
        formKey = "",
        applicationLogFileLines = 300,
        mode = ReportingInteractionMode.DIALOG,
        formUriBasicAuthLogin = BuildConfig.BITBUCKET_REPORTER_USER,
        formUriBasicAuthPassword = BuildConfig.BITBUCKET_REPORTER_PASSWORD,

        resToastText = R.string.crash_toast_text,
        resDialogText = R.string.crash_dialog_text,
        resDialogTitle = R.string.crash_dialog_title,
        resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
        resDialogOkToast = R.string.crash_dialog_ok_toast,

        customReportContent = {
                ReportField.APPLICATION_LOG,
                ReportField.STACK_TRACE,
                ReportField.PHONE_MODEL,
                ReportField.ANDROID_VERSION,
                ReportField.SHARED_PREFERENCES,
                ReportField.USER_COMMENT,
                ReportField.INSTALLATION_ID,
                ReportField.USER_EMAIL,
                ReportField.THREAD_DETAILS
        }
)
public class Illumina extends Application {

    private static final String TAG = Illumina.class.getSimpleName();

    public static final String PREF_HOST = "illumina.host";

    public static final String PREF_PORT = "illumina.port";

    public static final String PREF_THEME = "illumina.theme";

    public static final String PREF_AUTO_CONNECT = "illumina.auto_connect";

    public static final String PREFERENCES_NAME = BuildConfig.PACKAGE_NAME + "_preferences";

    public static final String LOG_FILE_NAME = "application.log";

    @Override
    public void onCreate() {
        super.onCreate();

        initErrorReporting();
        initLogging();
        initLicenses();
        initAppRater();

        /* If this service isn't started explicitly, it would be
         * destroyed if no more clients are bound */
        startService(new Intent(this, PilightServiceImpl.class));
    }

    private void initLogging() {
        final File logFile = new File(getFilesDir(), LOG_FILE_NAME);
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        final PatternLayoutEncoder logcatEncoder = new PatternLayoutEncoder();
        final RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        final LogcatAppender logcatAppender = new LogcatAppender();
        final RollingPolicy rolling = new TimeBasedRollingPolicy<>();
        final SizeBasedTriggeringPolicy<ILoggingEvent> trigger = new SizeBasedTriggeringPolicy<>();
        final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger)
                LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        loggerContext.reset();
        rolling.setParent(fileAppender);
        trigger.setMaxFileSize("1MB");

        encoder.setPattern("|%d{HH:mm:ss}|%thread|%level|%logger{0}|%msg|%n");
        encoder.setContext(loggerContext);
        encoder.start();

        logcatEncoder.setPattern("%msg%n");
        logcatEncoder.setContext(loggerContext);
        logcatEncoder.start();

        fileAppender.setRollingPolicy(rolling);
        fileAppender.setTriggeringPolicy(trigger);
        fileAppender.setContext(loggerContext);
        fileAppender.setFile(logFile.getAbsolutePath());
        fileAppender.setEncoder(encoder);
        fileAppender.start();

        logcatAppender.setContext(loggerContext);
        logcatAppender.setEncoder(logcatEncoder);
        logcatAppender.start();

        rootLogger.addAppender(fileAppender);
        rootLogger.addAppender(logcatAppender);

        loggerContext.getLogger(Setting.class).setLevel(Level.INFO);
    }

    private void initAppRater() {
        AppRater.setMarket(BuildConfig.MARKET);
    }

    private void initErrorReporting() {
        final File logFile = new File(getFilesDir(), LOG_FILE_NAME);

        ACRA.init(this);
        ACRA.getConfig().setApplicationLogFile(logFile.getAbsolutePath());

        try {
            ACRA.getErrorReporter().setReportSender(
                    new BitbucketReportSender(
                            BuildConfig.BITBUCKET_REPOSITORY_USER,
                            BuildConfig.BITBUCKET_REPOSITORY_NAME));

        } catch (Exception exception) {
            Log.e(TAG, "illumina won't be able to send error reports", exception);
        }
    }

    private void initLicenses() {
        LicenseResolver.registerLicense(
                new CreativeCommonsAttributionNoDerivs30Unported());
    }

    public SharedPreferences getSharedPreferences() {
        int flags = Context.MODE_PRIVATE;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            flags |= Context.MODE_MULTI_PROCESS;
        }

        return getSharedPreferences(Illumina.PREFERENCES_NAME, flags);
    }

}
