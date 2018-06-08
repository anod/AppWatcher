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
import info.anodsplace.framework.os.LiveDataTask
import java.util.*


class InstalledLoadResult(
        val recentlyInstalled: List<InstalledPairRow>,
        val installedPackages: List<InstalledPackage>,
        appsList: AppsList,
        sections: SparseArray<SectionHeader>): LoadResult(appsList, sections)

class InstalledTaskWorker(
        private val context: ApplicationContext,
        private val sortId: Int,
        private val titleFilter: String) : LiveDataTask.Worker<Void?, InstalledResult>(null) {

    override fun run(param: Void?): InstalledResult {
        val appsTable = Application.provide(context).database.apps()
        val watchingPackages = appsTable.loadPackages(false).associateBy { it.packageName }
        val installedPackages = context.packageManager.getInstalledPackages()

        val recentlyInstalled = installedPackages.sortedWith(AppUpdateTimeComparator(-1))
                .take(10)
                .map {
                    val rowId = watchingPackages[it.packageName]?.rowId ?: -1
                    Pair(it.packageName, rowId)
                }

        val list = installedPackages.filter { !watchingPackages.containsKey(it.packageName) }
        when (sortId) {
            Preferences.SORT_NAME_DESC -> Collections.sort(list, AppTitleComparator(-1))
            Preferences.SORT_DATE_ASC -> Collections.sort(list, AppUpdateTimeComparator(1))
            Preferences.SORT_DATE_DESC -> Collections.sort(list, AppUpdateTimeComparator(-1))
            else -> Collections.sort(list, AppTitleComparator(1))
        }

        if (titleFilter.isNotEmpty()) {
            val filtered = ArrayList<InstalledPackage>(list.size)
            val lcFilter = titleFilter.toLowerCase()
            for (installedPackage in list) {
                if (installedPackage.title.toLowerCase().contains(lcFilter)) {
                    filtered.add(installedPackage)
                }
            }
            return Pair(recentlyInstalled, filtered)
        } else {
            return Pair(recentlyInstalled, list)
        }
    }
}
