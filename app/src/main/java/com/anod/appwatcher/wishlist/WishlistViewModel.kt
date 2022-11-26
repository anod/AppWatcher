package com.anod.appwatcher.wishlist

import android.accounts.Account
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.*
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.search.ListEndpointPagingSource
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.date.UploadDateParserCache
import finsky.api.model.Document
import finsky.api.model.FilterComposite
import finsky.api.model.FilterPredicate
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.playstore.AppDetailsFilter
import info.anodsplace.playstore.AppNameFilter
import info.anodsplace.playstore.WishListEndpoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class WishListState(
        val account: Account? = null,
        val authToken: String = "",
        val nameFilter: String = "",
        val watchingPackages: List<String> = emptyList(),
)

sealed interface WishListActivityAction {
    class ShowTagList(val info: AppInfo) : WishListActivityAction
    object OnBackPress : WishListActivityAction
}

sealed interface WishListAction {
    class ShowTagSnackbar(val info: AppInfo) : WishListAction
    class AlreadyWatchedNotice(val document: Document) : WishListAction
    object OnBackPress : WishListAction
}

sealed interface WishListEvent {
    object OnBackPress : WishListEvent
    class Delete(val document: Document) : WishListEvent
    class ItemClick(val document: Document) : WishListEvent
    class OnNameFilter(val query: String) : WishListEvent
}

class WishListViewModel(account: Account?, authToken: String) : BaseFlowViewModel<WishListState, WishListEvent, WishListAction>(), KoinComponent {

    class Factory(private val account: Account?, private val authToken: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return WishListViewModel(account, authToken) as T
        }
    }

    private val database: AppsDatabase by inject()
    private val endpoint: WishListEndpoint by inject()
    private val uploadDateParserCache: UploadDateParserCache by inject()
    private val packageManager: PackageManager by inject()
    val installedApps by lazy { InstalledApps.MemoryCache(InstalledApps.PackageManager(packageManager)) }

    init {
        viewState = WishListState(
                account = account,
                authToken = authToken
        )
        viewModelScope.launch {
            database.apps().observePackages().map { list ->
                list.map { it.packageName }
            }.collect {
                viewState = viewState.copy(watchingPackages = it)
            }
        }
    }

    private var _pagingData: Flow<PagingData<Document>>? = null
    val pagingData: Flow<PagingData<Document>>
        get() {
            if (_pagingData == null) {
                _pagingData = createPager()
            }
            return _pagingData!!
        }

    private fun createPager() = Pager(PagingConfig(pageSize = 10)) { ListEndpointPagingSource(endpoint) }
            .flow
            .cachedIn(viewModelScope)
            .combine(viewStates.map { it.nameFilter }.distinctUntilChanged()) { pageData, nameFilter ->
                val predicate = predicate(nameFilter)
                pageData.filter { d -> predicate(d) }
            }

    override fun handleEvent(event: WishListEvent) {
        when (event) {
            is WishListEvent.Delete -> delete(event.document)
            is WishListEvent.ItemClick -> {
                if (viewState.watchingPackages.contains(event.document.appDetails.packageName)) {
                    emitAction(WishListAction.AlreadyWatchedNotice(event.document))
                } else add(event.document)
            }
            WishListEvent.OnBackPress -> emitAction(WishListAction.OnBackPress)
            is WishListEvent.OnNameFilter -> viewState = viewState.copy(nameFilter = event.query)
        }
    }

    private fun predicate(nameFilter: String): FilterPredicate {
        if (nameFilter.isBlank()) {
            return AppDetailsFilter.predicate
        }
        return FilterComposite(listOf(
                AppDetailsFilter.predicate,
                AppNameFilter(nameFilter).predicate
        )).predicate
    }

    private fun delete(document: Document) {
        viewModelScope.launch {
            val info = AppInfo(document, uploadDateParserCache)
            AppListTable.Queries.delete(info.appId, database)
        }
    }

    private fun add(document: Document) {
        viewModelScope.launch {
            val info = AppInfo(document, uploadDateParserCache)
            val result = AppListTable.Queries.insertSafetly(info, database)
            if (result != AppListTable.ERROR_INSERT) {
                emitAction(WishListAction.ShowTagSnackbar(info = info))
            }
        }
    }
}