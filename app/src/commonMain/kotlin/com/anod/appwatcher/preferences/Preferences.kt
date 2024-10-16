package com.anod.appwatcher.preferences

import kotlinx.coroutines.flow.Flow

data class PersistedAccount(val name: String, val type: String)

interface Preferences {
    val changes: Flow<String>
    var account: PersistedAccount?
    var notificationDisabledToastCount: Int
    var lastUpdateTime: Long
    var isWifiOnly: Boolean
    var isLastUpdatesViewed: Boolean
    var isDriveSyncEnabled: Boolean
    var lastDriveSyncTime: Long
    var lastCleanupTime: Long
    val areNotificationsEnabled: Boolean
    val useAutoSync: Boolean
    var updatesFrequency: Int
    var isRequiresCharging: Boolean
    var sortIndex: Int
    var versionCode: Int
    var isNotifyInstalledUpToDate: Boolean
    var isNotifyInstalled: Boolean
    var isNotifyNoChanges: Boolean
    var showRecent: Boolean
    var showOnDevice: Boolean
    var showRecentlyDiscovered: Boolean
    var uiMode: Int
    val appCompatNightMode: Int
    var theme: Int
    var themeIndex: Int
    var defaultMainFilterId: Int
    var enablePullToRefresh: Boolean
    var collectCrashReports: Boolean
    var iconShape: String
    val defaultSystemMask: String

    companion object {
        const val recentDays: Long = 2
        
        const val SORT_NAME_ASC = 0
        const val SORT_NAME_DESC = 1
        const val SORT_DATE_ASC = 2
        const val SORT_DATE_DESC = 3

        const val THEME_DEFAULT = 0
        const val THEME_BLACK = 1
    }
}
