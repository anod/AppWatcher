package com.anod.appwatcher.search

import android.accounts.Account
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.android.volley.VolleyError
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.utils.combineLatest
import info.anodsplace.framework.os.BackgroundTask
import info.anodsplace.playstore.*
import kotlinx.coroutines.launch

sealed class SearchStatus
class Loading(val isPackageSearch: Boolean) : SearchStatus()
object Available : SearchStatus()
object NoResults : SearchStatus()
object NoNetwork : SearchStatus()
object Error : SearchStatus()
object FreeTextRequest : SearchStatus()

class SearchViewModel(application: Application): AndroidViewModel(application), CompositeStateEndpoint.Listener, ResultsViewModel {
    private val context: Context
        get() = getApplication<AppWatcherApplication>()
    private val provide: AppComponent
        get() =  com.anod.appwatcher.Application.provide(context)

    var account: Account? = null
    var initiateSearch = false
    var isShareSource = false
    var hasFocus = false
    var isPackageSearch = false
    var searchQuery = MutableLiveData<String>()
    var status = MutableLiveData<SearchStatus>()
    var authToken = MutableLiveData<String>()
    val searchQueryAuthenticated = searchQuery.combineLatest(authToken)
    override val packages = provide.database.apps().observePackages().map { list ->
        list.map { it.packageName }
    }
    var appStatusChange = MutableLiveData<Pair<Int, AppInfo?>>()

    private var endpoints: CompositeStateEndpoint? = CompositeStateEndpoint(this)

    val endpointDetails: DetailsEndpoint
        get() =  endpoints!![DETAILS_ENDPOINT_ID] as DetailsEndpoint

    val endpointSearch: SearchEndpoint
        get() =  endpoints!![SEARCH_ENDPOINT_ID] as SearchEndpoint

    override fun onCleared() {
        endpoints = null
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

    fun search(query: String, authToken: String) {
        val requestQueue = provide.requestQueue
        val deviceInfo = provide.deviceInfo
        if (isPackageSearch) {
            val detailsUrl = AppInfo.createDetailsUrl(query)
            endpoints!!.put(DETAILS_ENDPOINT_ID, DetailsEndpoint(context, requestQueue, deviceInfo, account!!, detailsUrl))
        }
        endpoints!!.put(SEARCH_ENDPOINT_ID, SearchEndpoint(context, requestQueue, deviceInfo, account!!, query, true))

        if (isPackageSearch) {
            endpoints!!.activeId = DETAILS_ENDPOINT_ID
        } else {
            endpoints!!.activeId = SEARCH_ENDPOINT_ID
        }
        status.value = Loading(isPackageSearch)
        endpoints!!.authToken = authToken
        endpoints!!.reset()
        endpoints!!.active.startAsync()
    }

    override fun onDataChanged(id: Int, endpoint: PlayStoreEndpointBase) {
        if (id == DETAILS_ENDPOINT_ID) {
            if ((endpoint as DetailsEndpoint).document != null) {
                status.value = Available
            } else {
                status.value = FreeTextRequest
                endpoints!!.activate(SEARCH_ENDPOINT_ID).startAsync()
            }
        } else {
            val searchEndpoint = endpoint as SearchEndpoint
            if (searchEndpoint.count == 0) {
                status.value = NoResults
            } else {
                status.value = Available
            }
        }
        isPackageSearch = false
    }

    override fun onErrorResponse(id: Int, endpoint: PlayStoreEndpointBase, error: VolleyError) {
        if (!provide.networkConnection.isNetworkAvailable) {
            status.value = NoNetwork
            return
        }
        if (id == DETAILS_ENDPOINT_ID) {
            status.value = FreeTextRequest
            endpoints!!.activate(SEARCH_ENDPOINT_ID).startAsync()
        } else {
            status.value = Error
        }
    }

    override fun delete(info: AppInfo) {
        viewModelScope.launch {
            AppListTable.Queries.delete(info.appId, provide.database)
            appStatusChange.value = Pair(AppInfoMetadata.STATUS_DELETED, info)
        }
    }

    override fun add(info: AppInfo) {
        viewModelScope.launch {
            val result = AppListTable.Queries.insertSafetly(info, provide.database)
            if (result != AppListTable.ERROR_INSERT) {
                appStatusChange.value = Pair(AppInfoMetadata.STATUS_NORMAL, info)
            }
        }
    }

    companion object {
        private const val DETAILS_ENDPOINT_ID = 0
        private const val SEARCH_ENDPOINT_ID = 1
    }
}