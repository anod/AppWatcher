package com.anod.appwatcher.preferences

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.R
import com.anod.appwatcher.backup.ExportTask
import com.anod.appwatcher.backup.ImportTask
import com.anod.appwatcher.backup.gdrive.GDrive
import com.anod.appwatcher.compose.UiAction
import com.anod.appwatcher.sync.SyncScheduler
import com.google.android.gms.auth.api.signin.GoogleSignIn
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
        get() = getApplication()

    val preferences: Preferences = com.anod.appwatcher.Application.provide(application).prefs
    val actions: MutableSharedFlow<UiAction> = MutableSharedFlow()
    val isProgressVisible = MutableStateFlow(false)
    var recreateWatchlistOnBack: Boolean = false

    val items = isProgressVisible.map { inProgress ->
        preferenceItems(preferences, inProgress, playServices, application)
    }.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = emptyList())

    fun import(srcUri: Uri) = flow {
        emit(-1)
        val task = ImportTask(ApplicationContext(getApplication()))
        emit(task.execute(srcUri))
    }

    fun export(dstUri: Uri) = flow {
        emit(-1)
        val task = ExportTask(ApplicationContext(getApplication()))
        emit(task.execute(dstUri))
    }

    fun gDriveSyncToggle(checked: Boolean) {
        if (checked) {
            preferences.isDriveSyncEnabled = true
            isProgressVisible.value = true
            viewModelScope.launch {
                actions.emit(UiAction.GDriveSignIn)
            }
        } else {
            preferences.isDriveSyncEnabled = false
        }
    }

    fun gDriveSyncNow() {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(getApplication())
        if (googleAccount != null) {
            isProgressVisible.value = true
            Toast.makeText(context, R.string.sync_start, Toast.LENGTH_SHORT).show()
            appScope.launch {
                try {
                    GDrive(getApplication(), googleAccount).sync()
                    isProgressVisible.value = false
                    Toast.makeText(context, R.string.sync_finish, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    isProgressVisible.value = false
                    Toast.makeText(context, R.string.sync_error, Toast.LENGTH_SHORT).show()
                    if (e is GDrive.SyncError && e.error?.intent != null) {
                        viewModelScope.launch {
                            actions.emit(UiAction.GDriveErrorIntent(e.error.intent))
                        }
                    }
                }
            }
        } else {
            isProgressVisible.value = false
            Toast.makeText(context, R.string.no_gdrive_account, Toast.LENGTH_SHORT).show()
        }
    }

    fun onGDriveLoginResult(isSuccess: Boolean, errorCode: Int) {
        if (isSuccess) {
            preferences.isDriveSyncEnabled = true
            com.anod.appwatcher.Application.provide(context).uploadServiceContentObserver
            Toast.makeText(context, R.string.gdrive_connected, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Drive login error $errorCode", Toast.LENGTH_SHORT).show()
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
        preferences.iconShape = iconShapes
        com.anod.appwatcher.Application.provide(context).iconLoader.setIconShape(iconShapes)
        recreateWatchlistOnBack = true
        viewModelScope.launch {
            actions.emit(UiAction.Recreate)
        }
    }
}
