package com.anod.appwatcher.search

import android.accounts.Account
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.*
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.utils.networkConnection
import finsky.api.model.Document
import info.anodsplace.framework.app.HingeDeviceLayout
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.playstore.AppDetailsFilter
import info.anodsplace.playstore.DetailsEndpoint
import info.anodsplace.playstore.SearchEndpoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

sealed interface SearchStatus {
    object Loading : SearchStatus
    data class DetailsAvailable(val document: Document) : SearchStatus
    data class NoResults(val query: String) : SearchStatus
    data class NoNetwork(val query: String) : SearchStatus
    data class Error(val query: String) : SearchStatus
    data class SearchList(val query: String) : SearchStatus
}

sealed interface SearchViewAction {
    class StartActivity(val intent: Intent, val finish: Boolean) : SearchViewAction
    class ShowToast(val resId: Int = 0, val duration: Int = Toast.LENGTH_LONG, val finish: Boolean = false, val text: String = "") : SearchViewAction
    object ShowAccountDialog : SearchViewAction
    class ShowTagSnackbar(val info: AppInfo, val isShareSource: Boolean) : SearchViewAction
    class AlreadyWatchedNotice(val document: Document) : SearchViewAction
    object OnBackPressed : SearchViewAction
}


sealed interface SearchViewEvent {
    object NoAccount : SearchViewEvent
    object OnBackPressed : SearchViewEvent
    class Delete(val document: Document) : SearchViewEvent
    class ItemClick(val document: Document) : SearchViewEvent
    class SetWideLayout(val wideLayout: HingeDeviceLayout) : SearchViewEvent
    class SearchQueryChange(val query: String) : SearchViewEvent
    class OnSearchEnter(val query: String) : SearchViewEvent
    class AccountSelectError(val errorMessage: String) : SearchViewEvent
    class AccountSelected(val account: Account) : SearchViewEvent
}

data class SearchViewState(
        val searchQuery: String = "",
        val isShareSource: Boolean = false,
        val hasFocus: Boolean = false,
        val initiateSearch: Boolean = false,
        val isPackageSearch: Boolean = false,
        val authenticated: Boolean = false,
        val account: Account? = null,
        val searchStatus: SearchStatus = SearchStatus.Loading,
        val watchingPackages: List<String> = emptyList(),
        val wideLayout: HingeDeviceLayout = HingeDeviceLayout(),
)

