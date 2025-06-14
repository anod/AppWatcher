package com.anod.appwatcher.wishlist

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
import com.anod.appwatcher.utils.date.UploadDateParserCache
import finsky.api.DfeApi
import finsky.api.FilterComposite
import finsky.api.FilterPredicate
import info.anodsplace.framework.app.FoldableDeviceLayout
import info.anodsplace.framework.content.CommonActivityAction
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.playstore.AppDetailsFilter
import info.anodsplace.playstore.AppNameFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Immutable
data class WishListState(
    val nameFilter: String = "",
    val wideLayout: FoldableDeviceLayout = FoldableDeviceLayout(),
    val selectedApp: App? = null,
)

sealed interface WishListAction {
    class ShowTagSnackbar(val info: App) : WishListAction
    class ActivityAction(val action: CommonActivityAction) : WishListAction
}

sealed interface WishListEvent {
    data object OnBackPress : WishListEvent
    class OnNameFilter(val query: String) : WishListEvent
    class SelectApp(val app: App?) : WishListEvent
    class SetWideLayout(val wideLayout: FoldableDeviceLayout) : WishListEvent
}

class WishListViewModel(wideLayout: FoldableDeviceLayout) : BaseFlowViewModel<WishListState, WishListEvent, WishListAction>(), KoinComponent {

    class Factory(
        private val wideLayout: FoldableDeviceLayout
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return WishListViewModel(wideLayout) as T
        }
    }

    private val database: AppsDatabase by inject()
    private val dfeApi: DfeApi by inject()
    private val uploadDateParserCache: UploadDateParserCache by inject()
    private val packageManager: PackageManager by inject()
    private val installedApps by lazy { InstalledApps.MemoryCache(InstalledApps.PackageManager(packageManager)) }
    val authenticated: Boolean
        get() = dfeApi.authenticated

    init {
        viewState = WishListState(
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
            pageSize = 10,
            enablePlaceholders = false,
            initialLoadSize = 10,
            prefetchDistance = 10 * 2,
            maxSize = 200
        )
    ) {
        WishListEndpointPagingSource(
            dfeApi = dfeApi,
            installedApps = installedApps,
            uploadDateParserCache = uploadDateParserCache,
        )
    }
        .flow
        .cachedIn(viewModelScope)
        .combine(
            flow = viewStates.map { it.nameFilter }.distinctUntilChanged()
        ) { pageData, nameFilter ->
            val predicate = predicate(nameFilter)
            pageData.filter { li -> predicate(li.document) }
        }
        .combine(
            flow = database.apps().observePackages()
        ) { pageData, watchedPackages -> pageData.updateRowId(watchedPackages) }

    override fun handleEvent(event: WishListEvent) {
        when (event) {
            WishListEvent.OnBackPress -> emitAction(WishListAction.ActivityAction(CommonActivityAction.Finish))
            is WishListEvent.OnNameFilter -> viewState = viewState.copy(nameFilter = event.query)
            is WishListEvent.SelectApp -> {
                viewState = viewState.copy(selectedApp = event.app)
            }

            is WishListEvent.SetWideLayout -> {
                viewState = viewState.copy(wideLayout = event.wideLayout)
            }
        }
    }

    private fun predicate(nameFilter: String): FilterPredicate {
        if (nameFilter.isBlank()) {
            return AppDetailsFilter.hasAppDetails
        }
        return FilterComposite(listOf(
            AppDetailsFilter.hasAppDetails,
            AppNameFilter(nameFilter).containsQuery
        )).predicate
    }
}