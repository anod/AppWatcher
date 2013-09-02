package com.anod.appwatcher;

import android.app.Application;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

import com.newrelic.agent.android.NewRelic;


public class AppWatcherApplication extends Application {

	 @Override
	 public void onCreate() {
		super.onCreate();

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
