package com.anod.appwatcher.search

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.accounts.AuthTokenStartIntent
import com.anod.appwatcher.compose.CommonActivityAction
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.BaseFlowViewModel
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.utils.networkConnection
import finsky.api.DfeApi
import finsky.api.Document
import finsky.api.toDocument
import info.anodsplace.framework.app.HingeDeviceLayout
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.playstore.AppDetailsFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed interface SearchStatus {
    object Loading : SearchStatus
    data class DetailsAvailable(val app: App) : SearchStatus
    data class NoResults(val query: String) : SearchStatus
    data class NoNetwork(val query: String) : SearchStatus
    data class Error(val query: String) : SearchStatus
    data class SearchList(val query: String) : SearchStatus
}

sealed interface SearchViewAction {
    class ActivityAction(val action: CommonActivityAction) : SearchViewAction
    class ShowSnackbar(val message: String, val duration: SnackbarDuration = SnackbarDuration.Short, val finish: Boolean = false) : SearchViewAction
    object ShowAccountDialog : SearchViewAction
    class ShowTagSnackbar(val info: App, val isShareSource: Boolean) : SearchViewAction
    class AlreadyWatchedNotice(val document: Document) : SearchViewAction
}

private fun startActivityAction(intent: Intent, finish: Boolean = false) : SearchViewAction.ActivityAction {
    return SearchViewAction.ActivityAction(
        action = CommonActivityAction.StartActivity(
            intent = intent,
            finish = finish
        )
    )
}

sealed interface SearchViewEvent {
    object NoAccount : SearchViewEvent
    object OnBackPressed : SearchViewEvent
    class SetWideLayout(val wideLayout: HingeDeviceLayout) : SearchViewEvent
    class SearchQueryChange(val query: String) : SearchViewEvent
    class OnSearchEnter(val query: String) : SearchViewEvent
    class AccountSelectError(val errorMessage: String) : SearchViewEvent
    class AccountSelected(val account: Account) : SearchViewEvent
    class SelectApp(val app: App?) : SearchViewEvent
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
    val selectedApp: App? = null
)

