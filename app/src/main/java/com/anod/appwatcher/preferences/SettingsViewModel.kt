package com.anod.appwatcher.preferences

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import androidx.work.Operation
import com.anod.appwatcher.R
import com.anod.appwatcher.backup.ExportBackupTask
import com.anod.appwatcher.backup.ImportBackupTask
import com.anod.appwatcher.backup.gdrive.GDriveSignIn
import com.anod.appwatcher.backup.gdrive.GDriveSync
import com.anod.appwatcher.backup.gdrive.UploadServiceContentObserver
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.Cleanup
import com.anod.appwatcher.navigation.SceneNavKey
import com.anod.appwatcher.sync.SyncNotification
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.sync.UpdatedApp
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.prefs
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import info.anodsplace.applog.AppLog
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.context.ApplicationContext
import info.anodsplace.framework.app.FoldableDeviceLayout
import info.anodsplace.framework.content.ShowToastActionDefaults
import info.anodsplace.framework.content.StartActivityAction
import info.anodsplace.framework.content.forAppInfo
import info.anodsplace.notification.NotificationManager
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import info.anodsplace.playservices.GooglePlayServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

@Immutable
data class SettingsViewState(
    val items: List<PreferenceItem>,
    val isProgressVisible: Boolean = false,
    val recreateWatchlistOnBack: Boolean = false,
    val areNotificationsEnabled: Boolean = false,
    val wideLayout: FoldableDeviceLayout = FoldableDeviceLayout()
)

sealed interface SettingsViewEvent {
    class Export(val uri: Uri) : SettingsViewEvent
    class Import(val uri: Uri) : SettingsViewEvent
    class UpdateIconsShape(val newPath: String) : SettingsViewEvent
    class GDriveSyncToggle(val checked: Boolean) : SettingsViewEvent
    object GDriveSyncNow : SettingsViewEvent
    class GDriveActivityResult(val activityResult: ActivityResult) : SettingsViewEvent
    class ChangeUpdatePolicy(val frequency: Int, val isWifiOnly: Boolean, val isRequiresCharging: Boolean) : SettingsViewEvent
    class UpdateCrashReports(val checked: Boolean) : SettingsViewEvent
    class SetRecreateFlag(val item: PreferenceItem, val enabled: Boolean, val update: (Boolean) -> Unit) : SettingsViewEvent
    class UpdateTheme(val newTheme: Int) : SettingsViewEvent
    object NavigateBack : SettingsViewEvent
    object TestNotification : SettingsViewEvent
    object OssLicenses : SettingsViewEvent
    object OpenUserLog : SettingsViewEvent
    object OpenRefreshHistory : SettingsViewEvent
    object NotificationPermissionRequest : SettingsViewEvent
    class NotificationPermissionResult(val granted: Boolean) : SettingsViewEvent
    class SetWideLayout(val wideLayout: FoldableDeviceLayout) : SettingsViewEvent
    object ShowAppSettings : SettingsViewEvent
    object CheckNotificationPermission : SettingsViewEvent
    object DbCleanup : SettingsViewEvent
}

sealed interface SettingsViewAction {
    data object NavigateBack : SettingsViewAction
    data class StartActivity(override val intent: Intent) : SettingsViewAction, StartActivityAction
    class ShowToast(@StringRes resId: Int = 0, text: String = "", length: Int = Toast.LENGTH_SHORT) : ShowToastActionDefaults(resId, text, length), SettingsViewAction
    class GDriveErrorIntent(val intent: Intent) : SettingsViewAction
    object Recreate : SettingsViewAction
    object Rebirth : SettingsViewAction
    object RequestNotificationPermission : SettingsViewAction
    class ExportResult(val result: Int) : SettingsViewAction
    class ImportResult(val result: Int) : SettingsViewAction
    data class NavigateTo(val navKey: NavKey) : SettingsViewAction
}

private fun showToastAction(@StringRes resId: Int = 0, text: String = "", length: Int = Toast.LENGTH_SHORT): SettingsViewAction {
    return SettingsViewAction.ShowToast(
        resId = resId,
        text = text,
        length = length
    )
}

class SettingsViewModel : BaseFlowViewModel<SettingsViewState, SettingsViewEvent, SettingsViewAction>(), KoinComponent {
    private val application: Application by inject()
    private val playServices: GooglePlayServices = GooglePlayServices(application)
    private val appScope: CoroutineScope by inject()
    private val context: Context by inject()
    private val notificationManager: NotificationManager by inject()
    private val gDriveSignIn: GDriveSignIn by lazy { GDriveSignIn(ApplicationContext(application)) }

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
            SettingsViewEvent.GDriveSyncNow -> gDriveSyncNow()
            is SettingsViewEvent.GDriveSyncToggle -> gDriveSyncToggle(event.checked)
            SettingsViewEvent.NavigateBack -> emitAction(SettingsViewAction.NavigateBack)
            SettingsViewEvent.OpenRefreshHistory -> emitAction(
                SettingsViewAction.NavigateTo(
                    navKey = SceneNavKey.RefreshHistory
                )
            )