class SearchViewModel(
        initialState: SearchViewState
) : BaseFlowViewModel<SearchViewState, SearchViewEvent, SearchViewAction>(), KoinComponent {

    class Factory(private val initialState: SearchViewState) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return SearchViewModel(initialState) as T
        }
    }

    private val database: AppsDatabase by inject()
    private val authToken: AuthTokenBlocking by inject()
    private val uploadDateParserCache: UploadDateParserCache by inject()
    private val packageManager: PackageManager by inject()
    val installedApps by lazy { InstalledApps.MemoryCache(InstalledApps.PackageManager(packageManager)) }
    val prefs: Preferences by inject()

    private var endpointDetails: DetailsEndpoint? = null
    private var endpointSearch: SearchEndpoint? = null
    private var searchJob: Job? = null

    override fun onCleared() {
        endpointDetails = null
        endpointSearch = null
    }

    init {
        viewState = initialState.copy(
                account = prefs.account,
                searchStatus = if (initialState.searchQuery.isNotEmpty() && initialState.initiateSearch) SearchStatus.Loading else SearchStatus.NoResults(query = "")
        )
        if (prefs.account == null) {
            handleEvent(SearchViewEvent.NoAccount)
        }
        viewModelScope.launch {
            database.apps().observePackages().collect { list ->
                viewState = viewState.copy(watchingPackages = list.map { it.packageName })
            }
        }
        if (initialState.searchQuery.isNotEmpty() && initialState.initiateSearch) {
            handleEvent(SearchViewEvent.OnSearchEnter(initialState.searchQuery))
        }
    }

    override fun handleEvent(event: SearchViewEvent) {
        when (event) {
            is SearchViewEvent.ItemClick -> {
                if (viewState.watchingPackages.contains(event.document.appDetails.packageName)) {
                    emitAction(SearchViewAction.AlreadyWatchedNotice(event.document))
                } else add(event.document)
            }
            is SearchViewEvent.Delete -> delete(event.document)
            SearchViewEvent.NoAccount -> emitAction(SearchViewAction.ShowAccountDialog)
            is SearchViewEvent.SetWideLayout -> viewState = viewState.copy(wideLayout = event.wideLayout)
            is SearchViewEvent.SearchQueryChange -> viewState = viewState.copy(searchQuery = event.query)
            is SearchViewEvent.OnSearchEnter -> onSearchRequest(event.query)
            is SearchViewEvent.AccountSelectError -> onAccountSelectError(event.errorMessage)
            is SearchViewEvent.AccountSelected -> onAccountSelected(event.account)
            SearchViewEvent.OnBackPressed -> emitAction(SearchViewAction.OnBackPressed)
        }
    }

    private fun onAccountSelectError(errorMessage: String) {
        if (networkConnection.isNetworkAvailable) {
            if (errorMessage.isNotBlank()) {
                emitAction(SearchViewAction.ShowToast(text = errorMessage, duration = Toast.LENGTH_LONG, finish = true))
            } else {
                emitAction(SearchViewAction.ShowToast(resId = R.string.failed_gain_access, duration = Toast.LENGTH_LONG, finish = true))
            }
        } else {
            emitAction(SearchViewAction.ShowToast(resId = R.string.check_connection, duration = Toast.LENGTH_SHORT, finish = true))
        }
    }

    private fun onSearchRequest(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            search(query).collect { searchStatus ->
                viewState = viewState.copy(searchQuery = query, searchStatus = searchStatus)
                if (searchStatus is SearchStatus.NoNetwork) {
                    emitAction(SearchViewAction.ShowToast(resId = R.string.check_connection, duration = Toast.LENGTH_SHORT, finish = true))
                }
            }
        }
    }

    private fun search(query: String): Flow<SearchStatus> = flow {
        if (prefs.account == null) {
            emit(SearchStatus.Error(query = query))
            return@flow
        }
        endpointSearch = get { parametersOf(query) }
        if (viewState.isPackageSearch) {
            val detailsUrl = AppInfo.createDetailsUrl(query)
            endpointDetails = get { parametersOf(detailsUrl) }
        }
        emit(SearchStatus.Loading)
        if (endpointDetails == null) {
            _pagingData = null
            emit(SearchStatus.SearchList(query = query))
        } else {
            try {
                val model = endpointDetails!!.start()
                if (model.document != null) {
                    emit(SearchStatus.DetailsAvailable(model.document!!))
                } else {
                    _pagingData = null
                    emit(SearchStatus.SearchList(query = query))
                }
            } catch (e: Exception) {
                if (!networkConnection.isNetworkAvailable) {
                    emit(SearchStatus.NoNetwork(query = query))
                } else {
                    _pagingData = null
                    emit(SearchStatus.SearchList(query = query))
                }
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

    private fun createPager() = Pager(PagingConfig(pageSize = 10)) { ListEndpointPagingSource(endpointSearch!!) }
            .flow
            .cachedIn(viewModelScope)
            .map {
                it.filter { d -> AppDetailsFilter.predicate(d) }
            }

    private fun onAccountSelected(account: Account) {
        viewState = viewState.copy(account = account)
        viewModelScope.launch {
            try {
                if (!authToken.refreshToken(account)) {
                    if (networkConnection.isNetworkAvailable) {
                        emitAction(SearchViewAction.ShowToast(resId = R.string.failed_gain_access, duration = Toast.LENGTH_LONG, finish = true))
                    } else {
                        emitAction(SearchViewAction.ShowToast(resId = R.string.check_connection, duration = Toast.LENGTH_SHORT, finish = true))
                    }
                }
            } catch (e: AuthTokenStartIntent) {
                emitAction(SearchViewAction.StartActivity(intent = e.intent, finish = true))
            }
        }
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
                emitAction(SearchViewAction.ShowTagSnackbar(info = info, isShareSource = viewState.isShareSource))
            }
        }
    }

}