package com.anod.appwatcher.preferences

import android.app.Application
import android.app.Notification
import android.content.Context
import com.anod.appwatcher.backup.DbBackupManager
import com.anod.appwatcher.backup.ExportBackupTask
import com.anod.appwatcher.backup.ImportBackupTask
import com.anod.appwatcher.backup.gdrive.UploadServiceContentObserver
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.diagnostics.DiagnosticRecorder
import info.anodsplace.context.ApplicationContext
import info.anodsplace.notification.NotificationManager
import info.anodsplace.playservices.GooglePlayServices
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.Module
import org.koin.dsl.module
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

class TestNotificationManager(override var areNotificationsEnabled: Boolean = true) : NotificationManager {
    val posted = mutableListOf<Pair<Int, Notification>>()
    val cancelled = mutableListOf<Int>()

    override fun notify(notificationId: Int, notification: Notification) {
        posted += notificationId to notification
    }

    override fun cancel(notificationId: Int) {
        cancelled += notificationId
    }
}

/** The Koin bindings the Settings screen and [SettingsViewModel] look up, backed by the given test doubles. */
fun settingsTestModule(
    app: Application,
    appScope: CoroutineScope,
    notifications: NotificationManager,
    preferences: Preferences,
    recorder: DiagnosticRecorder,
    database: AppsDatabase
): Module {
    val backup = DbBackupManager(ApplicationContext(app), database)
    return module {
        single<Application> { app }
        single<Context> { app }
        single<CoroutineScope> { appScope }
        single<NotificationManager> { notifications }
        single { preferences }
        single { recorder }
        single { database }
        single { ExportBackupTask(backup) }
        single { ImportBackupTask(backup) }
        single { UploadServiceContentObserver(app, preferences) }
    }
}

/** Adds an app to the watch list; its package name is `<appId>.package`. */
suspend fun AppsDatabase.insertWatchedApp(appId: String) {
    AppListTable.Queries.insert(
        App(
            rowId = 0, appId = appId, packageName = "$appId.package", versionNumber = 1, versionName = "1.0", title = appId,
            creator = "creator", iconUrl = "", status = App.STATUS_NORMAL, uploadDate = "", price = Price("", "", 0),
            detailsUrl = null, uploadTime = 0, appType = "", syncTime = 0
        ),
        this
    )
}

/** Reports Google Play Services as available, which Robolectric never does, to exercise the Drive sync settings. */
@Implements(GooglePlayServices::class)
class ShadowSupportedPlayServices {
    @Implementation
    fun isSupported(): Boolean = true
}