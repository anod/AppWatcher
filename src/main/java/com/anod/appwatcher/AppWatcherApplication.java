package com.anod.appwatcher;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

import com.newrelic.agent.android.NewRelic;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
	formKey = "", // This is required for backward compatibility but not used
	formUri = "https://anodsplace.info/acra/report/report.php",
	httpMethod = org.acra.sender.HttpSender.Method.PUT,
	reportType = org.acra.sender.HttpSender.Type.JSON,
	logcatArguments = { "-t", "100", "-v", "tag", "AppWatcher:V", "*:S" }
)
public class AppWatcherApplication extends Application {

	 @Override
	 public void onCreate() {
		super.onCreate();

		ACRA.init(this);

		 TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		 ErrorReporter er = ACRA.getErrorReporter();
		 er.putCustomData("NetworkOperatorName", tm.getNetworkOperatorName());
		 er.putCustomData("SimOperatorName", tm.getSimOperatorName());
		 er.putCustomData("NetworkOperator", tm.getNetworkOperator());
		 er.putCustomData("SimOperator", tm.getSimOperator());

		 NewRelic.withApplicationToken(
			"AA47c4b684f2af988fdf3a13518738d7eaa8a4976f"
		 ).start(this);

		 try {
			 ViewConfiguration config = ViewConfiguration.get(this);
			 Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			 if(menuKeyField != null) {
				 menuKeyField.setAccessible(true);
				 menuKeyField.setBoolean(config, false);
			 }
		 } catch (Exception ex) {
			 // Ignore
		 }

	 }



}
