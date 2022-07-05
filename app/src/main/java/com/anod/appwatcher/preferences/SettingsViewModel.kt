package com.anod.appwatcher.preferences

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Operation
import com.anod.appwatcher.R
import com.anod.appwatcher.backup.ExportBackupTask
import com.anod.appwatcher.backup.ImportBackupTask
import com.anod.appwatcher.backup.gdrive.GDriveSync
import com.anod.appwatcher.backup.gdrive.UploadServiceContentObserver
import com.anod.appwatcher.compose.UiAction
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.sync.SyncNotification
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.sync.UpdatedApp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import info.anodsplace.applog.AppLog
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.playservices.GooglePlayServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class SettingsViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val playServices: GooglePlayServices = GooglePlayServices(application)
    private val appScope: CoroutineScope by inject()
    private val context: Context by inject()
    val prefs: Preferences by inject()

    val actions: MutableSharedFlow<UiAction> = MutableSharedFlow()
    val isProgressVisible = MutableStateFlow(false)
    val reload = MutableSharedFlow<Boolean>()
    var recreateWatchlistOnBack: Boolean = false

    val items = combine(isProgressVisible, reload.onStart { emit(true) }) { (inProgress, _) ->
        preferenceItems(prefs, inProgress, playServices, application)
    }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = emptyList())

    fun import(srcUri: Uri) = flow {
        emit(-1)
        val task = get<ImportBackupTask>()
        emit(task.execute(srcUri))
    }

    fun export(dstUri: Uri) = flow {
        emit(-1)
        val task = get<ExportBackupTask>()
        emit(task.execute(dstUri))
    }

    fun gDriveSyncToggle(checked: Boolean) {
        if (checked) {
            prefs.isDriveSyncEnabled = true
            isProgressVisible.value = true
            viewModelScope.launch {
                actions.emit(UiAction.GDriveSignIn)
                reload.emit(true)
            }
        } else {
            prefs.isDriveSyncEnabled = false
            viewModelScope.launch {
                actions.emit(UiAction.GDriveSignOut)
                reload.emit(true)
            }
        }
    }

    fun gDriveSyncNow() {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (googleAccount != null) {
            appScope.launch {
                try {
                    isProgressVisible.value = true
                    actions.emit(UiAction.ShowToast(resId = R.string.sync_start))
                    val gDriveSync = get<GDriveSync> { parametersOf(googleAccount) }
                    gDriveSync.doSync()
                    prefs.lastDriveSyncTime = System.currentTimeMillis()
                    isProgressVisible.value = false
                    actions.emit(UiAction.ShowToast(resId = R.string.sync_finish))
                } catch (e: Exception) {
                    AppLog.e(e)
                    isProgressVisible.value = false
                    actions.emit(UiAction.ShowToast(resId = R.string.sync_error))
                    if (e is GDriveSync.SyncError && e.error?.intent != null) {
                        viewModelScope.launch {
                            actions.emit(UiAction.GDriveErrorIntent(e.error.intent!!))
                        }
                    }
                }
            }
        } else {
            isProgressVisible.value = false
            viewModelScope.launch {
                actions.emit(UiAction.ShowToast(resId = R.string.no_gdrive_account))
            }
        }
    }

    fun onGDriveLoginResult(isSuccess: Boolean, errorCode: Int) {
        if (isSuccess) {
            prefs.isDriveSyncEnabled = true
            val observer = get<UploadServiceContentObserver>()
            val invalidationTracker = get<AppsDatabase>().invalidationTracker
            invalidationTracker.removeObserver(observer)
            invalidationTracker.addObserver(observer)
            viewModelScope.launch {
                actions.emit(UiAction.ShowToast(resId = R.string.gdrive_connected))
            }
        } else {
            prefs.isDriveSyncEnabled = false
            viewModelScope.launch {
                reload.emit(true)
                actions.emit(UiAction.ShowToast(text = "Drive login error $errorCode"))
            }
        }
        isProgressVisible.value = false
    }

    fun changeUpdatePolicy(frequency: Int, isWifiOnly: Boolean, isRequiresCharging: Boolean) {
        prefs.updatesFrequency = frequency
        prefs.isWifiOnly = isWifiOnly
        prefs.isRequiresCharging = isRequiresCharging

        val useAutoSync = prefs.useAutoSync

        appScope.launch {
            reload.emit(true)
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

    fun setRecreateFlag(item: PreferenceItem, oldValue: Boolean): Boolean {
        val newValue = (item as PreferenceItem.Switch).checked
        if (!recreateWatchlistOnBack) {
            recreateWatchlistOnBack = oldValue != newValue
        }
        return newValue
    }

    fun updateTheme(newThemeIndex: Int) {
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
            recreateWatchlistOnBack = true
            viewModelScope.launch {
                actions.emit(UiAction.Recreate)
            }
        }
    }

    fun updateIconsShape(newIconShape: String) {
        if (prefs.iconShape == newIconShape) {
            return
        }
        prefs.iconShape = newIconShape
        recreateWatchlistOnBack = true
    }

    fun updateCrashReports(checked: Boolean) {
        prefs.collectCrashReports = checked
        viewModelScope.launch {
            actions.emit(UiAction.Recreate)
        }
    }

    fun testNotification() {
        SyncNotification(ApplicationContext(context), get()).show(listOf(
                UpdatedApp(
                        packageName = "com.anod.appwatcher",
                        title = "Test",
                        installedVersionCode = 0,
                        isNewUpdate = true,
                        recentChanges = "Test notification",
                        uploadDate = "Now",
                        uploadTime = System.currentTimeMillis(),
                        versionNumber = 1,
                )
        ))
    }
}