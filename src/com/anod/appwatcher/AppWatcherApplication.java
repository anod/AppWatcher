package com.anod.appwatcher;

import android.app.Application;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

public class AppWatcherApplication extends Application {

	 @Override
	 public void onCreate() {
	     super.onCreate();

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
