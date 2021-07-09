package com.anod.appwatcher.preferences

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anod.appwatcher.R
import com.anod.appwatcher.backup.DbBackupManager
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.UiAction
import info.anodsplace.applog.AppLog
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.PreferencesScreen
import info.anodsplace.framework.content.CreateDocument
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val prefs = viewModel.preferences
    val progress by viewModel.isProgressVisible.collectAsState()
    val items by viewModel.items.collectAsState()
    val exportDocumentRequest = rememberLauncherForActivityResult(contract = CreateDocument()) { uri ->
        if (uri == null) {
            AppLog.d("Create document cancelled")
        } else {
            coroutineScope.launch { viewModel.actions.emit(UiAction.Export(uri)) }
        }
    }

    val importDocumentRequest = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            AppLog.d("Open document cancelled")
        } else {
            coroutineScope.launch { viewModel.actions.emit(UiAction.Import(uri)) }
        }
    }

    AppTheme(
        darkTheme = when (prefs.nightMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> false
            AppCompatDelegate.MODE_NIGHT_YES -> true
            // MODE_NIGHT_AUTO
            else -> isSystemInDarkTheme()
        },
        theme = prefs.theme
    ) {
        Surface {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = stringResource(id = R.string.navdrawer_item_settings)) },
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { viewModel.actions.emit(UiAction.OnBackNav) } }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                            }
                        },
                        actions = {
                            if (progress) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(32.dp),
                                    color = MaterialTheme.colors.secondaryVariant
                                )
                            }
                        },
                        elevation = 0.dp
                    )
                }
            ) {
                PreferencesScreen(
                    preferences = items,
                    onClick = { item ->
                        when (item.key) {
                            "export" -> {
                                exportDocumentRequest.launch(
                                    CreateDocument.Args(
                                        "application/json",
                                        "appwatcher-" + DbBackupManager.generateFileName(),
                                        Uri.parse(DbBackupManager.defaultBackupDir.absolutePath),
                                    )
                                )
                            }
                            "import" -> importDocumentRequest.launch(arrayOf("application/json", "text/plain", "*/*"))
                            "licenses" -> coroutineScope.launch { viewModel.actions.emit(UiAction.OssLicenses) }
                            "user-log" -> coroutineScope.launch { viewModel.actions.emit(UiAction.OpenUserLog) }
                            "refresh-history" -> coroutineScope.launch { viewModel.actions.emit(UiAction.OpenRefreshHistory) }
                            else -> onSettingsItemClick(prefs, item, viewModel)
                        }
                    }
                )
            }
        }
    }
}

fun onSettingsItemClick(prefs: Preferences, item: PreferenceItem, viewModel: SettingsViewModel) {
    when (item.key) {
        "drive_sync" -> viewModel.gDriveSyncToggle((item as PreferenceItem.Switch).checked)
        "drive-sync-now" -> viewModel.gDriveSyncNow()
        "update_frequency" -> viewModel.changeUpdatePolicy(
            (item as PreferenceItem.List).value.toInt(),
            prefs.isWifiOnly,
            prefs.isRequiresCharging
        )
        "wifi_only" -> {
            val useWifiOnly = (item as PreferenceItem.Switch).checked
            viewModel.changeUpdatePolicy(prefs.updatesFrequency, useWifiOnly, prefs.isRequiresCharging)
        }
        "requires-charging" -> {
            val requiresCharging = (item as PreferenceItem.Switch).checked
            viewModel.changeUpdatePolicy(prefs.updatesFrequency, prefs.isWifiOnly, requiresCharging)
        }
        Preferences.NOTIFY_INSTALLED_UPTODATE -> {
            prefs.isNotifyInstalledUpToDate = (item as PreferenceItem.Switch).checked
        }
        "notify-installed" -> {
            prefs.isNotifyInstalled = (item as PreferenceItem.Switch).checked
        }
        "notify-no-changes" -> {
            prefs.isNotifyNoChanges = (item as PreferenceItem.Switch).checked
        }
        "crash-reports" -> {
            viewModel.updateCrashReports((item as PreferenceItem.Switch).checked)
        }
        "pull-to-refresh" -> {
            prefs.enablePullToRefresh = viewModel.setRecreateFlag(item, prefs.enablePullToRefresh)
        }
        "show-recent" -> {
            prefs.showRecent = viewModel.setRecreateFlag(item, prefs.showRecent)
        }
        "show-on-device" -> {
            prefs.showOnDevice = viewModel.setRecreateFlag(item, prefs.showOnDevice)
        }
        "show-recently-updated" -> {
            prefs.showRecentlyUpdated = viewModel.setRecreateFlag(item, prefs.showRecentlyUpdated)
        }
        "default-filter" -> {
            prefs.defaultMainFilterId = (item as PreferenceItem.List).value.toInt()
        }
        "icon-style" -> {
            viewModel.updateIconsShape((item as PreferenceItem.List).value)
        }
        "theme" -> {
            viewModel.updateTheme((item as PreferenceItem.List).value.toInt())
        }
    }
}
