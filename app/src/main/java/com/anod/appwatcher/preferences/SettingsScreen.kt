package com.anod.appwatcher.preferences

import android.graphics.Matrix
import android.graphics.Path
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anod.appwatcher.R
import com.anod.appwatcher.backup.DbBackupManager
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.UiAction
import com.anod.appwatcher.utils.AdaptiveIconTransformation
import com.google.accompanist.flowlayout.FlowRow
import info.anodsplace.applog.AppLog
import info.anodsplace.compose.Preference
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.compose.PreferencesScreen
import info.anodsplace.framework.content.CreateDocument
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val prefs = viewModel.prefs
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
            darkTheme = when (prefs.uiMode) {
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
                        CenterAlignedTopAppBar(
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
                                                color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                },
                        )
                    }
            ) { contentPadding ->
                PreferencesScreen(
                        modifier = Modifier.padding(contentPadding),
                        preferences = items,
                        placeholder = { item, _ ->
                            when (item.key) {
                                "icon-style" -> Preference(
                                        item,
                                        secondaryText = {
                                            Column {
                                                Text(
                                                        modifier = Modifier.padding(top = 4.dp),
                                                        text = stringResource(id = R.string.adaptive_icon_style_summary),
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                                color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                )
                                                IconShapeSelector(
                                                        prefs = prefs,
                                                        modifier = Modifier
                                                                .padding(top = 8.dp)
                                                                .fillMaxWidth(),
                                                        onPathChange = { newPath -> viewModel.updateIconsShape(newPath) }
                                                )
                                            }
                                        },
                                        onClick = { })
                                else -> {}
                            }
                        },
                        onClick = { item ->
                            when (item.key) {
                                "export" -> exportDocumentRequest.launch(
                                        CreateDocument.Args(
                                                "application/json",
                                                "appwatcher-" + DbBackupManager.generateFileName(),
                                                Uri.parse(DbBackupManager.defaultBackupDir.absolutePath),
                                        )
                                )
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

@Composable
fun IconShapeSelector(prefs: Preferences, modifier: Modifier = Modifier, onPathChange: (String) -> Unit = {}) {
    val pathMasks = stringArrayResource(id = R.array.adaptive_icon_style_paths_values)
    val names = stringArrayResource(id = R.array.adaptive_icon_style_names)
    val iconSize = 48.dp
    val iconSizePx = with(LocalDensity.current) { iconSize.roundToPx() }
    var value by remember { mutableStateOf(prefs.iconShape) }

    FlowRow(
            modifier = modifier,
            mainAxisSpacing = 8.dp,
            crossAxisSpacing = 4.dp
    )
    {
        val isNone = value.isEmpty()
        Box(
                modifier = Modifier
                        .size(iconSize, iconSize)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(
                                alpha = if (isNone) 1.0f else 0.1f
                        )))
                        .clickable(onClick = {
                            value = ""
                            onPathChange("")
                        }),
                contentAlignment = Alignment.Center
        ) {
            Text(
                    text = names[0],
                    color = if (isNone) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
            )
        }

        pathMasks.filter { it.isNotEmpty() }.forEachIndexed { index, pathMask ->
            val path = AdaptiveIconTransformation.maskToPath(pathMask)
            val outline = Path()
            val maskMatrix = Matrix().apply {
                setScale(iconSizePx / AdaptiveIconTransformation.MASK_SIZE, iconSizePx / AdaptiveIconTransformation.MASK_SIZE)
            }
            path.transform(maskMatrix, outline)

            val selected = value == pathMask
            Box(
                    modifier = Modifier
                            .size(iconSize, iconSize)
                            .clip(GenericShape { _, _ ->
                                addPath(outline.asComposePath())
                            })
                            .clickable(onClick = {
                                value = pathMask
                                onPathChange(pathMask)
                            }, role = Role.Button, onClickLabel = names[index])
                            .background(color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            ) {
            }
        }
    }
}

fun onSettingsItemClick(prefs: Preferences, item: PreferenceItem, viewModel: SettingsViewModel) {
    when (item.key) {
        "drive_sync" -> viewModel.gDriveSyncToggle((item as PreferenceItem.Switch).checked)
        "drive-sync-now" -> viewModel.gDriveSyncNow()
        "update_frequency" -> viewModel.changeUpdatePolicy(
                frequency = (item as PreferenceItem.Pick).value.toInt(),
                isWifiOnly = prefs.isWifiOnly,
                isRequiresCharging = prefs.isRequiresCharging
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
            prefs.defaultMainFilterId = (item as PreferenceItem.Pick).value.toInt()
        }
        "icon-style" -> {
            viewModel.updateIconsShape((item as PreferenceItem.Pick).value)
        }
        "theme" -> {
            viewModel.updateTheme((item as PreferenceItem.Pick).value.toInt())
        }
        "test-notification" -> {
            viewModel.testNotification()
        }
    }
}


@Preview
@Composable
fun PreferencesScreenPreview() {
    AppTheme {
        Surface {
            PreferencesScreen(
                    preferences = listOf(
                            PreferenceItem.Category(titleRes = R.string.category_updates),
                            PreferenceItem.Pick(
                                    entriesRes = R.array.updates_frequency,
                                    entryValuesRes = R.array.updates_frequency_values,
                                    value = "3600",
                                    titleRes = R.string.pref_title_updates_frequency,
                                    summary = "Every 3600 minutes",
                                    key = "update_frequency"
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
                    ),
                    onClick = { }
            )
        }
    }
}