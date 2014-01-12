package de.medienDresden.illumina;

import android.text.TextUtils;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.HttpSender;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.acra.util.HttpRequest;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.acra.ACRA.LOG_TAG;

public class BitbucketReportSender implements ReportSender {

    private static final String TPL_URI = "https://bitbucket.org/api/1.0/repositories/%s/%s/issues";

    private static final String TPL_H1 = "# %s #\n";

    private static final String TPL_H2 = "## %s ##\n";

    private static final String TPL_KEY_VALUE = "* %s: %s\n";

    private static final String TPL_CODE_PLAIN = "```\n#!text\n%s\n```\n";

    private final URL mUri;

    public BitbucketReportSender(String user, String repository) throws Exception {
        mUri = new URL(format(TPL_URI, user, repository));
    }

    private String format(String template, Object... args) {
        return String.format(Locale.getDefault(), template, args);
    }

    @Override
    public void send(CrashReportData report) throws ReportSenderException {
        try {
            Log.d(LOG_TAG, "Connect to " + mUri.toString());

            final String login = ACRAConfiguration.isNull(
                    ACRA.getConfig().formUriBasicAuthLogin()) ? null
                        : ACRA.getConfig().formUriBasicAuthLogin();

            final String password = ACRAConfiguration.isNull(
                    ACRA.getConfig().formUriBasicAuthPassword()) ? null
                    : ACRA.getConfig().formUriBasicAuthPassword();

            final HttpRequest request = new HttpRequest();
            request.setConnectionTimeOut(ACRA.getConfig().connectionTimeout());
            request.setSocketTimeOut(ACRA.getConfig().socketTimeout());
            request.setMaxNrRetries(ACRA.getConfig().maxNumberOfRequestRetries());
            request.setLogin(login);
            request.setPassword(password);
            request.setHeaders(ACRA.getConfig().getHttpHeaders());

            String reportAsString;
            final Map<String, String> finalReport = remap(report);
            reportAsString = HttpRequest.getParamsAsFormString(finalReport);

            request.send(mUri, HttpSender.Method.POST, reportAsString, HttpSender.Type.FORM);

        } catch (IOException exception) {
            throw new ReportSenderException(
                    "Error while sending " + ACRA.getConfig().reportType()
                        + " report via Http POST", exception);
        }
    }

    private Map<String, String> remap(CrashReportData report) {
        final Map<String, String> map = new HashMap<>();

        StringBuilder title = new StringBuilder();
        StringBuilder content = new StringBuilder();
        String version = report.getProperty(ReportField.APP_VERSION_NAME);

        String exception = "unknown exception";
        final String[] trace = TextUtils.split(report.getProperty(ReportField.STACK_TRACE), "\n");
        if (trace.length > 0) {
            final String line = trace[0];
            final String[] components = TextUtils.split(line, ": ");

            if (components.length > 1) {
                exception  = components[0].substring(components[0].lastIndexOf(".") + 1);
                exception += ": ";
                exception += components[1];
            } else {
                exception = line;
            }
        }

        title.append(exception).append(" (")
                .append(report.getProperty(ReportField.PHONE_MODEL)).append(", ")
                .append(report.getProperty(ReportField.ANDROID_VERSION)).append(")");

        final String preferences = report.getProperty(ReportField.SHARED_PREFERENCES);
        final String[] preferencesArray = TextUtils.split(preferences, "\n");

        StringBuilder preferencesBuilder = new StringBuilder("\n");
        for (String preference : preferencesArray) {
            if (!TextUtils.isEmpty(preference)) {
                preferencesBuilder.append("* ").append(preference).append('\n');
            }
        }

        content.append(format(TPL_H1, "User Data"));
        content.append(format(TPL_KEY_VALUE, "comment",
                report.getProperty(ReportField.USER_COMMENT)));
        content.append(format(TPL_KEY_VALUE, "mail",
                report.getProperty(ReportField.USER_EMAIL)));
        content.append(format(TPL_KEY_VALUE, "installation",
                report.getProperty(ReportField.INSTALLATION_ID)));

        content.append(format(TPL_H2, "Preferences"));
        content.append(preferencesBuilder.toString());

        content.append(format(TPL_H1, "Traces and Logs"));
        content.append(format(TPL_H2, "Stack Trace"));
        content.append(format(TPL_CODE_PLAIN,
                report.getProperty(ReportField.STACK_TRACE)));

        final String logCat =  report.getProperty(ReportField.LOGCAT);

        if (!TextUtils.isEmpty(logCat)) {
            content.append(format(TPL_H2, "Logcat"));
            content.append(format(TPL_CODE_PLAIN,
                    report.getProperty(ReportField.LOGCAT)));
        }

        final String threadingDetails = report.getProperty(ReportField.THREAD_DETAILS);

        if (!TextUtils.isEmpty(threadingDetails)) {
            content.append(format(TPL_H1, "Threading"));
            content.append(format(TPL_CODE_PLAIN, threadingDetails));
        }

        map.put("title", title.toString());
        map.put("content", content.toString());
        map.put("kind", "task");

        return map;
    }

}
