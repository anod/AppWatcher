package com.anod.appwatcher.preferences

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.R
import com.anod.appwatcher.backup.ExportTask
import com.anod.appwatcher.backup.ImportTask
import com.anod.appwatcher.backup.gdrive.GDriveSync
import com.anod.appwatcher.compose.UiAction
import com.anod.appwatcher.sync.SyncScheduler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import info.anodsplace.applog.AppLog
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.playservices.GooglePlayServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val playServices: GooglePlayServices = GooglePlayServices(application)
    private val appScope: CoroutineScope = com.anod.appwatcher.Application.provide(application).appScope
    private val context: Context
        get() = getApplication<AppWatcherApplication>()

    val preferences: Preferences = com.anod.appwatcher.Application.provide(application).prefs
    val actions: MutableSharedFlow<UiAction> = MutableSharedFlow()
    val isProgressVisible = MutableStateFlow(false)
    val reload = MutableSharedFlow<Boolean>()
    var recreateWatchlistOnBack: Boolean = false

    val items = combine(isProgressVisible, reload.onStart { emit(true) }) { (inProgress, _) ->
        preferenceItems(preferences, inProgress, playServices, application)
    }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = emptyList())

    fun import(srcUri: Uri) = flow {
        emit(-1)
        val task = ImportTask(ApplicationContext(context))
        emit(task.execute(srcUri))
    }

    fun export(dstUri: Uri) = flow {
        emit(-1)
        val task = ExportTask(ApplicationContext(context))
        emit(task.execute(dstUri))
    }

    fun gDriveSyncToggle(checked: Boolean) {
        if (checked) {
            preferences.isDriveSyncEnabled = true
            isProgressVisible.value = true
            viewModelScope.launch {
                actions.emit(UiAction.GDriveSignIn)
                reload.emit(true)
            }
        } else {
            preferences.isDriveSyncEnabled = false
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
                    GDriveSync(context, googleAccount).doSync()
                    preferences.lastDriveSyncTime = System.currentTimeMillis()
                    isProgressVisible.value = false
                    actions.emit(UiAction.ShowToast(resId = R.string.sync_finish))
                } catch (e: Exception) {
                    AppLog.e(e)
                    isProgressVisible.value = false
                    actions.emit(UiAction.ShowToast(resId = R.string.sync_error))
                    if (e is GDriveSync.SyncError && e.error?.intent != null) {
                        viewModelScope.launch {
                            actions.emit(UiAction.GDriveErrorIntent(e.error.intent))
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
            preferences.isDriveSyncEnabled = true
            com.anod.appwatcher.Application.provide(context).uploadServiceContentObserver
            viewModelScope.launch {
                actions.emit(UiAction.ShowToast(resId = R.string.gdrive_connected))
            }
        } else {
            viewModelScope.launch {
                actions.emit(UiAction.ShowToast(text = "Drive login error $errorCode"))
            }
        }
        isProgressVisible.value = false
    }

    fun changeUpdatePolicy(frequency: Int, isWifiOnly: Boolean, isRequiresCharging: Boolean) {
        preferences.updatesFrequency = frequency
        preferences.isWifiOnly = isWifiOnly
        preferences.isRequiresCharging = isRequiresCharging

        val useAutoSync = preferences.useAutoSync

        appScope.launch {
            if (useAutoSync) {
                SyncScheduler(context).schedule(
                    preferences.isRequiresCharging,
                    preferences.isWifiOnly,
                    preferences.updatesFrequency.toLong(),
                    true
                ).collect { }
            } else {
                SyncScheduler(context).cancel().collect { }
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
        if (preferences.themeIndex == newThemeIndex) {
            return
        }
        val nightMode = preferences.nightMode
        val theme = preferences.theme
        preferences.themeIndex = newThemeIndex
        var recreate = false
        if (preferences.theme != theme) {
            recreate = true
        }
        if (preferences.nightMode != nightMode) {
            recreate = true
        }
        if (recreate) {
            AppCompatDelegate.setDefaultNightMode(preferences.nightMode)
            recreateWatchlistOnBack = true
            viewModelScope.launch {
                actions.emit(UiAction.Recreate)
            }
        }
    }

    fun updateIconsShape(iconShapes: String) {
        if (preferences.iconShape == iconShapes) {
            return
        }
        preferences.iconShape = iconShapes
        com.anod.appwatcher.Application.provide(context).iconLoader.setIconShape(iconShapes)
        recreateWatchlistOnBack = true
        viewModelScope.launch {
            actions.emit(UiAction.Recreate)
        }
    }

    fun updateCrashReports(checked: Boolean) {
        preferences.collectCrashReports = checked
        viewModelScope.launch {
            actions.emit(UiAction.Recreate)
        }
    }
}
