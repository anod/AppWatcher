package com.anod.appwatcher

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import androidx.work.Configuration
import com.anod.appwatcher.backup.createBackupModule
import com.anod.appwatcher.backup.gdrive.UploadServiceContentObserver
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.installed.createInstalledModule
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.sync.SyncNotification
import com.anod.appwatcher.utils.AppLogLogger
import com.anod.appwatcher.utils.networkConnection
import com.google.firebase.crashlytics.FirebaseCrashlytics
import finsky.api.DfeError
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.ApplicationInstance
import info.anodsplace.framework.app.CustomThemeActivity
import info.anodsplace.framework.app.WindowCustomTheme
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.IOException
import java.net.UnknownHostException

class AppWatcherApplication : Application(), AppLog.Listener, ApplicationInstance, Configuration.Provider, KoinComponent {

    override val nightMode: Int
        get() = get<Preferences>().nightMode

    val appsDatabase: AppsDatabase by lazy {
        Room.databaseBuilder(this, AppsDatabase::class.java, AppsDatabase.dbName)
                .enableMultiInstanceInvalidation()
                .addMigrations(*AppsDatabase.migrations)
                .build()
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .detectAll()
                    .detectNonSdkApiUsage()
                    .penaltyLog()
                    .build())
        }

        AppLog.setDebug(BuildConfig.DEBUG, "AppWatcher")

        startKoin {
            logger(AppLogLogger())
            modules(modules = listOf(
                    module {
                        single<Context> { this@AppWatcherApplication }
                        single<Application> { this@AppWatcherApplication }
                        single { appsDatabase }
                    },
                    createAppModule(),
                    createPlayStoreModule(),
                    createBackupModule(),
                    createInstalledModule()
            ))
        }

        val prefs = get<Preferences>()
        if (prefs.collectCrashReports) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            AppLog.logger = FirebaseLogger()
            AppLog.instance.listener = this
        }

        if (prefs.isDriveSyncEnabled) {
            appsDatabase.invalidationTracker.addObserver(get<UploadServiceContentObserver>())
        }

        AppCompatDelegate.setDefaultNightMode(prefs.nightMode)
        SyncNotification(ApplicationContext(this), get()).createChannels()
        registerActivityLifecycleCallbacks(LifecycleCallbacks())
    }

    override fun onLogException(tr: Throwable) {

        if (isNetworkError(tr) && !networkConnection.isNetworkAvailable) {
            // Ignore
            return
        }

        if (get<Preferences>().collectCrashReports) {
            FirebaseCrashlytics.getInstance().recordException(tr)
        }
    }

    private fun isNetworkError(tr: Throwable): Boolean {
        return tr is DfeError
                || tr is UnknownHostException
                || (tr is IOException && tr.message?.contains("NetworkError") == true)
                || networkConnection.isNetworkException(tr)
    }

    private inner class FirebaseLogger : AppLog.Logger.Android() {
        override fun println(priority: Int, tag: String, msg: String) {
            super.println(priority, tag, msg)
            FirebaseCrashlytics.getInstance().log("$priority/$tag: $msg")
        }
    }

    override fun getWorkManagerConfiguration() = Configuration.Builder().apply {
        setMinimumLoggingLevel(android.util.Log.DEBUG)
    }.build()
}

class LifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityStopped(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is CustomThemeActivity) {
            val themeRes = activity.themeRes
            if (themeRes > 0) {
                if (activity.themeColors.available) {
                    WindowCustomTheme.apply(activity.themeColors, activity.window, activity)
                }
                activity.setTheme(themeRes)
            }
        }
    }
}