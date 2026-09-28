package com.anod.appwatcher.utils

import android.content.pm.PackageManager
import com.anod.appwatcher.database.AppListTable
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun PackageManager.isPackageEnabled(packageName: String): Boolean {
    val applicationInfo = try {
        getApplicationInfo(packageName, 0)
    } catch (_: PackageManager.NameNotFoundException) {
        return false
    }
    val enabledSetting = try {
        getApplicationEnabledSetting(packageName)
    } catch (_: IllegalArgumentException) {
        return false
    }
    return isPackageEnabled(applicationInfo.enabled, enabledSetting)
}

internal fun isPackageEnabled(manifestEnabled: Boolean, enabledSetting: Int): Boolean = when (enabledSetting) {
    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> manifestEnabled
    PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> false
    else -> manifestEnabled
}

internal suspend fun AppListTable.clearDisabledUpdateStatuses(
    installedApps: InstalledApps,
    packageEnabled: (String) -> Boolean
): Int = withContext(Dispatchers.IO) {
    val disabledRowIds = loadUpdatedPackages().mapNotNull { row ->
        val packageInfo = installedApps.packageInfo(row.packageName)
        row.rowId.takeIf {
            packageInfo.isInstalled && !packageEnabled(row.packageName)
        }
    }
    var cleared = 0
    for (rowIds in disabledRowIds.chunked(998)) {
        cleared += clearUpdateStatuses(rowIds)
    }
    cleared
}