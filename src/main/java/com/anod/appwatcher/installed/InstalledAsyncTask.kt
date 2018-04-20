package com.anod.appwatcher.installed

import android.app.Application
import android.content.pm.PackageManager
import android.support.v4.util.SimpleArrayMap
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.watchlist.AppListAsyncTask
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.getAppTitle
import info.anodsplace.framework.content.getAppUpdateTime
import info.anodsplace.framework.content.getInstalledPackagesCompat
import java.util.*

/**
 * @author algavris
 * @date 14/04/2018
 */

typealias PackageRowPair = Pair<String, Int>
typealias InstalledAsyncTaskCompletion = (List<AppInfo>, AppListFilter, installedPackages: List<String>, recentlyInstalled: List<PackageRowPair>) -> Unit

class InstalledAsyncTask(private val context: ApplicationContext,
                         private val titleFilter: String,
                         private val sortId: Int,
                         cursorFilter: AppListFilter,
                         tag: Tag?,
                         private val completion: InstalledAsyncTaskCompletion)
    : AppListAsyncTask(context, titleFilter, sortId, cursorFilter, tag, { _, _ -> }) {

    constructor(application: Application, titleFilter: String, sortId: Int, listFilter: AppListFilter, tag: Tag?, completion: InstalledAsyncTaskCompletion)
            : this(ApplicationContext(application), titleFilter, sortId, listFilter, tag, completion)

    private val packageManager: PackageManager = context.packageManager

    private var installedApps = listOf<String>()
    private var recentlyInstalled = listOf<PackageRowPair>()

    private val titleCache = SimpleArrayMap<String, String>()
    private val updateTimeCache = SimpleArrayMap<String, Long>()

    override fun doInBackground(vararg params: Void?): List<AppInfo> {
        val apps = super.doInBackground()

        val cr = DbContentProviderClient(context)
        val watchingPackages = cr.queryPackagesMap(false)
        cr.close()

        val installed = packageManager.getInstalledPackagesCompat()

        this.recentlyInstalled = installed
                .sortedWith(InstalledAsyncTask.AppUpdateTimeComparator(-1, this))
                .take(10)
                .map {
                    val rowId = watchingPackages[it] ?: -1
                    Pair(it, rowId)
                }

        val list = installed.filter { !watchingPackages.containsKey(it) }
        when (sortId) {
            Preferences.SORT_NAME_DESC -> Collections.sort(list, InstalledAsyncTask.AppTitleComparator(-1, this))
            Preferences.SORT_DATE_ASC -> Collections.sort(list, InstalledAsyncTask.AppUpdateTimeComparator(1, this))
            Preferences.SORT_DATE_DESC -> Collections.sort(list, InstalledAsyncTask.AppUpdateTimeComparator(-1, this))
            else -> Collections.sort(list, InstalledAsyncTask.AppTitleComparator(1, this))
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

        return apps
    }

    override fun onPostExecute(result: List<AppInfo>?) {
        completion(result ?: emptyList(), listFilter, this.installedApps, this.recentlyInstalled)
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

    private class AppTitleComparator(private val order: Int, private val task: InstalledAsyncTask) : Comparator<String> {
        override fun compare(lPackageName: String, rPackageName: String): Int {
            return order * task.getPackageTitle(lPackageName).compareTo(task.getPackageTitle(rPackageName))
        }
    }

    private class AppUpdateTimeComparator(private val order: Int, private val task: InstalledAsyncTask) : Comparator<String> {

        override fun compare(lPackageName: String, rPackageName: String): Int {
            val lTime = task.getPackageUpdateTime(lPackageName)
            val rTime = task.getPackageUpdateTime(rPackageName)
            return order * lTime.compareTo(rTime)
        }
    }
}