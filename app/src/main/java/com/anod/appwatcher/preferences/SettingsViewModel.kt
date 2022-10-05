package com.anod.appwatcher.preferences

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.Operation
import com.anod.appwatcher.R
import com.anod.appwatcher.backup.ExportBackupTask
import com.anod.appwatcher.backup.ImportBackupTask
import com.anod.appwatcher.backup.gdrive.GDriveSync
import com.anod.appwatcher.backup.gdrive.UploadServiceContentObserver
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.sync.SchedulesHistoryActivity
import com.anod.appwatcher.sync.SyncNotification
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.sync.UpdatedApp
import com.anod.appwatcher.userLog.UserLogActivity
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.prefs
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import info.anodsplace.applog.AppLog
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.NotificationManager
import info.anodsplace.framework.content.forAppInfo
import info.anodsplace.framework.playservices.GooglePlayServices
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf


data class SettingsViewState(
        val items: List<PreferenceItem>,
        val isProgressVisible: Boolean = false,
        val recreateWatchlistOnBack: Boolean = false,
        val areNotificationsEnabled: Boolean = false
)

sealed interface SettingsViewEvent {
    class Export(val uri: Uri) : SettingsViewEvent
    class Import(val uri: Uri) : SettingsViewEvent
    class UpdateIconsShape(val newPath: String) : SettingsViewEvent
    class GDriveSyncToggle(val checked: Boolean) : SettingsViewEvent
    object GDriveSyncNow : SettingsViewEvent
    class ChangeUpdatePolicy(val frequency: Int, val isWifiOnly: Boolean, val isRequiresCharging: Boolean) : SettingsViewEvent
    class UpdateCrashReports(val checked: Boolean) : SettingsViewEvent
    class SetRecreateFlag(val item: PreferenceItem, val enabled: Boolean, val update: (Boolean) -> Unit) : SettingsViewEvent
    class UpdateTheme(val newTheme: Int) : SettingsViewEvent
    object OnBackNav : SettingsViewEvent
    object TestNotification : SettingsViewEvent
    object OssLicenses : SettingsViewEvent
    object OpenUserLog : SettingsViewEvent
    object OpenRefreshHistory : SettingsViewEvent
    class GDriveLoginResult(val isSuccess: Boolean, val errorCode: Int) : SettingsViewEvent
    object NotificationPermissionRequest : SettingsViewEvent
    class NotificationPermissionResult(val granted: Boolean) : SettingsViewEvent
    object ShowAppSettings : SettingsViewEvent
    object CheckNotificationPermission : SettingsViewEvent
}

sealed interface SettingsViewAction {
    object OnBackNav : SettingsViewAction
    class StartActivity(val intent: Intent) : SettingsViewAction
    object GDriveSignIn : SettingsViewAction
    object GDriveSignOut : SettingsViewAction
    class GDriveErrorIntent(val intent: Intent) : SettingsViewAction
    object Recreate : SettingsViewAction
    object Rebirth : SettingsViewAction
    object RequestNotificationPermission : SettingsViewAction

    class ShowToast(@StringRes val resId: Int = 0, val text: String = "", val length: Int = Toast.LENGTH_SHORT) : SettingsViewAction
    class ExportResult(val result: Int) : SettingsViewAction
    class ImportResult(val result: Int) : SettingsViewAction
}

class SettingsViewModel : BaseFlowViewModel<SettingsViewState, SettingsViewEvent, SettingsViewAction>(), KoinComponent {
    private val application: Application by inject()
    private val playServices: GooglePlayServices = GooglePlayServices(application)
    private val appScope: CoroutineScope by inject()
    private val context: Context by inject()
    private val notificationManager: NotificationManager by inject()

    init {
        viewState = SettingsViewState(
                items = preferenceItems(prefs, false, playServices, application),
                areNotificationsEnabled = notificationManager.areNotificationsEnabled
        )
    }

