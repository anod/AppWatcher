package com.anod.appwatcher

import android.app.Application
import android.content.Context
import android.os.Build
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
import com.anod.appwatcher.utils.networkConnection
import com.google.firebase.crashlytics.FirebaseCrashlytics
import finsky.api.DfeError
import info.anodsplace.applog.AndroidLogger
import info.anodsplace.applog.AppLog
import info.anodsplace.context.ApplicationInstance
import java.io.IOException
import java.net.UnknownHostException
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.dsl.module

class AppWatcherApplication : Application(), AppLog.Listener, ApplicationInstance, Configuration.Provider, KoinComponent {

    override val workManagerConfiguration = Configuration.Builder().apply {
        setMinimumLoggingLevel(android.util.Log.DEBUG)
    }.build()

    override val appCompatNightMode: Int
        get() = get<Preferences>().appCompatNightMode

    val appsDatabase: AppsDatabase by lazy {
        Room.databaseBuilder(this, AppsDatabase::class.java, AppsDatabase.dbName)
            .enableMultiInstanceInvalidation()
            .addMigrations(*AppsDatabase.migrations)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .detectAll()
                .detectNonSdkApiUsage()
                .penaltyLog()
                .build())
        }

        AppLog.setDebug(BuildConfig.DEBUG, "AppWatcher")

        startKoin {
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

        AppCompatDelegate.setDefaultNightMode(prefs.appCompatNightMode)
        SyncNotification(info.anodsplace.context.ApplicationContext(this), get()).createChannels()
    }

    override fun onLogException(tr: Throwable) {
        if (isNetworkError(tr) || tr is kotlinx.coroutines.CancellationException) {
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

    private inner class FirebaseLogger : AndroidLogger() {
        override fun println(priority: Int, tag: String, msg: String) {
            super.println(priority, tag, msg)
            FirebaseCrashlytics.getInstance().log("$priority/$tag: $msg")
        }
    }
}