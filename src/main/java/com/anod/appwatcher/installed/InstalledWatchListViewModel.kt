package com.anod.appwatcher.installed

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.util.SparseArray
import androidx.core.util.set
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.watchlist.RecentlyInstalled
import com.anod.appwatcher.watchlist.SectionHeader
import com.anod.appwatcher.watchlist.WatchListViewModel

/**
 * @author algavris
 * @date 14/04/2018
 */

class InstalledWatchListViewModel(application: Application) : WatchListViewModel(application) {

    val installedPackages: MutableLiveData<List<String>> = MutableLiveData()
    val recentlyInstalled: MutableLiveData<List<PackageRowPair>> = MutableLiveData()

    override fun load(titleFilter: String, sortId: Int, filter: AppListFilter, tag: Tag?) {
        val application = getApplication<AppWatcherApplication>()
        InstalledAsyncTask(application, titleFilter, sortId, filter, tag, {
            list, listFilter, installedPackages, recent ->

            this.installedPackages.value = installedPackages
            this.recentlyInstalled.value = recent
            setValues(list, listFilter)
        }).execute()
    }

    override fun createHeader(totalAppsCount: Int, newAppsCount: Int, recentlyUpdatedCount: Int): SparseArray<SectionHeader> {
        val sections = super.createHeader(totalAppsCount, newAppsCount, recentlyUpdatedCount)

        val isRecentVisible = !(this.recentlyInstalled.value?.isEmpty() ?: false)
        if (isRecentVisible) {
            val newSections = SparseArray<SectionHeader>()
            newSections[0] = RecentlyInstalled()
            for (i in 0 until sections.size()) {
                newSections[sections.keyAt(i) + 1] = sections.valueAt(i)
            }
            return newSections
        }
        return sections
    }
}