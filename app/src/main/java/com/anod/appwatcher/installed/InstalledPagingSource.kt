// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.content.pm.PackageManager
import androidx.paging.PagingState
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.watchlist.FilterablePagingSource
import com.anod.appwatcher.watchlist.SectionItem
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.util.dayStartAgoMillis

class InstalledPagingSource(
    private val changelogAdapter: ChangelogAdapter,
    private val packageManager: PackageManager,
    private val database: AppsDatabase,
    private val installedApps: InstalledApps,
) : FilterablePagingSource() {
    override var filterQuery: String = ""
    var sortId: Int = 0
    var selectionMode: Boolean = false

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SectionItem> {
        AppLog.d("$params")
        val installed = InstalledTaskWorker(packageManager, sortId, filterQuery).run()
        val allInstalledPackageNames = installed.map { it.pkg.name }
        val watchingPackages = database.apps().loadRowIds(allInstalledPackageNames).associateBy({ it.packageName }, { it.rowId })

        if (sortId == Preferences.SORT_DATE_ASC || sortId == Preferences.SORT_DATE_DESC) {
            val recentTime = dayStartAgoMillis(Preferences.recentDays)
            val recentNotWatched = installed.filter {
                !watchingPackages.containsKey(it.pkg.name) && it.pkg.updateTime > recentTime
            }.map { it.pkg }
            if (recentNotWatched.isNotEmpty() || watchingPackages.isNotEmpty()) {
                changelogAdapter.load(watchingPackages.keys.toList(), recentNotWatched)
            }
        }

        val items: List<SectionItem> = installed
                .asSequence()
                .mapNotNull {
                    val rowId = watchingPackages[it.pkg.name] ?: -1
                    if (selectionMode && rowId >= 0)
                        null
                    else
                        App.fromInstalledPackage(rowId, it)
                }
                .map { app ->
                    val appChange = changelogAdapter.changelogs[app.appId]
                    SectionItem.OnDevice(
                        appListItem = AppListItem(
                            app = app,
                            changeDetails = appChange?.details ?: "",
                            noNewDetails = false,
                            recentFlag = false
                        ),
                        showSelection = selectionMode,
                        packageInfo = installedApps.packageInfo(app.packageName)
                    )
                }.toList()


        return LoadResult.Page(
                data = items,
                prevKey = null,
                nextKey = null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, SectionItem>): Int? = null
}