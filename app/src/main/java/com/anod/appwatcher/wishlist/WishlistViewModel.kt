package com.anod.appwatcher.wishlist

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.filter
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.search.ListEndpointPagingSource
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.date.UploadDateParserCache
import finsky.api.model.Document
import finsky.api.model.FilterComposite
import finsky.api.model.FilterPredicate
import info.anodsplace.playstore.AppDetailsFilter
import info.anodsplace.playstore.AppNameFilter
import info.anodsplace.playstore.WishListEndpoint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class WishListState(
        val nameFilter: String = "",
        val watchingPackages: List<String> = emptyList(),
)

sealed interface WishListAction {
    class AppStateChanged(val newStatus: Int, val info: AppInfo) : WishListAction
}

sealed interface WishListEvent {
    class Delete(val document: Document) : WishListEvent
    class ItemClick(val document: Document) : WishListEvent
}

class WishListViewModel() : BaseFlowViewModel<WishListState, WishListEvent, WishListAction>(), KoinComponent {
    private val database: AppsDatabase by inject()
    private val endpoint: WishListEndpoint by inject()
    private val uploadDateParserCache: UploadDateParserCache by inject()

    val packages: StateFlow<List<String>> = database.apps().observePackages().map { list ->
        list.map { it.packageName }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
            emitAction(WishListAction.AppStateChanged(newStatus = AppInfoMetadata.STATUS_DELETED, info = info))
        }
    }

    private fun add(document: Document) {
        viewModelScope.launch {
            val info = AppInfo(document, uploadDateParserCache)
            val result = AppListTable.Queries.insertSafetly(info, database)
            if (result != AppListTable.ERROR_INSERT) {
                emitAction(WishListAction.AppStateChanged(newStatus = AppInfoMetadata.STATUS_NORMAL, info = info))
            }
        }
    }

    fun load() = Pager(PagingConfig(pageSize = 10)) { ListEndpointPagingSource(endpoint) }
            .flow
            .map { pageData ->
                val predicate = predicate(viewState.nameFilter)
                pageData.filter { d -> predicate(d) }
            }
            .cachedIn(viewModelScope)

    override fun handleEvent(event: WishListEvent) {
        when (event) {
            is WishListEvent.Delete -> {}
            is WishListEvent.ItemClick -> {}
        }
    }

}