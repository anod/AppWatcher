package com.anod.appwatcher.wishlist

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.search.ResultsViewModel
import com.anod.appwatcher.utils.map

class WishlistViewModel(application: Application): AndroidViewModel(application), ResultsViewModel {
    private val context: Context
        get() = getApplication<AppWatcherApplication>()
    private val provide: AppComponent
        get() =  com.anod.appwatcher.Application.provide(context)

    var appStatusChange = MutableLiveData<Pair<Int, AppInfo?>>()

    override val packages = provide.database.apps().observePackages().map { list ->
        list.map { it.packageName }
    }

    override fun delete(info: AppInfo) {
        AppListTable.Queries.delete(info.appId, provide.database)
        appStatusChange.value = Pair(AppInfoMetadata.STATUS_DELETED, info)
    }

    override fun add(info: AppInfo) {
        AppListTable.Queries.insert(info, provide.database)
        appStatusChange.value = Pair(AppInfoMetadata.STATUS_NORMAL, info)
    }
}