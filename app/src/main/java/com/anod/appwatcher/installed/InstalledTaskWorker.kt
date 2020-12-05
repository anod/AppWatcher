package com.anod.appwatcher.installed

import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.AppTitleComparator
import info.anodsplace.framework.content.AppUpdateTimeComparator
import info.anodsplace.framework.content.InstalledPackageApp
import info.anodsplace.framework.content.getInstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class InstalledTaskWorker(
        private val context: ApplicationContext,
        private val sortId: Int,
        private val titleFilter: String) {

    suspend fun run(): List<InstalledPackageApp> = withContext(Dispatchers.Default) {
        val installedPackages = context.packageManager.getInstalledApps().toMutableList()

        when (sortId) {
            Preferences.SORT_NAME_DESC -> installedPackages.sortWith(AppTitleComparator(-1))
            Preferences.SORT_DATE_ASC -> installedPackages.sortWith(AppUpdateTimeComparator(1))
            Preferences.SORT_DATE_DESC -> installedPackages.sortWith(AppUpdateTimeComparator(-1))
            else -> installedPackages.sortWith(AppTitleComparator(1))
        }

        if (titleFilter.isNotEmpty()) {
            val filtered = ArrayList<InstalledPackageApp>(installedPackages.size)
            val locale = Locale.getDefault()
            val lcFilter = titleFilter.toLowerCase(locale)
            for (installedPackage in installedPackages) {
                if (installedPackage.title.toLowerCase(locale).contains(lcFilter)) {
                    filtered.add(installedPackage)
                }
            }
            filtered
        } else {
            installedPackages
        }
    }
}
