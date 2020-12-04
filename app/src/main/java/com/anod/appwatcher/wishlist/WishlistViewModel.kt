package com.anod.appwatcher.wishlist

import android.accounts.Account
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.filter
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.search.ListEndpointPagingSource
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

class WishListViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context
        get() = getApplication<AppWatcherApplication>()
    private val provide: AppComponent
        get() = com.anod.appwatcher.Application.provide(context)

    var appStatusChange = MutableLiveData<Pair<Int, AppInfo?>>()

    private var endpoint: WishListEndpoint? = null

    val packages: StateFlow<List<String>> = provide.database.apps().observePackages().map { list ->
        list.map { it.packageName }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun init(account: Account, authToken: String) {
        endpoint = WishListEndpoint(context, provide.requestQueue, provide.deviceInfo, account).also {
            it.authToken = authToken
        }
    }

    var nameFilter = ""

    private fun predicate(nameFilter: String): FilterPredicate {
        if (nameFilter.isBlank()) {
            return AppDetailsFilter.predicate
        }
        return FilterComposite(listOf(
                AppDetailsFilter.predicate,
                AppNameFilter(nameFilter).predicate
        )).predicate
    }

    fun delete(info: AppInfo) {
        viewModelScope.launch {
            AppListTable.Queries.delete(info.appId, provide.database)
            appStatusChange.value = Pair(AppInfoMetadata.STATUS_DELETED, info)
        }
    }

    fun add(info: AppInfo) {
        viewModelScope.launch {
            AppListTable.Queries.insert(info, provide.database)
            appStatusChange.value = Pair(AppInfoMetadata.STATUS_NORMAL, info)
        }
    }

    fun load() = Pager(PagingConfig(pageSize = 10)) { ListEndpointPagingSource(endpoint!!) }
            .flow
            .map { pageData ->
                val predicate = predicate(nameFilter)
                pageData.filter { d -> predicate(d) }
            }
            .cachedIn(viewModelScope)

}