package com.anod.appwatcher.watchlist

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.installed.InstalledPagingSource
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.prefs
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author Alex Gavrishev
 * @date 13/04/2018
 */

data class WatchListState(
        val pagingSourceConfig: WatchListPagingSource.Config,
)

sealed interface WatchListAction {
    class ItemClick(val app: App, val index: Int) : WatchListAction
    class SectionHeaderClick(val header: SectionHeader) : WatchListAction
    class EmptyButton(val idx: Int) : WatchListAction
    class ItemLongClick(val app: App, val index: Int) : WatchListAction
}

sealed interface WatchListEvent {
    object Refresh : WatchListEvent
    class AppClick(val app: App, val index: Int) : WatchListEvent
    class EmptyButton(val idx: Int) : WatchListEvent
    class AppLongClick(val app: App, val index: Int) : WatchListEvent
    class SectionHeaderClick(val type: SectionHeader) : WatchListEvent
}

abstract class FilterablePagingSource : PagingSource<Int, SectionItem>() {
    abstract var filterQuery: String
}

abstract class WatchListViewModel(pagingSourceConfig: WatchListPagingSource.Config) : BaseFlowViewModel<WatchListState, WatchListEvent, WatchListAction>(), KoinComponent {
    companion object {
        const val recentlyInstalledViews = 10
    }

    val database: AppsDatabase
        get() = getKoin().get()
    private val packageManager: PackageManager
        get() = getKoin().get()

    val installedApps = InstalledApps.MemoryCache(InstalledApps.PackageManager(packageManager))

    init {
        viewState = WatchListState(
                pagingSourceConfig = pagingSourceConfig,
        )
    }

    var filterQuery: String = ""
        set(value) {
            field = value
            (pagingSource as? InstalledPagingSource)?.filterQuery = value
        }

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

    abstract fun createPagingSource(): FilterablePagingSource
    abstract fun createSectionHeaderFactory(): SectionHeaderFactory

    override fun handleEvent(event: WatchListEvent) {
        when (event) {
            is WatchListEvent.AppClick -> emitAction(WatchListAction.ItemClick(event.app, event.index))
            is WatchListEvent.EmptyButton -> emitAction(WatchListAction.EmptyButton(event.idx))
            is WatchListEvent.AppLongClick -> emitAction(WatchListAction.ItemLongClick(event.app, event.index))
            is WatchListEvent.SectionHeaderClick -> emitAction(WatchListAction.SectionHeaderClick(event.type))
            WatchListEvent.Refresh -> {}
        }
    }

    private fun createPager(): Flow<PagingData<SectionItem>> {
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
        )
            .flow
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

    override fun createPagingSource(): WatchListPagingSource {
       return WatchListPagingSource(
            prefs = prefs,
            config = viewState.pagingSourceConfig,
            packageManager = packageManager,
            database = database,
            installedApps = installedApps
        ).also {
            it.filterQuery = filterQuery
       }
    }

    override fun createSectionHeaderFactory() = DefaultSectionHeaderFactory(viewState.pagingSourceConfig.showRecentlyUpdated)
}