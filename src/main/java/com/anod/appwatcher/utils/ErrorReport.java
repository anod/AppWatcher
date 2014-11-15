package com.anod.appwatcher.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.anod.appwatcher.AppWatcherApplication;

import org.acra.ACRA;
import org.acra.ErrorReporter;

/**
 * @author alex
 * @date 2014-11-15
 */
public class ErrorReport {

    private static final boolean ENABLED = false;
    public static void putCustomData(String key, String value) {
       if (ENABLED) ACRA.getErrorReporter().putCustomData(key, value);
    }

    public static void handleException(Throwable e) {
        if (ENABLED) ACRA.getErrorReporter().handleException(e);
    }

    public static void setDefaultReportSenders() {
        if (ENABLED) ACRA.getErrorReporter().setDefaultReportSenders();
    }

    public static void reportByEmail(Context context) {
        if (!ENABLED){
            return;
        }
        ErrorReporter rs = ACRA.getErrorReporter();
        rs.removeAllReportSenders();

        EmailReportSender sender = new EmailReportSender(AppWatcherApplication.get(context));
        rs.setReportSender(sender);
        Throwable ex = new Throwable("Report a problem");
        rs.handleException(ex);
    }

    public static void init(AppWatcherApplication app) {
        if (!ENABLED){
            return;
        }

        ACRA.init(app);

        TelephonyManager tm = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
        ErrorReporter er = ACRA.getErrorReporter();
        er.putCustomData("NetworkOperatorName", tm.getNetworkOperatorName());
        er.putCustomData("SimOperatorName", tm.getSimOperatorName());
        er.putCustomData("NetworkOperator", tm.getNetworkOperator());
        er.putCustomData("SimOperator", tm.getSimOperator());

    }
}
