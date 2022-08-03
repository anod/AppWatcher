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
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.model.Filters
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
        val titleFilter: String = "",
        val sortId: Int = 0,
        val tag: Tag? = null,
        val filter: AppListFilter = AppListFilter.All()
)

sealed interface WatchListAction {
    class ItemClick(val app: App, val index: Int) : WatchListAction
    object Reload : WatchListAction
    object SearchInStore : WatchListAction
    class SectionHeaderClick(val header: SectionHeader) : WatchListAction
    class Installed(val importMode: Boolean) : WatchListAction
    object ShareFromStore : WatchListAction
    class AddAppToTag(val tag: Tag) : WatchListAction
    class EmptyButton(val idx: Int) : WatchListAction
    class ItemLongClick(val app: App, val index: Int) : WatchListAction
}

sealed interface WatchListEvent {
    class SetFilter(val filterId: Int) : WatchListEvent
    class ChangeSort(val sortId: Int) : WatchListEvent
    class FilterByTitle(val titleFilter: String) : WatchListEvent
    class ItemClick(val item: SectionItem, val index: Int) : WatchListEvent
}

data class WatchListPageArgs(val sortId: Int, val filterId: Int, val tag: Tag?)

abstract class WatchListViewModel(args: WatchListPageArgs) : BaseFlowViewModel<WatchListState, WatchListEvent, WatchListAction>(), KoinComponent {
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
                filter = createFilter(args.filterId),
                sortId = args.sortId,
                tag = args.tag,
        )
    }

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

    val recentlyInstalledPackages: Flow<List<InstalledPackageRow>> = packageChanged
            .observer
            .onStart { emit("") }
            .map { recentlyInstalledPackagesLoader.load() }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)

    override fun handleEvent(event: WatchListEvent) {
        when (event) {
            is WatchListEvent.SetFilter -> {
                viewState = viewState.copy(filter = createFilter(event.filterId))
            }
            is WatchListEvent.ChangeSort -> {
                viewState = viewState.copy(sortId = event.sortId)
                emitAction(WatchListAction.Reload)
            }
            is WatchListEvent.FilterByTitle -> {
                viewState = viewState.copy(titleFilter = event.titleFilter)
                emitAction(WatchListAction.Reload)
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
        }
    }

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
                }
                .cachedIn(viewModelScope)
    }

    private fun createFilter(filterId: Int): AppListFilter {
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

class AppsWatchListViewModel(args: WatchListPageArgs) : WatchListViewModel(args), KoinComponent {
    private val packageManager: PackageManager by inject()

    class Factory(private val args: WatchListPageArgs) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return AppsWatchListViewModel(args) as T
        }
    }

    override fun createPagingSource(config: WatchListPagingSource.Config) = WatchListPagingSource(
            sortId = viewState.sortId,
            titleFilter = viewState.titleFilter,
            config = config,
            itemFilter = viewState.filter,
            tag = viewState.tag,
            packageManager = packageManager,
            database = database
    )

    override fun createSectionHeaderFactory(config: WatchListPagingSource.Config) = DefaultSectionHeaderFactory(config.showRecentlyUpdated)

}