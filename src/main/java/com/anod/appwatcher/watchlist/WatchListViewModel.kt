package com.anod.appwatcher.watchlist

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.util.SparseArray
import androidx.core.util.set
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.model.*

/**
 * @author algavris
 * @date 13/04/2018
 */
open class WatchListViewModel(application: Application): AndroidViewModel(application) {

    val appList: MutableLiveData<List<AppInfo>> = MutableLiveData()
    val sections: MutableLiveData<SparseArray<SectionHeader>> = MutableLiveData()
    val updatable = MutableLiveData<Boolean>()
    var showRecentlyUpdated = false

    open fun load(titleFilter: String, sortId: Int, filter: AppListFilter, tag: Tag?) {
        AppListAsyncTask(getApplication<AppWatcherApplication>(), titleFilter, sortId, filter, tag, {
            list, listFilter -> this.setValues(list, listFilter)
        }).execute()
    }

    fun setValues(list: List<AppInfo>, listFilter: AppListFilter) {
        appList.value = list
        sections.value = this.createHeader(list.size, listFilter.newCount, listFilter.recentlyUpdatedCount)
        if (listFilter.updatableNewCount > 0) {
            updatable.value = true
        }
    }

    protected open fun createHeader(totalAppsCount: Int, newAppsCount: Int, recentlyUpdatedCount: Int): SparseArray<SectionHeader> {
        val sections = SparseArray<SectionHeader>()
        if (newAppsCount > 0) {
            sections[0] = New(newAppsCount)
        }
        val effectiveRecentlyUpdatedCount  = if (showRecentlyUpdated) recentlyUpdatedCount else 0
        if (effectiveRecentlyUpdatedCount > 0) {
            sections[newAppsCount] = RecentlyUpdated(effectiveRecentlyUpdatedCount)
        }
        sections[effectiveRecentlyUpdatedCount + newAppsCount] = Watching(totalAppsCount - effectiveRecentlyUpdatedCount - newAppsCount)
        return sections
    }
}