    override fun handleEvent(event: SettingsViewEvent) {
        when (event) {
            is SettingsViewEvent.ChangeUpdatePolicy -> changeUpdatePolicy(event.frequency, event.isWifiOnly, event.isRequiresCharging)
            is SettingsViewEvent.Export -> {
                appScope.launch {
                    export(event.uri).collect { result ->
                        viewState = viewState.copy(isProgressVisible = result == -1)
                        emitAction(SettingsViewAction.ExportResult(result))
                    }
                }
            }
            is SettingsViewEvent.Import -> {
                appScope.launch {
                    import(event.uri).collect { result ->
                        viewState = viewState.copy(isProgressVisible = result == -1)
                        emitAction(SettingsViewAction.ImportResult(result))
                    }
                }
            }
            is SettingsViewEvent.GDriveLoginResult -> onGDriveLoginResult(event.isSuccess, event.errorCode)
            SettingsViewEvent.GDriveSyncNow -> gDriveSyncNow()
            is SettingsViewEvent.GDriveSyncToggle -> gDriveSyncToggle(event.checked)
            SettingsViewEvent.OnBackNav -> emitAction(SettingsViewAction.OnBackNav)
            SettingsViewEvent.OpenRefreshHistory -> emitAction(SettingsViewAction.StartActivity(Intent(application, SchedulesHistoryActivity::class.java)))
            SettingsViewEvent.OpenUserLog -> emitAction(SettingsViewAction.StartActivity(Intent(application, UserLogActivity::class.java)))
            SettingsViewEvent.OssLicenses -> emitAction(SettingsViewAction.StartActivity(Intent(application, OssLicensesMenuActivity::class.java)))
            is SettingsViewEvent.SetRecreateFlag -> {
                val result = setRecreateFlag(event.item, event.enabled)
                event.update(result)
            }
            SettingsViewEvent.TestNotification -> testNotification()
            is SettingsViewEvent.UpdateCrashReports -> updateCrashReports(event.checked)
            is SettingsViewEvent.UpdateIconsShape -> updateIconsShape(event.newPath)
            is SettingsViewEvent.UpdateTheme -> updateTheme(event.newTheme)
            SettingsViewEvent.NotificationPermissionRequest -> {
                AppPermissions.isGranted(application, AppPermission.PostNotification)

                emitAction(SettingsViewAction.RequestNotificationPermission)
            }
            is SettingsViewEvent.NotificationPermissionResult -> {
                viewState = viewState.copy(
                        areNotificationsEnabled = prefs.areNotificationsEnabled,
                        items = preferenceItems(prefs, inProgress = false, playServices, application)
                )
            }
            SettingsViewEvent.ShowAppSettings -> emitAction(SettingsViewAction.StartActivity(Intent().forAppInfo(application.packageName, application)))
            SettingsViewEvent.CheckNotificationPermission -> {
                val areNotificationsEnabled = prefs.areNotificationsEnabled
                if (areNotificationsEnabled != viewState.areNotificationsEnabled) {
                    viewState = viewState.copy(
                            areNotificationsEnabled = prefs.areNotificationsEnabled,
                            items = preferenceItems(prefs, inProgress = false, playServices, application)
                    )
                }
            }
        }
    }

    private fun import(srcUri: Uri) = flow {
        emit(-1)
        val task = get<ImportBackupTask>()
        emit(task.execute(srcUri))
    }

    private fun export(dstUri: Uri) = flow {
        emit(-1)
        val task = get<ExportBackupTask>()
        emit(task.execute(dstUri))
    }

    private fun gDriveSyncToggle(checked: Boolean) {
        if (checked) {
            prefs.isDriveSyncEnabled = true
            viewState = viewState.copy(
                    isProgressVisible = true,
                    items = preferenceItems(prefs, inProgress = true, playServices, application)
            )
            emitAction(SettingsViewAction.GDriveSignIn)
        } else {
            prefs.isDriveSyncEnabled = false
            viewState = viewState.copy(
                    isProgressVisible = false,
                    items = preferenceItems(prefs, inProgress = false, playServices, application)
            )
            emitAction(SettingsViewAction.GDriveSignOut)
        }
    }

