package com.anod.appwatcher;

import org.acra.*;
import org.acra.annotation.*;

import android.app.Application;

@ReportsCrashes (formKey = "dFY4YVFTcGY5ZTFzNWJNRGtCNWJySXc6MQ")
public class AppWatcherApplication extends Application {

	 @Override
	 public void onCreate() {
	     // The following line triggers the initialization of ACRA
		 ACRA.init(this);
	     super.onCreate();
	 }
}
