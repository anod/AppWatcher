package com.anod.appwatcher.watchlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Alex Gavrishev
 * @date 13/04/2018
 */

class WatchListViewModel(application: Application) : AndroidViewModel(application) {

    val context: ApplicationContext
        get() = ApplicationContext(getApplication())
    val database: AppsDatabase
        get() = com.anod.appwatcher.Application.provide(context).database
    val prefs: Preferences
        get() = com.anod.appwatcher.Application.provide(context).prefs

    val installedApps = InstalledApps.PackageManager(context.packageManager)
    var titleFilter = ""
    var sortId = 0
    var tag: Tag? = null
    var filterId: Int = Filters.TAB_ALL
        get() = this.filter.filterId
        set(value) {
            field = value
            this.filter = createFilter(value, installedApps)
        }

    private lateinit var headerFactory: SectionHeaderFactory
    private var filter: AppListFilter = AppListFilter.All()

    fun load(): Flow<PagingData<SectionItem>> {
        val showRecentlyUpdated = prefs.showRecentlyUpdated
        headerFactory = SectionHeaderFactory(showRecentlyUpdated)

        return Pager(PagingConfig(pageSize = 20)) {
            WatchListPagingSource(
                    sortId = sortId,
                    showRecentlyUpdated = showRecentlyUpdated,
                    showOnDevice = filterId == Filters.TAB_ALL && prefs.showOnDevice,
                    showRecentlyInstalled = filterId == Filters.TAB_ALL && prefs.showRecent,
                    titleFilter = titleFilter,
                    tag = tag,
                    appContext = context
            )
        }.flow.map { pagingData: PagingData<SectionItem> ->
            pagingData
                    .filterSync { item ->
                        if (item is AppItem) {
                            !filter.filterRecord(item.appListItem)
                        } else true
                    }
                    .insertSeparators { before, after ->
                        headerFactory.insertSeparator(before, after)
                    }
        }.cachedIn(viewModelScope)
    }

    private fun createFilter(filterId: Int, installedApps: InstalledApps): AppListFilter {
        return when (filterId) {
            Filters.INSTALLED -> AppListFilter.Installed(installedApps)
            Filters.UNINSTALLED -> AppListFilter.Uninstalled(installedApps)
            Filters.UPDATABLE -> AppListFilter.Updatable(installedApps)
            else -> AppListFilter.All()
        }
    }

}