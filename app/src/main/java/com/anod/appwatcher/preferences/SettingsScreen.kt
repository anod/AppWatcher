package com.anod.appwatcher.preferences

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anod.appwatcher.R
import com.anod.appwatcher.backup.DbBackupManager
import com.anod.appwatcher.compose.AppTheme
import info.anodsplace.applog.AppLog
import info.anodsplace.compose.IconShapeSelector
import info.anodsplace.compose.Preference
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.PreferencesScreen
import info.anodsplace.compose.key
import info.anodsplace.framework.app.NotificationManager
import info.anodsplace.framework.content.CreateDocument
import org.koin.java.KoinJavaComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(screenState: SettingsViewState, onEvent: (SettingsViewEvent) -> Unit, prefs: Preferences = KoinJavaComponent.getKoin().get()) {

    val exportDocumentRequest = rememberLauncherForActivityResult(contract = CreateDocument("application/json")) { uri ->
        if (uri == null) {
            AppLog.d("Create document cancelled")
        } else {
            onEvent(SettingsViewEvent.Export(uri))
        }
    }

    val importDocumentRequest = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            AppLog.d("Open document cancelled")
        } else {
            onEvent(SettingsViewEvent.Import(uri))
        }
    }

    Surface {
        Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                            title = { Text(text = stringResource(id = R.string.navdrawer_item_settings)) },
                            navigationIcon = {
                                IconButton(onClick = { onEvent(SettingsViewEvent.OnBackNav) }) {
                                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                                }
                            },
                            actions = {
                                if (screenState.isProgressVisible) {
                                    CircularProgressIndicator(
                                            modifier = Modifier
                                                    .size(32.dp),
                                            color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            },
                    )
                },
        ) { contentPadding ->
            PreferencesScreen(
                    modifier = Modifier.padding(contentPadding),
                    preferences = screenState.items,
                    placeholder = { item, _ ->
                        when (item.key) {
                            "icon-style" -> Preference(
                                    item,
                                    secondary = {
                                        IconShapeSelector(
                                                pathMasks = stringArrayResource(id = R.array.adaptive_icon_style_paths_values),
                                                names = stringArrayResource(id = R.array.adaptive_icon_style_names),
                                                selected = prefs.iconShape,
                                                defaultSystemMask = prefs.defaultSystemMask,
                                                systemMaskName = stringResource(id = R.string.system),
                                                modifier = Modifier
                                                    .padding(top = 8.dp)
                                                    .fillMaxWidth(),
                                                onPathChange = { newPath -> onEvent(SettingsViewEvent.UpdateIconsShape(newPath)) }
                                        )
                                    },
                                    onClick = { })
                            "update_frequency" -> {
                                if (!screenState.areNotificationsEnabled) {
                                    Column(modifier = Modifier
                                            .padding(top = 16.dp)
                                            .border(width = 1.dp, color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                                text = stringResource(R.string.notifications_not_enabled),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodyLarge
                                        )
                                        Button(
                                                modifier = Modifier.padding(all = 4.dp),
                                                onClick = { onEvent(SettingsViewEvent.NotificationPermissionRequest) }
                                        ) {
                                            Text(text = stringResource(R.string.allow))
                                        }
                                    }

                                }
                            }
                            else -> {}
                        }
                    },
                    onClick = { item ->
                        when (item.key) {
                            "export" -> exportDocumentRequest.launch(
                                    CreateDocument.Args(
                                            "appwatcher-" + DbBackupManager.generateFileName(),
                                            Uri.parse(DbBackupManager.defaultBackupDir.absolutePath),
                                    )
                            )
                            "import" -> importDocumentRequest.launch(arrayOf("application/json", "text/plain", "*/*"))
                            "licenses" -> onEvent(SettingsViewEvent.OssLicenses)
                            "user-log" -> onEvent(SettingsViewEvent.OpenUserLog)
                            "refresh-history" -> onEvent(SettingsViewEvent.OpenRefreshHistory)
                            else -> onSettingsItemClick(prefs, item, onEvent)
                        }
                    }
            )
        }
    }
}

fun onSettingsItemClick(prefs: Preferences, item: PreferenceItem, onEvent: (SettingsViewEvent) -> Unit) {
    when (item.key) {
        "drive_sync" -> onEvent(SettingsViewEvent.GDriveSyncToggle((item as PreferenceItem.Switch).checked))
        "drive-sync-now" -> onEvent(SettingsViewEvent.GDriveSyncNow)
        "update_frequency" -> onEvent(SettingsViewEvent.ChangeUpdatePolicy(
                frequency = (item as PreferenceItem.Pick).value.toInt(),
                isWifiOnly = prefs.isWifiOnly,
                isRequiresCharging = prefs.isRequiresCharging
        ))
        "wifi_only" -> {
            val useWifiOnly = (item as PreferenceItem.Switch).checked
            onEvent(SettingsViewEvent.ChangeUpdatePolicy(prefs.updatesFrequency, useWifiOnly, prefs.isRequiresCharging))
        }
        "requires-charging" -> {
            val requiresCharging = (item as PreferenceItem.Switch).checked
            onEvent(SettingsViewEvent.ChangeUpdatePolicy(prefs.updatesFrequency, prefs.isWifiOnly, requiresCharging))
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
            onEvent(SettingsViewEvent.UpdateCrashReports((item as PreferenceItem.Switch).checked))
        }
        "pull-to-refresh" -> {
            onEvent(SettingsViewEvent.SetRecreateFlag(item, prefs.enablePullToRefresh) { prefs.enablePullToRefresh = it })

        }
        "show-recent" -> {
            onEvent(SettingsViewEvent.SetRecreateFlag(item, prefs.showRecent) { prefs.showRecent = it })
        }
        "show-on-device" -> {
            onEvent(SettingsViewEvent.SetRecreateFlag(item, prefs.showOnDevice) { prefs.showOnDevice = it })
        }
        "show-recently-updated" -> {
            onEvent(SettingsViewEvent.SetRecreateFlag(item, prefs.showRecentlyUpdated) { prefs.showRecentlyUpdated = it })
        }
        "default-filter" -> {
            prefs.defaultMainFilterId = (item as PreferenceItem.Pick).value.toInt()
        }
        "icon-style" -> {
            onEvent(SettingsViewEvent.UpdateIconsShape((item as PreferenceItem.Pick).value))
        }
        "theme" -> {
            onEvent(SettingsViewEvent.UpdateTheme((item as PreferenceItem.Pick).value.toInt()))
        }
        "test-notification" -> {
            onEvent(SettingsViewEvent.TestNotification)
        }
        "db-cleanup" -> {
            onEvent(SettingsViewEvent.DbCleanup)
        }
    }
}

