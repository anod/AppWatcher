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
import com.anod.appwatcher.utils.SelectionState
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

/**
 * @author Alex Gavrishev
 * @date 13/04/2018
 */

typealias InstalledPackageRow = Pair<String, Int>

abstract class WatchListViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val recentlyInstalledViews = 10
    }

    var firstResume: Boolean = true
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

    val changes = AppListTable.Queries.changes(database.apps())
         .drop(1)
        .map { (System.currentTimeMillis() / 1000).toInt() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialValue = 0)
        .filter { it > 0 }
        .onEach { delay(600) }
        .flowOn(Dispatchers.Default)

    var pagingSource: PagingSource<Int, SectionItem>? = null
        private set

    private lateinit var headerFactory: SectionHeaderFactory
    val selection = MutableLiveData<Pair<Int, AppViewHolder.Selection>>()

    abstract fun createPagingSource(config: WatchListPagingSource.Config): PagingSource<Int, SectionItem>
    abstract fun createSectionHeaderFactory(config: WatchListPagingSource.Config): SectionHeaderFactory

    val recentlyInstalledPackages: Flow<List<InstalledPackageRow>> = provide
        .packageRemoved.onStart { emit("") }.map {
            provide.recentlyInstalledPackages.load()
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)

    fun load(config: WatchListPagingSource.Config, initialKey: Int? = null): Flow<PagingData<SectionItem>> {
        headerFactory = createSectionHeaderFactory(config)
        installedApps.reset()
        // When initialLoadSize larger than pageSize it cause a bug
        // where after filter if there is only one pages items are shown multiple times
        return Pager(PagingConfig(pageSize = WatchListPagingSource.pageSize, initialLoadSize = WatchListPagingSource.pageSize), initialKey = initialKey) {
            pagingSource = createPagingSource(config)
            pagingSource!!
        }.flow
                .map { pagingData: PagingData<SectionItem> ->
                    pagingData.insertSeparators { before, after ->
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

    fun updateSelection(
        change: SelectionState.Change,
        getPackageSelection: (key: String) -> AppViewHolder.Selection
    ) {
        val index = change.extras.getInt("index", -1)
        if (change.key == null) {
            selection.value = Pair(
                index,
                if (change.defaultSelected) AppViewHolder.Selection.Selected else AppViewHolder.Selection.NotSelected
            )
        } else {
            selection.value = Pair(index, getPackageSelection(change.key))
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