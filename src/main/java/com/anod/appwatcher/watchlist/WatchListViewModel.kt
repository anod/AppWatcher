package com.anod.appwatcher.watchlist

import android.app.Application
import androidx.lifecycle.*
import android.util.SparseArray
import androidx.room.InvalidationTracker
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.*
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext

/**
 * @author Alex Gavrishev
 * @date 13/04/2018
 */

typealias AppsList = List<AppListItem>

open class LoadResult(val appsList: AppsList, val sections: SparseArray<SectionHeader>)

open class WatchListViewModel(application: Application): AndroidViewModel(application) {

    val context: ApplicationContext
        get() = ApplicationContext(getApplication())

    var showRecentlyUpdated = false
    val database: AppsDatabase
        get() = com.anod.appwatcher.Application.provide(context).database

    var titleFilter = ""
    var sortId = 0
    var filter: AppListFilter = AppListFilter.None()
    var tag: Tag? = null
    val sections: MutableLiveData<SparseArray<SectionHeader>> = MutableLiveData()
    val reload = MutableLiveData(false)
    val appsList = reload.switchMap {
        AppListTable.Queries.loadAppList(sortId, tag, titleFilter, database.apps()).map { allApps ->
            filter.resetNewCount()
            val filtered = allApps.filter { appItem -> !filter.filterRecord(appItem) }
            filtered
        }
    }
    open val result = appsList.map {
        val sections = SectionHeaderFactory(showRecentlyUpdated, hasSectionRecent = false, hasSectionOnDevice = false)
                .create(it.size,
                        filter.newCount,
                        filter.recentlyUpdatedCount,
                        filter.updatableNewCount,
                        hasRecentlyInstalled = false,
                        hasInstalledPackages = false)
        this@WatchListViewModel.sections.value = sections
        LoadResult(it, sections)
    }

    fun init(sortId: Int, tag: Tag?, listFilter: AppListFilter, prefs: Preferences) {
        this.sortId = sortId
        this.tag = tag
        this.filter = listFilter
        this.showRecentlyUpdated = prefs.showRecentlyUpdated
        this.reload.value = true
    }
}