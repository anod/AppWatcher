package com.anod.appwatcher.installed

import android.util.SparseArray
import com.anod.appwatcher.Application
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.watchlist.AppsList
import com.anod.appwatcher.watchlist.LoadResult
import com.anod.appwatcher.watchlist.SectionHeader
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.AppTitleComparator
import info.anodsplace.framework.content.AppUpdateTimeComparator
import info.anodsplace.framework.content.InstalledPackage
import info.anodsplace.framework.content.getInstalledPackages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


class InstalledLoadResult(
        val recentlyInstalled: List<String>,
        val installedPackages: List<InstalledPackage>,
        appsList: AppsList,
        sections: SparseArray<SectionHeader>): LoadResult(appsList, sections)

class InstalledTaskWorker(
        private val context: ApplicationContext,
        private val sortId: Int,
        private val titleFilter: String) {

    suspend fun run(): InstalledResult = withContext(Dispatchers.IO) {
        val installedPackages = context.packageManager.getInstalledPackages()

        val recentlyInstalled = installedPackages.sortedWith(AppUpdateTimeComparator(-1))
                .take(10)
                .map { it.packageName }

        when (sortId) {
            Preferences.SORT_NAME_DESC -> Collections.sort(installedPackages, AppTitleComparator(-1))
            Preferences.SORT_DATE_ASC -> Collections.sort(installedPackages, AppUpdateTimeComparator(1))
            Preferences.SORT_DATE_DESC -> Collections.sort(installedPackages, AppUpdateTimeComparator(-1))
            else -> Collections.sort(installedPackages, AppTitleComparator(1))
        }

        if (titleFilter.isNotEmpty()) {
            val filtered = ArrayList<InstalledPackage>(installedPackages.size)
            val locale = Locale.getDefault()
            val lcFilter = titleFilter.toLowerCase(locale)
            for (installedPackage in installedPackages) {
                if (installedPackage.title.toLowerCase(locale).contains(lcFilter)) {
                    filtered.add(installedPackage)
                }
            }
            Pair(recentlyInstalled, filtered)
        } else {
            Pair(recentlyInstalled, installedPackages)
        }
    }
}
