package com.anod.appwatcher.wishlist

import android.accounts.Account
import android.app.Application
import android.content.Context
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
import com.anod.appwatcher.search.ResultsViewModel
import finsky.api.model.DfeList
import finsky.api.model.DfeModel
import info.anodsplace.playstore.PlayStoreEndpoint
import info.anodsplace.playstore.WishListEndpoint
import kotlinx.coroutines.launch

class WishListViewModel(application: Application) : AndroidViewModel(application), ResultsViewModel, PlayStoreEndpoint.Listener {
    private val context: Context
        get() = getApplication<AppWatcherApplication>()
    private val provide: AppComponent
        get() = com.anod.appwatcher.Application.provide(context)

    var appStatusChange = MutableLiveData<Pair<Int, AppInfo?>>()
    val listData = MutableLiveData<DfeList>()
    val loading = MutableLiveData<Boolean>()

    private var endpoint: WishListEndpoint? = null

    override fun onCleared() {
        endpoint?.listener = null
        super.onCleared()
    }

    override val packages = provide.database.apps().observePackages().map { list ->
        list.map { it.packageName }
    }

    fun init(account: Account, authToken: String) {
        loading.value = false
        endpoint = WishListEndpoint(context, provide.requestQueue, provide.deviceInfo, account, true).also {
            it.authToken = authToken
            it.listener = this
            it.startAsync()
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
            AppListTable.Queries.insert(info, provide.database)
            appStatusChange.value = Pair(AppInfoMetadata.STATUS_NORMAL, info)
        }
    }

    override fun onDataChanged(data: DfeModel) {
        listData.value = endpoint!!.listData
    }

    override fun onErrorResponse(error: VolleyError) {
        loading.value = true
    }

    fun retry() {
        loading.value = false
        endpoint?.startAsync()
    }

    fun filter(query: String) {
        loading.value = false
        endpoint!!.nameFilter = query
        endpoint!!.startAsync()
    }
}