class SearchViewModel(
        initialState: SearchViewState
) : BaseFlowViewModel<SearchViewState, SearchViewEvent, SearchViewAction>(), KoinComponent {

    class Factory(private val initialState: SearchViewState) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return SearchViewModel(initialState) as T
        }
    }

    private val context: Context by inject()
    private val database: AppsDatabase by inject()
    private val authToken: AuthTokenBlocking by inject()
    private val uploadDateParserCache: UploadDateParserCache by inject()
    private val packageManager: PackageManager by inject()
    val installedApps by lazy { InstalledApps.MemoryCache(InstalledApps.PackageManager(packageManager)) }
    val prefs: Preferences by inject()

    private val dfeApi: DfeApi by inject()

    private var searchJob: Job? = null

    override fun onCleared() {
        searchJob?.cancel()
    }

    init {
        viewState = initialState.copy(
            account = prefs.account,
            searchStatus = if (initialState.searchQuery.isNotEmpty() && initialState.initiateSearch) SearchStatus.Loading else SearchStatus.NoResults(query = ""),
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
            SearchViewEvent.NoAccount -> emitAction(SearchViewAction.ShowAccountDialog)
            is SearchViewEvent.SetWideLayout -> viewState = viewState.copy(wideLayout = event.wideLayout)
            is SearchViewEvent.SearchQueryChange -> viewState = viewState.copy(searchQuery = event.query)
            is SearchViewEvent.OnSearchEnter -> onSearchRequest(event.query)
            is SearchViewEvent.AccountSelectError -> onAccountSelectError(event.errorMessage)
            is SearchViewEvent.AccountSelected -> onAccountSelected(event.account)
            SearchViewEvent.OnBackPressed -> onBackPressed()
            is SearchViewEvent.SelectApp -> {
                viewState = viewState.copy(selectedApp = event.app)
            }
        }
    }

    private fun onBackPressed() {
        emitAction(SearchViewAction.ActivityAction(CommonActivityAction.Finish))
    }

    private fun onAccountSelectError(errorMessage: String) {
        if (networkConnection.isNetworkAvailable) {
            if (errorMessage.isNotBlank()) {
                emitAction(SearchViewAction.ShowSnackbar(message = errorMessage, duration = SnackbarDuration.Short, finish = true))
            } else {
                emitAction(SearchViewAction.ShowSnackbar(message = context.getString(R.string.failed_gain_access), duration = SnackbarDuration.Long, finish = true))
            }
        } else {
            emitAction(SearchViewAction.ShowSnackbar(message = context.getString(R.string.check_connection), duration = SnackbarDuration.Short, finish = true))
        }
    }

    private fun onSearchRequest(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            viewState = viewState.copy(searchQuery = query)
            search(query).collect { searchStatus ->
                viewState = viewState.copy(searchStatus = searchStatus)
                if (searchStatus is SearchStatus.NoNetwork) {
                    emitAction(SearchViewAction.ShowSnackbar(message = context.getString(R.string.check_connection), duration = SnackbarDuration.Short, finish = true))
                } else if (searchStatus is SearchStatus.Error) {
                    emitAction(SearchViewAction.ShowSnackbar(message = context.getString(R.string.error_fetching_info), duration = SnackbarDuration.Short, finish = true))
                }
            }
        }
    }

    private fun search(query: String): Flow<SearchStatus> = flow {
        if (prefs.account == null) {
            emit(SearchStatus.Error(query = query))
            return@flow
        }
        if (!authToken.isFresh) {
            if (!authToken.refreshToken(prefs.account!!)) {
                if (!networkConnection.isNetworkAvailable) {
                    emit(SearchStatus.NoNetwork(query = query))
                } else {
                    emit(SearchStatus.Error(query = query))
                }
                return@flow
            }
        }
        emit(SearchStatus.Loading)
        if (viewState.isPackageSearch) {
            try {
                val detailsUrl = App.createDetailsUrl(query)
                val document = dfeApi.details(detailsUrl).toDocument()
                if (document != null) {
                    emit(SearchStatus.DetailsAvailable(app = App(document, uploadDateParserCache)))
                } else {
                    resetPager()
                    emit(SearchStatus.SearchList(query = query))
                }
            } catch (e: Exception) {
                if (!networkConnection.isNetworkAvailable) {
                    emit(SearchStatus.NoNetwork(query = query))
                } else {
                    resetPager()
                    emit(SearchStatus.SearchList(query = query))
                }
            }
        } else {
            resetPager()
            emit(SearchStatus.SearchList(query = query))
        }
    }

    private var _pagingData: Flow<PagingData<App>>? = null
    val pagingData: Flow<PagingData<App>>
        get() {
            if (_pagingData == null) {
                _pagingData = createPager()
            }
            return _pagingData!!
        }

    private fun resetPager() {
        // Trigger new pager creation
        _pagingData = null
    }

    private fun createPager() = Pager(PagingConfig(pageSize = 10)) {
        SearchEndpointPagingSource(dfeApi, viewState.searchQuery)
    }
    .flow
    .cachedIn(viewModelScope)
    .map {
        it
            .filter { d -> AppDetailsFilter.predicate(d) }
            .map { d -> App(d, uploadDateParserCache) }
    }

    private fun onAccountSelected(account: Account) {
        viewState = viewState.copy(account = account)
        viewModelScope.launch {
            try {
                if (!authToken.refreshToken(account)) {
                    if (networkConnection.isNetworkAvailable) {
                        emitAction(SearchViewAction.ShowSnackbar(message = context.getString(R.string.failed_gain_access), duration = SnackbarDuration.Long, finish = true))
                    } else {
                        emitAction(SearchViewAction.ShowSnackbar(message = context.getString(R.string.check_connection), duration = SnackbarDuration.Short, finish = true))
                    }
                }
            } catch (e: AuthTokenStartIntent) {
                emitAction(startActivityAction(intent = e.intent, finish = true))
            }
        }
    }
}