    private fun gDriveSyncNow() {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (googleAccount != null) {
            appScope.launch {
                try {
                    viewState = viewState.copy(isProgressVisible = true)
                    emitAction(SettingsViewAction.ShowToast(resId = R.string.sync_start))
                    val gDriveSync = get<GDriveSync> { parametersOf(googleAccount) }
                    gDriveSync.doSync()
                    prefs.lastDriveSyncTime = System.currentTimeMillis()
                    viewState = viewState.copy(
                            isProgressVisible = false,
                            items = preferenceItems(prefs, inProgress = false, playServices, application)
                    )
                    emitAction(SettingsViewAction.ShowToast(resId = R.string.sync_finish))
                } catch (e: Exception) {
                    AppLog.e(e)
                    viewState = viewState.copy(isProgressVisible = false)
                    emitAction(SettingsViewAction.ShowToast(resId = R.string.sync_error))
                    if (e is GDriveSync.SyncError && e.error?.intent != null) {
                        emitAction(SettingsViewAction.GDriveErrorIntent(e.error.intent!!))
                    }
                }
            }
        } else {
            viewState = viewState.copy(isProgressVisible = false)
            emitAction(SettingsViewAction.ShowToast(resId = R.string.no_gdrive_account))
        }
    }

    private fun onGDriveLoginResult(isSuccess: Boolean, errorCode: Int) {
        if (isSuccess) {
            prefs.isDriveSyncEnabled = true
            val observer = get<UploadServiceContentObserver>()
            val invalidationTracker = get<AppsDatabase>().invalidationTracker
            invalidationTracker.removeObserver(observer)
            invalidationTracker.addObserver(observer)
            emitAction(SettingsViewAction.ShowToast(resId = R.string.gdrive_connected))
            viewState = viewState.copy(
                    isProgressVisible = false,
                    items = preferenceItems(prefs, inProgress = false, playServices, application)
            )
        } else {
            prefs.isDriveSyncEnabled = false
            emitAction(SettingsViewAction.ShowToast(text = "Drive login error $errorCode"))
            viewState = viewState.copy(
                    isProgressVisible = false,
                    items = preferenceItems(prefs, inProgress = false, playServices, application)
            )
        }
    }

    private fun changeUpdatePolicy(frequency: Int, isWifiOnly: Boolean, isRequiresCharging: Boolean) {
        prefs.updatesFrequency = frequency
        prefs.isWifiOnly = isWifiOnly
        prefs.isRequiresCharging = isRequiresCharging

        val useAutoSync = prefs.useAutoSync

        appScope.launch {
            viewState = viewState.copy(
                    items = preferenceItems(prefs, inProgress = viewState.isProgressVisible, playServices, application)
            )
            if (useAutoSync) {
                SyncScheduler(context).schedule(
                        prefs.isRequiresCharging,
                        prefs.isWifiOnly,
                        prefs.updatesFrequency.toLong(),
                        true
                ).first { it !is Operation.State.IN_PROGRESS }
            } else {
                SyncScheduler(context).cancel().first { it !is Operation.State.IN_PROGRESS }
            }
        }
    }

    private fun setRecreateFlag(item: PreferenceItem, oldValue: Boolean): Boolean {
        val newValue = (item as PreferenceItem.Switch).checked
        if (!viewState.recreateWatchlistOnBack) {
            viewState = viewState.copy(recreateWatchlistOnBack = oldValue != newValue)
        }
        return newValue
    }

    private fun updateTheme(newThemeIndex: Int) {
        if (prefs.themeIndex == newThemeIndex) {
            return
        }
        val nightMode = prefs.uiMode
        val theme = prefs.theme
        prefs.themeIndex = newThemeIndex
        var recreate = false
        if (prefs.theme != theme) {
            recreate = true
        }
        if (prefs.uiMode != nightMode) {
            recreate = true
        }
        if (recreate) {
            AppCompatDelegate.setDefaultNightMode(prefs.appCompatNightMode)
            viewState = viewState.copy(recreateWatchlistOnBack = true)
            emitAction(SettingsViewAction.Recreate)
        }
    }

    private fun updateIconsShape(newIconShape: String) {
        if (prefs.iconShape == newIconShape) {
            return
        }
        prefs.iconShape = newIconShape
        viewState = viewState.copy(recreateWatchlistOnBack = true)
    }

    private fun updateCrashReports(checked: Boolean) {
        prefs.collectCrashReports = checked
        emitAction(SettingsViewAction.Recreate)
    }

    private fun testNotification() {
        SyncNotification(ApplicationContext(context), get()).show(listOf(
                UpdatedApp(
                        packageName = "com.anod.appwatcher",
                        title = "Test",
                        installedVersionCode = 25,
                        isNewUpdate = true,
                        recentChanges = "Test notification",
                        uploadDate = "Now",
                        uploadTime = System.currentTimeMillis(),
                        versionNumber = 27,
                )
        ))
    }
}