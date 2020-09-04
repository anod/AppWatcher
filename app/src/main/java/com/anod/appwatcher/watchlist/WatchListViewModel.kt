package com.anod.appwatcher.watchlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.debounce
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Alex Gavrishev
 * @date 13/04/2018
 */

class WatchListViewModel(application: Application) : AndroidViewModel(application) {

    private val context: ApplicationContext
        get() = ApplicationContext(getApplication())
    private val database: AppsDatabase
        get() = com.anod.appwatcher.Application.provide(context).database
    private val prefs: Preferences
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

    var pagingSource: WatchListPagingSource? = null
        private set

    private var hasData = false
    val changes = AppListTable.Queries.changes(database.apps())
            .debounce(600)
            .map {
                if (hasData) {
                    hasData = false
                    pagingSource?.invalidate()
                }
                true
            }

    private lateinit var headerFactory: SectionHeaderFactory
    private var filter: AppListFilter = AppListFilter.All()

    fun load(config: WatchListPagingSource.Config): Flow<PagingData<SectionItem>> {
        headerFactory = SectionHeaderFactory(config.showRecentlyUpdated)
        hasData = false

        return Pager(PagingConfig(pageSize = 10)) {
            pagingSource = WatchListPagingSource(
                    sortId = sortId,
                    titleFilter = titleFilter,
                    config = config,
                    tag = tag,
                    appContext = context
            )
            pagingSource!!
        }.flow.map { pagingData: PagingData<SectionItem> ->
            hasData = true
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