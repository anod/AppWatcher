package com.anod.appwatcher.installed

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.support.v4.util.SimpleArrayMap
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.AppListCursorLoader
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.model.Tag
import info.anodsplace.framework.database.FilterCursor
import info.anodsplace.framework.content.getAppTitle
import info.anodsplace.framework.content.getAppUpdateTime
import info.anodsplace.framework.content.getInstalledPackagesCompat
import java.util.*

class InstalledLoader(
        context: Context,
        titleFilter: String,
        private val sortId: Int,
        cursorFilter: AppListFilter,
        tag: Tag?,
        private val packageManager: PackageManager)
    : AppListCursorLoader(context, titleFilter, sortId, cursorFilter, tag) {

    private val titleCache = SimpleArrayMap<String, String>()
    private val updateTimeCache = SimpleArrayMap<String, Long>()

    var installedApps = listOf<String>()
        private set
    var recentlyInstalled = listOf<Pair<String, Int>>()
        private set

    override fun loadInBackground(): Cursor {
        val cursor = super.loadInBackground()

        val cr = DbContentProviderClient(context)
        val watchingPackages = cr.queryPackagesMap(false)
        cr.close()

        val installed = packageManager.getInstalledPackagesCompat()

        this.recentlyInstalled = installed
                .sortedWith(AppUpdateTimeComparator(-1, this))
                .take(10)
                .map {
                    val rowId = watchingPackages[it] ?: -1
                    Pair(it, rowId)
                }

        val list = installed.filter { !watchingPackages.containsKey(it) }
        when (sortId) {
            Preferences.SORT_NAME_DESC -> Collections.sort(list, AppTitleComparator(-1, this))
            Preferences.SORT_DATE_ASC -> Collections.sort(list, AppUpdateTimeComparator(1, this))
            Preferences.SORT_DATE_DESC -> Collections.sort(list, AppUpdateTimeComparator(-1, this))
            else -> Collections.sort(list, AppTitleComparator(1, this))
        }

        if (titleFilter.isNotEmpty()) {
            val filtered = ArrayList<String>(list.size)
            val lcFilter = titleFilter.toLowerCase()
            for (packageName in list) {
                val title = getPackageTitle(packageName)
                if (title.toLowerCase().contains(lcFilter)) {
                    filtered.add(packageName)
                }
            }
            installedApps = filtered
        } else {
            installedApps = list
        }
        titleCache.clear()
        return cursor
    }

    private fun getPackageTitle(packageName: String): String {
        if (titleCache.containsKey(packageName)) {
            return titleCache.get(packageName)
        } else {
            val title = packageManager.getAppTitle(packageName)
            titleCache.put(packageName, title)
            return title
        }
    }

    private fun getPackageUpdateTime(packageName: String): Long {
        if (updateTimeCache.containsKey(packageName)) {
            return updateTimeCache.get(packageName)
        } else {
            val updateTime = packageManager.getAppUpdateTime(packageName)
            updateTimeCache.put(packageName, updateTime)
            return updateTime
        }
    }

    private class AppTitleComparator(private val order: Int, private val loader: InstalledLoader) : Comparator<String> {
        override fun compare(lPackageName: String, rPackageName: String): Int {
            return order * loader.getPackageTitle(lPackageName).compareTo(loader.getPackageTitle(rPackageName))
        }
    }

    private class AppUpdateTimeComparator(private val order: Int, private val loader: InstalledLoader) : Comparator<String> {

        override fun compare(lPackageName: String, rPackageName: String): Int {
            val lTime = loader.getPackageUpdateTime(lPackageName)
            val rTime = loader.getPackageUpdateTime(rPackageName)
            return order * lTime.compareTo(rTime)
        }
    }
}
