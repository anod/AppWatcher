package com.anod.appwatcher.watchlist

import android.app.Application
import android.arch.lifecycle.*
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.SparseArray
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.*
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext

/**
 * @author Alex Gavrishev
 * @date 13/04/2018
 */

typealias AppsList = List<AppListItem>

private class AppsUpdateObserver(private val viewModel: WatchListViewModel) : ContentObserver(Handler()) {

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        uri?.let {
            AppLog.d("onChange: $it")
            viewModel.appsDbChanged.value = true
        }
    }
}

open class LoadResult(val appsList: AppsList, val sections: SparseArray<SectionHeader>)

open class WatchListViewModel(application: Application): AndroidViewModel(application) {

    val context: ApplicationContext
        get() = ApplicationContext(getApplication())

    val appsDbChanged = MutableLiveData<Boolean>()
    var showRecentlyUpdated = false

    private val observer = AppsUpdateObserver(this)
    init {
        application.contentResolver.registerContentObserver(DbContentProvider.appsUri, false, observer)
    }

    override fun onCleared() {
        getApplication<AppWatcherApplication>().contentResolver.unregisterContentObserver(observer)
    }

    var titleFilter = ""
    var sortId = 0
    var filter: AppListFilter = AppListFilter.None()
    var tag: Tag? = null
    val sections: MutableLiveData<SparseArray<SectionHeader>> = MutableLiveData()

    fun init(sortId: Int, tag: Tag?, listFilter: AppListFilter, prefs: Preferences) {
        this.sortId = sortId
        this.tag = tag
        this.filter = listFilter
        this.showRecentlyUpdated = prefs.showRecentlyUpdated
    }

    open fun load(): LiveData<LoadResult> {
        val liveData = loadApps()
        return Transformations.map(liveData, {
            val sections = SectionHeaderFactory(showRecentlyUpdated, false, false)
                    .create(it?.size ?: 0,
                            filter.newCount,
                            filter.recentlyUpdatedCount,
                            filter.updatableNewCount,
                            false,
                            false)
            this@WatchListViewModel.sections.value = sections
            LoadResult(it ?: emptyList(), sections)
        })
    }

    fun loadApps(): LiveData<List<AppListItem>> {
        val appsTable = com.anod.appwatcher.Application.provide(context).database.apps()
        val list = AppListTable.Queries.loadAppList(sortId, tag, titleFilter, appsTable)
        return Transformations.map(list, { allApps ->
            filter.resetNewCount()
            val items = allApps ?: emptyList()
            val filtered = items.filter { appItem -> !filter.filterRecord(appItem) }
            filtered
        })
    }

}