@Preview(showSystemUi = true, locale = "ru")
@Composable
fun PreferencesScreenPreview() {
    val scope = rememberCoroutineScope()
    val items = listOf(
            PreferenceItem.Category(titleRes = R.string.category_updates),
            PreferenceItem.Pick(
                    entriesRes = R.array.updates_frequency,
                    entryValuesRes = R.array.updates_frequency_values,
                    value = "3600",
                    titleRes = R.string.pref_title_updates_frequency,
                    summary = "Every 3600 minutes",
                    key = "update_frequency",
                    enabled = false
            ),
            PreferenceItem.Switch(
                    checked = true,
                    enabled = true,
                    titleRes = R.string.menu_wifi_only,
                    key = "wifi_only"
            ),
            PreferenceItem.Switch(
                    checked = true,
                    enabled = false,
                    titleRes = R.string.menu_requires_charging,
                    key = "requires-charging"
            ),

            PreferenceItem.Category(titleRes = R.string.pref_header_drive_sync),
            PreferenceItem.Switch(
                    checked = false,
                    enabled = true,
                    titleRes = R.string.pref_title_drive_sync_enabled,
                    summaryRes = R.string.pref_descr_drive_sync_enabled,
                    summary = "",
                    key = "drive_sync"
            ),
            PreferenceItem.Text(
                    enabled = false,
                    titleRes = R.string.pref_title_drive_sync_now,
                    summary = "",
                    key = "drive-sync-now"
            ),

            PreferenceItem.Category(titleRes = R.string.pref_header_backup),
            PreferenceItem.Text(
                    titleRes = R.string.pref_title_export,
                    summaryRes = R.string.pref_descr_export,
                    key = "export"
            ),
            PreferenceItem.Text(
                    titleRes = R.string.pref_title_import,
                    summaryRes = R.string.pref_descr_import,
                    key = "import"
            ),

            PreferenceItem.Category(titleRes = R.string.pref_header_interface),
            PreferenceItem.List(
                    entries = R.array.themes,
                    entryValues = 0,
                    value = "0",
                    titleRes = R.string.pref_title_theme,
                    summaryRes = R.string.pref_descr_theme,
                    key = "theme"
            ),
            PreferenceItem.Switch(
                    checked = false,
                    titleRes = R.string.pref_show_recent_title,
                    summaryRes = R.string.pref_show_recent_descr,
                    key = "show-recent"
            ),
            PreferenceItem.Switch(
                    checked = true,
                    titleRes = R.string.pref_show_ondevice_title,
                    summaryRes = R.string.pref_show_ondevice_descr,
                    key = "show-on-device"
            ),
            PreferenceItem.List(
                    entries = R.array.filter_titles,
                    entryValues = 0,
                    value = "1",
                    titleRes = R.string.pref_default_filter,
                    summaryRes = R.string.pref_default_filter_summary,
                    key = "default-filter"
            ),
            PreferenceItem.Switch(
                    checked = false,
                    titleRes = R.string.pref_pull_to_refresh,
                    key = "pull-to-refresh"
            ),
            PreferenceItem.Placeholder(
                    titleRes = R.string.adaptive_icon_style,
                    summaryRes = R.string.adaptive_icon_style_summary,
                    key = "icon-style"
            ),

            PreferenceItem.Category(titleRes = R.string.pref_privacy),
            PreferenceItem.Switch(
                    checked = false,
                    titleRes = R.string.crash_reports_title,
                    summaryRes = R.string.crash_reports_descr,
                    key = "crash-reports"
            ),

            PreferenceItem.Category(titleRes = R.string.pref_header_about),
            PreferenceItem.Text(
                    titleRes = R.string.pref_title_about,
                    summary = "12345566",
                    key = "about"
            ),
            PreferenceItem.Text(
                    titleRes = R.string.pref_title_opensource,
                    summaryRes = R.string.pref_descr_opensource,
                    key = "licenses"
            ),
            PreferenceItem.Text(
                    titleRes = R.string.user_log,
                    key = "user-log"
            )
    )
    val context = LocalContext.current
    AppTheme {
        Surface {
            SettingsScreen(
                    screenState = SettingsViewState(
                            items = items,
                            areNotificationsEnabled = false,
                            isProgressVisible = true
                    ),
                    onEvent = { },
                    prefs = Preferences(context, NotificationManager.NoOp(), scope)
            )
        }
    }
}