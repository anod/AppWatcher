package com.anod.appwatcher.watchlist

import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.*
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.PackageChangedReceiver
import com.anod.appwatcher.utils.SelectionState
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author Alex Gavrishev
 * @date 13/04/2018
 */

typealias InstalledPackageRow = Pair<String, Int>

data class WatchListState(
        val pagingSourceConfig: WatchListPagingSource.Config,
        val titleFilter: String,
)

sealed interface WatchListAction {
    class ItemClick(val app: App, val index: Int) : WatchListAction
    object Reload : WatchListAction
    object SearchInStore : WatchListAction
    class SectionHeaderClick(val header: SectionHeader) : WatchListAction
    class Installed(val importMode: Boolean) : WatchListAction
    object ShareFromStore : WatchListAction
    class EmptyButton(val idx: Int) : WatchListAction
    class ItemLongClick(val app: App, val index: Int) : WatchListAction
}

sealed interface WatchListEvent {
    object Reload : WatchListEvent
    object Refresh : WatchListEvent
    class FilterByTitle(val titleFilter: String, val reload: Boolean) : WatchListEvent
    class ItemClick(val item: SectionItem, val index: Int) : WatchListEvent
    class EmptyButton(val idx: Int) : WatchListEvent
    class ItemLongClick(val item: SectionItem, val index: Int) : WatchListEvent
}

abstract class FilterablePagingSource : PagingSource<Int, SectionItem>() {
    abstract var filterQuery: String
}

abstract class WatchListViewModel(pagingSourceConfig: WatchListPagingSource.Config) : BaseFlowViewModel<WatchListState, WatchListEvent, WatchListAction>(), KoinComponent {
    companion object {
        const val recentlyInstalledViews = 10
    }

    val context: ApplicationContext
        get() = getKoin().get()
    val database: AppsDatabase
        get() = getKoin().get()
    val prefs: Preferences
        get() = getKoin().get()
    private val packageChanged: PackageChangedReceiver
        get() = getKoin().get()
    private val recentlyInstalledPackagesLoader: RecentlyInstalledPackagesLoader
        get() = getKoin().get()
    private val packageManager: PackageManager
        get() = getKoin().get()

    val installedApps = InstalledApps.MemoryCache(InstalledApps.PackageManager(packageManager))

    init {
        viewState = WatchListState(
                pagingSourceConfig = pagingSourceConfig,
                titleFilter = "",
        )
    }

    val changes = AppListTable.Queries.changes(database.apps())
            .drop(1)
            .map { (System.currentTimeMillis() / 1000).toInt() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), initialValue = 0)
            .filter { it > 0 }
            .onEach { delay(600) }
            .flowOn(Dispatchers.Default)


    protected var pagingSource: FilterablePagingSource? = null
    private var _pagingData: Flow<PagingData<SectionItem>>? = null
    val pagingData: Flow<PagingData<SectionItem>>
        get() {
            if (_pagingData == null) {
                _pagingData = createPager()
            }
            return _pagingData!!
        }

    private lateinit var headerFactory: SectionHeaderFactory
    val selection = MutableLiveData<Pair<Int, AppViewHolder.Selection>>()

    abstract fun createPagingSource(): FilterablePagingSource
    abstract fun createSectionHeaderFactory(): SectionHeaderFactory

    val recentlyInstalledPackages: Flow<List<InstalledPackageRow>> = packageChanged
            .observer
            .onStart { emit("") }
            .map { recentlyInstalledPackagesLoader.load() }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)

    override fun handleEvent(event: WatchListEvent) {
        when (event) {
            is WatchListEvent.FilterByTitle -> { // TODO: Not in use in compose implementation
                viewState = viewState.copy(titleFilter = event.titleFilter)
                pagingSource?.filterQuery = event.titleFilter
                if (event.reload) {
                    emitAction(WatchListAction.Reload)
                }
            }
            is WatchListEvent.ItemClick -> {
                when (event.item) {
                    is SectionItem.Header -> emitAction(WatchListAction.SectionHeaderClick(event.item.type))
                    is SectionItem.App -> emitAction(WatchListAction.ItemClick(event.item.appListItem.app, event.index))
                    is SectionItem.OnDevice -> emitAction(WatchListAction.ItemClick(event.item.appListItem.app, event.index))
                    SectionItem.Empty -> {}
                    SectionItem.Recent -> {}
                }
            }
            is WatchListEvent.EmptyButton -> emitAction(WatchListAction.EmptyButton(event.idx))
            WatchListEvent.Refresh -> {}
            WatchListEvent.Reload -> emitAction(WatchListAction.Reload)
            is WatchListEvent.ItemLongClick -> {
                when (event.item) {
                    is SectionItem.Header -> {}
                    is SectionItem.App -> emitAction(WatchListAction.ItemLongClick(event.item.appListItem.app, event.index))
                    is SectionItem.OnDevice -> emitAction(WatchListAction.ItemLongClick(event.item.appListItem.app, event.index))
                    SectionItem.Empty -> {}
                    SectionItem.Recent -> {}
                }
            }
        }
    }

    fun createPager(): Flow<PagingData<SectionItem>> {
        headerFactory = createSectionHeaderFactory()
        installedApps.reset()

        // When initialLoadSize larger than pageSize it cause a bug
        // where after filter if there is only one pages items are shown multiple times
        return Pager(
                config = PagingConfig(pageSize = WatchListPagingSource.pageSize, initialLoadSize = WatchListPagingSource.pageSize),
                initialKey = null,
                pagingSourceFactory = {
                    createPagingSource().also {
                        pagingSource = it
                    }
                }
        ).flow
                .map { pagingData: PagingData<SectionItem> ->
                    pagingData.insertSeparators { before, after ->
                        headerFactory.insertSeparator(before, after)
                    }
                }
                .cachedIn(viewModelScope)
    }
}

class AppsWatchListViewModel(pagingSourceConfig: WatchListPagingSource.Config) : WatchListViewModel(pagingSourceConfig), KoinComponent {
    private val packageManager: PackageManager by inject()

    class Factory(private val pagingSourceConfig: WatchListPagingSource.Config) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return AppsWatchListViewModel(pagingSourceConfig) as T
        }
    }

    override fun createPagingSource() = WatchListPagingSource(
            filterQuery = viewState.titleFilter,
            prefs = prefs,
            config = viewState.pagingSourceConfig,
            packageManager = packageManager,
            database = database,
            installedApps = installedApps
    )

    override fun createSectionHeaderFactory() = DefaultSectionHeaderFactory(viewState.pagingSourceConfig.showRecentlyUpdated)
}