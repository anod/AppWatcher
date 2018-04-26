package com.anod.appwatcher.watchlist

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.database.ContentObserver
import android.os.Handler
import android.util.SparseArray
import androidx.core.util.set
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.model.*

/**
 * @author algavris
 * @date 13/04/2018
 */

private class AppsUpdateObserver(private val viewModel: WatchListViewModel) : ContentObserver(Handler()) {
    override fun onChange(selfChange: Boolean) {
        viewModel.reload()
    }
}

open class WatchListViewModel(application: Application): AndroidViewModel(application) {

    val appList: MutableLiveData<List<AppInfo>> = MutableLiveData()
    val sections: MutableLiveData<SparseArray<SectionHeader>> = MutableLiveData()
    var showRecentlyUpdated = false

    private val observer = AppsUpdateObserver(this)
    init {
        application.contentResolver.registerContentObserver(DbContentProvider.appsUri, true, observer)

    }

    override fun onCleared() {
        getApplication<AppWatcherApplication>().contentResolver.unregisterContentObserver(observer)
    }

    private var titleFilter = ""
    private var sortId = 0
    private var filter: AppListFilter = AppListFilter.None()
    private var tag: Tag? = null

    fun reload() {
        load(titleFilter, sortId, filter, tag)
    }

    open fun load(titleFilter: String, sortId: Int, filter: AppListFilter, tag: Tag?) {
        this.titleFilter = titleFilter
        this.sortId = sortId
        this.filter = filter
        this.tag = tag

        AppListAsyncTask(getApplication<AppWatcherApplication>(), titleFilter, sortId, filter, tag, { list, listFilter ->
            this.setValues(list, listFilter)
        }).execute()
    }

    fun setValues(list: List<AppInfo>, listFilter: AppListFilter) {
        appList.value = list
        sections.value = this.createHeader(list.size, listFilter.newCount, listFilter.recentlyUpdatedCount, listFilter.updatableNewCount)
    }

    protected open fun createHeader(totalAppsCount: Int, newAppsCount: Int, recentlyUpdatedCount: Int, updatableNewCount: Int): SparseArray<SectionHeader> {
        val sections = SparseArray<SectionHeader>()
        if (newAppsCount > 0) {
            sections[0] = New(newAppsCount, updatableNewCount)
        }
        val effectiveRecentlyUpdatedCount  = if (showRecentlyUpdated) recentlyUpdatedCount else 0
        if (effectiveRecentlyUpdatedCount > 0) {
            sections[newAppsCount] = RecentlyUpdated(effectiveRecentlyUpdatedCount)
        }
        sections[effectiveRecentlyUpdatedCount + newAppsCount] = Watching(totalAppsCount - effectiveRecentlyUpdatedCount - newAppsCount)
        return sections
    }

}