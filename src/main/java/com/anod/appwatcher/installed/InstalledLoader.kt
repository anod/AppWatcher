package com.anod.appwatcher.installed

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.support.v4.util.SimpleArrayMap
import android.text.TextUtils
import com.anod.appwatcher.Preferences
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.AppListCursorLoader
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.utils.FilterCursorWrapper
import com.anod.appwatcher.utils.PackageManagerUtils
import java.util.*

class InstalledLoader(
        context: Context,
        titleFilter: String,
        private val sortId: Int,
        cursorFilter: FilterCursorWrapper.CursorFilter?,
        tag: Tag?,
        private val mPackageManager: PackageManager)
    : AppListCursorLoader(context, titleFilter, sortId, cursorFilter, tag) {

    private val mTitleCache = SimpleArrayMap<String, String>()
    private val mUpdateTimeCache = SimpleArrayMap<String, Long>()

    var installedApps = listOf<String>()
        private set
    var recentlyInstalled = listOf<String>()
        private set

    override fun loadInBackground(): Cursor {
        val cursor = super.loadInBackground()

        val cr = DbContentProviderClient(context)
        val watchingPackages = cr.queryPackagesMap(false)
        cr.close()

        val list = PackageManagerUtils.getDownloadedApps(watchingPackages, mPackageManager)

        if (sortId == Preferences.SORT_NAME_DESC) {
            Collections.sort(list, AppTitleComparator(-1, this))
        } else if (sortId == Preferences.SORT_DATE_ASC) {
            Collections.sort(list, AppUpdateTimeComparator(1, this))
        } else if (sortId == Preferences.SORT_DATE_DESC) {
            Collections.sort(list, AppUpdateTimeComparator(-1, this))
        } else {
            Collections.sort(list, AppTitleComparator(1, this))
        }

        this.recentlyInstalled = list.sortedWith(AppUpdateTimeComparator(-1, this)).take(10)

        if (!TextUtils.isEmpty(titleFilter)) {
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
        mTitleCache.clear()
        return cursor
    }

    private fun getPackageTitle(packageName: String): String {
        if (mTitleCache.containsKey(packageName)) {
            return mTitleCache.get(packageName)
        } else {
            val title = PackageManagerUtils.getAppTitle(packageName, mPackageManager)
            mTitleCache.put(packageName, title)
            return title
        }
    }

    private fun getPackageUpdateTime(packageName: String): Long {
        if (mUpdateTimeCache.containsKey(packageName)) {
            return mUpdateTimeCache.get(packageName)
        } else {
            val updateTime = PackageManagerUtils.getAppUpdateTime(packageName, mPackageManager)
            mUpdateTimeCache.put(packageName, updateTime)
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
