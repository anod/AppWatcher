package com.anod.appwatcher.watchlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map

/**
 * @author Alex Gavrishev
 * @date 13/04/2018
 */

abstract class WatchListViewModel(application: Application) : AndroidViewModel(application) {
    val context: ApplicationContext
        get() = ApplicationContext(getApplication())
    val provide: AppComponent
        get() = com.anod.appwatcher.Application.provide(context)
    val database: AppsDatabase
        get() = provide.database
    val prefs: Preferences
        get() = provide.prefs
    var titleFilter = ""
    var sortId = 0
    var tag: Tag? = null
    val installedApps = InstalledApps.MemoryCache(InstalledApps.PackageManager(context.packageManager))
    var filterId: Int = Filters.TAB_ALL
        get() = this.filter.filterId
        set(value) {
            field = value
            this.filter = createFilter(value, installedApps)
        }
    var filter: AppListFilter = AppListFilter.All()
        private set

    private var hasData = false
    private var firstChange = true
    val changes = AppListTable.Queries.changes(database.apps())
            .debounce(600)
            .map {
                if (hasData && !firstChange) {
                    hasData = false
                    pagingSource?.invalidate()
                }
                firstChange = false
                true
            }
    var pagingSource: PagingSource<Int, SectionItem>? = null
        private set

    private lateinit var headerFactory: SectionHeaderFactory
    val selection = MutableLiveData<Pair<Int, AppViewHolder.Selection>>()

    abstract fun createPagingSource(config: WatchListPagingSource.Config): PagingSource<Int, SectionItem>
    abstract fun createSectionHeaderFactory(config: WatchListPagingSource.Config): SectionHeaderFactory

    fun load(config: WatchListPagingSource.Config): Flow<PagingData<SectionItem>> {
        headerFactory = createSectionHeaderFactory(config)
        hasData = false
        installedApps.reset()
        // When initialLoadSize larger than pageSize it cause a bug
        // where after filter if there is only one pages items are shown multiple times
        return Pager(PagingConfig(pageSize = 20, initialLoadSize = 20)) {
            pagingSource = createPagingSource(config)
            pagingSource!!
        }.flow.map { pagingData: PagingData<SectionItem> ->
            hasData = true
            pagingData
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

class AppsWatchListViewModel(application: Application) : WatchListViewModel(application) {

    override fun createPagingSource(config: WatchListPagingSource.Config) = WatchListPagingSource(
            sortId = sortId,
            titleFilter = titleFilter,
            config = config,
            itemFilter = filter,
            tag = tag,
            appContext = context
    )

    override fun createSectionHeaderFactory(config: WatchListPagingSource.Config) = DefaultSectionHeaderFactory(config.showRecentlyUpdated)

}