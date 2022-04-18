package com.anod.appwatcher.search

import android.accounts.Account
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.utils.networkConnection
import com.anod.appwatcher.utils.prefs
import finsky.api.model.Document
import info.anodsplace.playstore.AppDetailsFilter
import info.anodsplace.playstore.DetailsEndpoint
import info.anodsplace.playstore.SearchEndpoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

sealed class SearchStatus
object Loading : SearchStatus()
class DetailsAvailable(val document: Document) : SearchStatus()
object NoResults : SearchStatus()
object NoNetwork : SearchStatus()
object Error : SearchStatus()
class SearchPage(val pagingData: PagingData<Document>) : SearchStatus()

sealed class ResultAction
class Delete(val info: AppInfo) : ResultAction()
class Add(val info: AppInfo) : ResultAction()

class SearchViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    private val database: AppsDatabase by inject()
    val authToken: AuthTokenBlocking by inject()
    val account: Account?
        get() = prefs.account

    private var initiateSearch = false
    var isShareSource = false
    var hasFocus = false
    private var isPackageSearch = false
    var searchQuery = MutableStateFlow("")
    val searchQueryAuthenticated = searchQuery.combine(authToken.tokenAvailable) { query, _ -> query }
    val packages: StateFlow<List<String>> = database.apps().observePackages().map { list ->
        list.map { it.packageName }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var appStatusChange = MutableStateFlow<Pair<Int, AppInfo?>>(Pair(-1, null))

    private var endpointDetails: DetailsEndpoint? = null
    private var endpointSearch: SearchEndpoint? = null

    override fun onCleared() {
        endpointDetails = null
        endpointSearch = null
    }

    fun initFromIntent(intent: Intent?) {
        if (intent == null) {
            searchQuery.value = ""
            return
        }
        searchQuery.value = intent.getStringExtra(SearchActivity.EXTRA_KEYWORD) ?: ""
        isPackageSearch = intent.getBooleanExtra(SearchActivity.EXTRA_PACKAGE, false)
        initiateSearch = intent.getBooleanExtra(SearchActivity.EXTRA_EXACT, false)
        isShareSource = intent.getBooleanExtra(SearchActivity.EXTRA_SHARE, false)
        hasFocus = intent.getBooleanExtra(SearchActivity.EXTRA_FOCUS, false)
    }

    fun search(query: String): Flow<SearchStatus> = flow {
        if (account == null) {
            emit(Error)
            return@flow
        }
        endpointSearch = get() { parametersOf(query) }
        if (isPackageSearch) {
            val detailsUrl = AppInfo.createDetailsUrl(query)
            endpointDetails = get() { parametersOf(detailsUrl) }
        }
        emit(Loading)
        if (endpointDetails == null) {
            emitAll(createPager(endpointSearch!!))
        } else {
            try {
                val model = endpointDetails!!.start()
                if (model.document != null) {
                    emit(DetailsAvailable(model.document!!))
                } else {
                    emitAll(createPager(endpointSearch!!))
                }
            } catch (e: Exception) {
                if (!networkConnection.isNetworkAvailable) {
                    emit(NoNetwork)
                } else {
                    emitAll(createPager(endpointSearch!!))
                }
            }
        }
    }

    private fun createPager(endpointSearch: SearchEndpoint) = Pager(PagingConfig(pageSize = 10)) { ListEndpointPagingSource(endpointSearch) }
            .flow
            .cachedIn(viewModelScope)
            .map {
                val filtered = it.filter { d -> AppDetailsFilter.predicate(d) }
                SearchPage(filtered)
            }

    fun delete(info: AppInfo) {
        viewModelScope.launch {
            AppListTable.Queries.delete(info.appId, database)
            appStatusChange.value = Pair(AppInfoMetadata.STATUS_DELETED, info)
        }
    }

    fun add(info: AppInfo) {
        viewModelScope.launch {
            val result = AppListTable.Queries.insertSafetly(info, database)
            if (result != AppListTable.ERROR_INSERT) {
                appStatusChange.value = Pair(AppInfoMetadata.STATUS_NORMAL, info)
            }
        }
    }
}