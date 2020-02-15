package com.anod.appwatcher.wishlist

import android.accounts.Account
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.search.ResultsViewModel
import finsky.api.model.DfeList
import info.anodsplace.playstore.WishListEndpoint
import kotlinx.coroutines.launch

class WishListViewModel(application: Application) : AndroidViewModel(application), ResultsViewModel {
    private val context: Context
        get() = getApplication<AppWatcherApplication>()
    private val provide: AppComponent
        get() = com.anod.appwatcher.Application.provide(context)

    var appStatusChange = MutableLiveData<Pair<Int, AppInfo?>>()
    val listData = MutableLiveData<DfeList>()
    val loading = MutableLiveData<Boolean>()

    private var endpoint: WishListEndpoint? = null

    override val packages = provide.database.apps().observePackages().map { list ->
        list.map { it.packageName }
    }

    fun init(account: Account, authToken: String) {
        endpoint = WishListEndpoint(context, provide.requestQueue, provide.deviceInfo, account, true).also {
            it.authToken = authToken
        }
        run()
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

    fun retry() {
        run()
    }

    fun filter(query: String) {
        endpoint!!.nameFilter = query
        run()
    }

    private fun run() {
        loading.value = false
        viewModelScope.launch {
            try {
                val model = endpoint!!.start()
                listData.value = model
            } catch (e: Exception) {
                loading.value = true
            }
        }
    }
}