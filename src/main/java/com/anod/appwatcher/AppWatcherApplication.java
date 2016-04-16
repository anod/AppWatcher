package com.anod.appwatcher;

import android.app.Application;
import android.content.Context;
import android.view.ViewConfiguration;

import com.squareup.leakcanary.LeakCanary;

import java.lang.reflect.Field;

import info.anodsplace.android.log.AppLog;

public class AppWatcherApplication extends Application implements AppLog.Listener {
    private ObjectGraph mObjectGraph;

    @Override
	 public void onCreate() {
		super.onCreate();
		LeakCanary.install(this);

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

        AppLog.setDebug(BuildConfig.DEBUG, "AppWatcher");
        AppLog.instance().setListener(this);

        mObjectGraph = new ObjectGraph(this);
    }

    public ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }

    public static AppWatcherApplication get(Context context) {
        return (AppWatcherApplication)context.getApplicationContext();
    }

    public static ObjectGraph provide(Context context) {
        return ((AppWatcherApplication) context.getApplicationContext()).getObjectGraph();
    }

    @Override
    public void onLogException(Throwable tr) {
        // Ignore for now - ExceptionHandler.saveException(tr, null, null);
    }
}
