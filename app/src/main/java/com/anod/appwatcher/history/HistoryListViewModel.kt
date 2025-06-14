package com.anod.appwatcher.history

import android.accounts.Account
import android.content.pm.PackageManager
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.observePackages
import com.anod.appwatcher.search.ListItem
import com.anod.appwatcher.search.updateRowId
import com.anod.appwatcher.utils.BaseFlowViewModel
import finsky.api.DfeApi
import finsky.api.FilterComposite
import finsky.api.FilterPredicate
import info.anodsplace.framework.app.FoldableDeviceLayout
import info.anodsplace.framework.content.CommonActivityAction
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.playstore.AppNameFilter
import info.anodsplace.playstore.PaidHistoryFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Immutable
data class HistoryListState(
    val account: Account? = null,
    val authToken: String = "",
    val nameFilter: String = "",
    val wideLayout: FoldableDeviceLayout = FoldableDeviceLayout(),
    val selectedApp: App? = null,
)

sealed interface HistoryListAction {
    class ShowTagSnackbar(val info: App) : HistoryListAction
    class ActivityAction(val action: CommonActivityAction) : HistoryListAction
}

sealed interface HistoryListEvent {
    data object OnBackPress : HistoryListEvent
    class OnNameFilter(val query: String) : HistoryListEvent
    class SelectApp(val app: App?) : HistoryListEvent
    class SetWideLayout(val wideLayout: FoldableDeviceLayout) : HistoryListEvent
}

class HistoryListViewModel(wideLayout: FoldableDeviceLayout) : BaseFlowViewModel<HistoryListState, HistoryListEvent, HistoryListAction>(), KoinComponent {

    class Factory(
        private val wideLayout: FoldableDeviceLayout
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return HistoryListViewModel(wideLayout) as T
        }
    }

    private val database: AppsDatabase by inject()
    private val dfeApi: DfeApi by inject()
    private val packageManager: PackageManager by inject()
    private val installedApps by lazy { InstalledApps.MemoryCache(InstalledApps.PackageManager(packageManager)) }
    val authenticated: Boolean
        get() = dfeApi.authenticated

    init {
        viewState = HistoryListState(
            wideLayout = wideLayout
        )
    }

    private var _pagingData: Flow<PagingData<ListItem>>? = null
    val pagingData: Flow<PagingData<ListItem>>
        get() {
            if (_pagingData == null) {
                _pagingData = createPager()
            }
            return _pagingData!!
        }

    private fun createPager() = Pager(
        PagingConfig(
            pageSize = 17,
            enablePlaceholders = false,
            initialLoadSize = 17,
            prefetchDistance = 17 * 2,
            maxSize = 204
        )
    ) {
        HistoryEndpointPagingSource(
            dfeApi = dfeApi,
            installedApps = installedApps,
        )
    }
        .flow
        .cachedIn(viewModelScope)
        .combine(
            flow = viewStates.map { it.nameFilter }.distinctUntilChanged(),
        ) { pageData, nameFilter ->
            val predicate = predicate(nameFilter)
            pageData.filter { li -> predicate(li.document) }
        }.combine(
            flow = database.apps().observePackages().distinctUntilChanged()
        ) { pageData, watchingPackages -> pageData.updateRowId(watchingPackages) }

    override fun handleEvent(event: HistoryListEvent) {
        when (event) {
            HistoryListEvent.OnBackPress -> emitAction(HistoryListAction.ActivityAction(CommonActivityAction.Finish))
            is HistoryListEvent.OnNameFilter -> viewState = viewState.copy(nameFilter = event.query)
            is HistoryListEvent.SelectApp -> {
                viewState = viewState.copy(selectedApp = event.app)
            }
            is HistoryListEvent.SetWideLayout -> {
                viewState = viewState.copy(wideLayout = event.wideLayout)
            }
        }
    }

    private fun predicate(nameFilter: String): FilterPredicate {
        if (nameFilter.isBlank()) {
            return FilterComposite(listOf(
                PaidHistoryFilter.hasPrice,
                PaidHistoryFilter.noPurchaseStatus
            )).predicate
        }
        return FilterComposite(listOf(
            PaidHistoryFilter.hasPrice,
            PaidHistoryFilter.noPurchaseStatus,
            AppNameFilter(nameFilter).containsQuery
        )).predicate
    }
}