            SettingsViewEvent.OpenUserLog -> emitAction(
                SettingsViewAction.NavigateTo(
                    navKey = SceneNavKey.UserLog
            ))

            SettingsViewEvent.OssLicenses -> emitAction(
                SettingsViewAction.StartActivity(
                Intent(application, OssLicensesMenuActivity::class.java),
            ))
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
            SettingsViewEvent.ShowAppSettings -> emitAction(
                SettingsViewAction.StartActivity(
                intent = Intent().forAppInfo(application.packageName),
            ))
            SettingsViewEvent.CheckNotificationPermission -> {
                val areNotificationsEnabled = prefs.areNotificationsEnabled
                if (areNotificationsEnabled != viewState.areNotificationsEnabled) {
                    viewState = viewState.copy(
                        areNotificationsEnabled = prefs.areNotificationsEnabled,
                        items = preferenceItems(prefs, inProgress = false, playServices, application)
                    )
                }
            }

            SettingsViewEvent.DbCleanup -> {
                viewModelScope.launch {
                    Cleanup(prefs, database = get()).perform(System.currentTimeMillis())
                }
            }

            is SettingsViewEvent.SetWideLayout -> {
                viewState = viewState.copy(wideLayout = event.wideLayout)
            }
            is SettingsViewEvent.GDriveActivityResult -> onGDriveActivityResult(event.activityResult)
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
            viewModelScope.launch {
                try {
                    gDriveSignIn.signIn()
                    onGDriveLoginResult(true, -1)
                } catch (e: Throwable) {
                    when (e) {
                        is GDriveSignIn.GoogleSignInRequestException -> emitAction(SettingsViewAction.GDriveErrorIntent(e.intent))
                        is GDriveSignIn.GoogleSignInFailedException -> onGDriveLoginResult(false, e.resultCode)
                        else -> onGDriveLoginResult(false, 0)
                    }
                }
            }
        } else {
            prefs.isDriveSyncEnabled = false
            viewState = viewState.copy(
                isProgressVisible = false,
                items = preferenceItems(prefs, inProgress = false, playServices, application)
            )
            viewModelScope.launch {
                gDriveSignIn.signOut()
            }
        }
    }

    private fun onGDriveActivityResult(activityResult: ActivityResult): Unit {
        viewModelScope.launch {
            try {
                gDriveSignIn.onActivityResult(activityResult.resultCode, activityResult.data)
                onGDriveLoginResult(true, -1)
            } catch (e: Throwable) {
                when (e) {
                    is GDriveSignIn.GoogleSignInFailedException -> onGDriveLoginResult(false, e.resultCode)
                    else -> onGDriveLoginResult(false, 0)
                }
            }
        }
    }

    private fun gDriveSyncNow() {
        val googleAccount = GDriveSignIn.getLastSignedInAccount(context)
        if (googleAccount != null) {
            appScope.launch {
                try {
                    viewState = viewState.copy(isProgressVisible = true)
                    emitAction(showToastAction(resId = R.string.sync_start))
                    val gDriveSync = get<GDriveSync> { parametersOf(googleAccount) }
                    gDriveSync.doSync()
                    prefs.lastDriveSyncTime = System.currentTimeMillis()
                    viewState = viewState.copy(
                        isProgressVisible = false,
                        items = preferenceItems(prefs, inProgress = false, playServices, application)
                    )
                    emitAction(showToastAction(resId = R.string.sync_finish))
                } catch (e: Exception) {
                    AppLog.e(e)
                    viewState = viewState.copy(isProgressVisible = false)
                    emitAction(showToastAction(resId = R.string.sync_error))
                    if (e is GDriveSync.SyncError && e.error?.intent != null) {
                        emitAction(SettingsViewAction.GDriveErrorIntent(e.error.intent!!))
                    }
                }
            }
        } else {
            viewState = viewState.copy(isProgressVisible = false)
            emitAction(showToastAction(resId = R.string.no_gdrive_account))
        }
    }

    private fun onGDriveLoginResult(isSuccess: Boolean, errorCode: Int) {
        if (isSuccess) {
            prefs.isDriveSyncEnabled = true
            val observer = get<UploadServiceContentObserver>()
            val invalidationTracker = get<AppsDatabase>().invalidationTracker
            invalidationTracker.removeObserver(observer)
            invalidationTracker.addObserver(observer)
            emitAction(showToastAction(resId = R.string.gdrive_connected))
            viewState = viewState.copy(
                isProgressVisible = false,
                items = preferenceItems(prefs, inProgress = false, playServices, application)
            )
        } else {
            prefs.isDriveSyncEnabled = false
            emitAction(showToastAction(text = "Drive login error $errorCode"))
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