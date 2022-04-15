package com.anod.appwatcher.preferences

import android.content.Context
import android.text.format.DateUtils
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.R
import info.anodsplace.compose.PreferenceItem
import info.anodsplace.framework.playservices.GooglePlayServices
import java.util.*

private val appVersion: String
    get() = "%s (%d)".format(Locale.US, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

private fun renderDriveSyncTime(prefs: Preferences, context: Context): String {
    val time = prefs.lastDriveSyncTime
    return if (time == (-1).toLong()) {
        context.getString(R.string.pref_descr_drive_sync_now, context.getString(R.string.never))
    } else {
        context.getString(
            R.string.pref_descr_drive_sync_now,
            DateUtils.getRelativeDateTimeString(context, time, 0, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL)
        )
    }
}

fun preferenceItems(prefs: Preferences, inProgress: Boolean, playServices: GooglePlayServices, context: Context): List<PreferenceItem> {
    val hasPlayServices = playServices.isSupported
    val useAutoSync = prefs.useAutoSync

    return mutableListOf(
        PreferenceItem.Category(titleRes = R.string.category_updates),
        PreferenceItem.Pick(
            entriesRes = R.array.updates_frequency,
            entryValuesRes = R.array.updates_frequency_values,
            value = prefs.updatesFrequency.toString(),
            titleRes = R.string.pref_title_updates_frequency,
            key = "update_frequency"
        ),
        PreferenceItem.Switch(
            checked = prefs.isWifiOnly,
            enabled = useAutoSync,
            titleRes = R.string.menu_wifi_only,
            key = "wifi_only"
        ),
        PreferenceItem.Switch(
            checked = prefs.isRequiresCharging,
            enabled = useAutoSync,
            titleRes = R.string.menu_requires_charging,
            key = "requires-charging"
        ),
        PreferenceItem.Text(
                titleRes = R.string.refresh_history,
                key = "refresh-history"
        ),
        PreferenceItem.Category(titleRes = R.string.settings_notifications),
        PreferenceItem.Switch(
            checked = prefs.isNotifyInstalledUpToDate,
            titleRes = R.string.uptodate_title,
            summaryRes = R.string.uptodate_summary,
            key = Preferences.NOTIFY_INSTALLED_UPTODATE
        ),
        PreferenceItem.Switch(
            checked = prefs.isNotifyInstalled,
            titleRes = R.string.pref_notify_installed,
            summaryRes = R.string.pref_notify_installed_summary,
            key = "notify-installed"
        ),
        PreferenceItem.Switch(
            checked = prefs.isNotifyNoChanges,
            titleRes = R.string.pref_notify_no_changes,
            summaryRes = R.string.pref_notify_no_changes_summary,
            key = "notify-no-changes"
        ),

        PreferenceItem.Category(titleRes = R.string.pref_header_drive_sync),
        PreferenceItem.Switch(
            checked = if (hasPlayServices) prefs.isDriveSyncEnabled else false,
            enabled = hasPlayServices,
            titleRes = R.string.pref_title_drive_sync_enabled,
            summaryRes = if (hasPlayServices) R.string.pref_descr_drive_sync_enabled else 0,
            summary = if (hasPlayServices) "" else playServices.availabilityMessage,
            key = "drive_sync"
        ),
        PreferenceItem.Text(
            enabled = if (hasPlayServices && !inProgress) prefs.isDriveSyncEnabled else false,
            titleRes = R.string.pref_title_drive_sync_now,
            summary = if (hasPlayServices) renderDriveSyncTime(prefs, context) else "",
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
        PreferenceItem.Pick(
            entriesRes = R.array.themes,
            entryValuesRes = 0,
            value = prefs.themeIndex.toString(),
            titleRes = R.string.pref_title_theme,
            key = "theme"
        ),
        PreferenceItem.Switch(
            checked = prefs.showRecent,
            titleRes = R.string.pref_show_recent_title,
            summaryRes = R.string.pref_show_recent_descr,
            key = "show-recent"
        ),
        PreferenceItem.Switch(
            checked = prefs.showOnDevice,
            titleRes = R.string.pref_show_ondevice_title,
            summaryRes = R.string.pref_show_ondevice_descr,
            key = "show-on-device"
        ),
        PreferenceItem.Switch(
            checked = prefs.showRecentlyUpdated,
            titleRes = R.string.pref_show_recently_updated_title,
            summaryRes = R.string.pref_show_recently_updated_descr,
            key = "show-recently-updated"
        ),
        PreferenceItem.Pick(
            entriesRes = R.array.filter_titles,
            entryValuesRes = 0,
            value = prefs.defaultMainFilterId.toString(),
            titleRes = R.string.pref_default_filter,
            summaryRes = R.string.pref_default_filter_summary,
            key = "default-filter"
        ),
        PreferenceItem.Switch(
            checked = prefs.enablePullToRefresh,
            titleRes = R.string.pref_pull_to_refresh,
            key = "pull-to-refresh"
        ),
        PreferenceItem.Pick(
            entriesRes = R.array.adaptive_icon_style_names,
            entryValuesRes = R.array.adaptive_icon_style_paths_values,
            value = prefs.iconShape,
            titleRes = R.string.adaptive_icon_style,
            summaryRes = R.string.adaptive_icon_style_summary,
            key = "icon-style"
        ),

        PreferenceItem.Category(titleRes = R.string.pref_privacy),
        PreferenceItem.Switch(
            checked = prefs.collectCrashReports,
            titleRes = R.string.crash_reports_title,
            summaryRes = R.string.crash_reports_descr,
            key = "crash-reports"
        ),

        PreferenceItem.Category(titleRes = R.string.pref_header_about),
        PreferenceItem.Text(
            titleRes = R.string.pref_title_about,
            summary = appVersion,
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
}