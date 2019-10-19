package com.anod.appwatcher.watchlist

import android.app.Application
import android.util.SparseArray
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.model.AppListFilterInclusion
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.InstalledApps

/**
 * @author Alex Gavrishev
 * @date 13/04/2018
 */

typealias AppsList = List<AppListItem>

open class LoadResult(val appsList: AppsList, val sections: SparseArray<SectionHeader>)

open class WatchListViewModel(application: Application): AndroidViewModel(application) {

    val context: ApplicationContext
        get() = ApplicationContext(getApplication())

    val installedApps = InstalledApps.PackageManager(context.packageManager)
    var showRecentlyUpdated = false
    val database: AppsDatabase
        get() = com.anod.appwatcher.Application.provide(context).database

    var titleFilter = ""
    var sortId = 0
    var tag: Tag? = null
    val sections: MutableLiveData<SparseArray<SectionHeader>> = MutableLiveData()
    val reload = MutableLiveData(false)

    private var filterId = 0

    internal val appsList = reload.switchMap {

        AppListTable.Queries.loadAppList(sortId, showRecentlyUpdated, tag, titleFilter, database.apps()).map { allApps ->
            val filter = createFilter(filterId, installedApps)
            val filtered = allApps.filter { appItem -> !filter.filterRecord(appItem) }
            Pair(filtered, filter)
        }
    }
    open val result = appsList.map { list ->
        val sections = SectionHeaderFactory(showRecentlyUpdated, hasSectionRecent = false, hasSectionOnDevice = false)
                .create(list.first.size,
                        list.second.newCount,
                        list.second.recentlyUpdatedCount,
                        list.second.updatableNewCount,
                        hasRecentlyInstalled = false,
                        hasInstalledPackages = false)
        this@WatchListViewModel.sections.value = sections
        LoadResult(list.first, sections)
    }

    fun init(sortId: Int, tag: Tag?, filterId: Int, prefs: Preferences) {
        this.sortId = sortId
        this.tag = tag
        this.filterId = filterId
        this.showRecentlyUpdated = prefs.showRecentlyUpdated
        this.reload.value = true
    }


    private fun createFilter(filterId: Int, installedApps: InstalledApps): AppListFilter {
        return when (filterId) {
            Filters.INSTALLED -> AppListFilterInclusion(AppListFilterInclusion.Installed(), installedApps)
            Filters.UNINSTALLED -> AppListFilterInclusion(AppListFilterInclusion.Uninstalled(), installedApps)
            Filters.UPDATABLE -> AppListFilterInclusion(AppListFilterInclusion.Updatable(), installedApps)
            else -> AppListFilterInclusion(AppListFilterInclusion.All(), installedApps)
        }
    }
}