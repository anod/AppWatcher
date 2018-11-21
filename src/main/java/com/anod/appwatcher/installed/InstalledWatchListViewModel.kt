package com.anod.appwatcher.installed

import androidx.lifecycle.*
import android.util.SparseArray
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.watchlist.*
import info.anodsplace.framework.content.*
import info.anodsplace.framework.livedata.OneTimeObserver
import info.anodsplace.framework.os.LiveDataTask

/**
 * @author Alex Gavrishev
 * @date 14/04/2018
 */

typealias InstalledPairRow = Pair<String, Int>
typealias InstalledResult = Pair<List<InstalledPairRow>, List<InstalledPackage>>

private class InstalledResultMediator(
        private var showRecentlyUpdated: Boolean,
        private var hasSectionRecent: Boolean,
        private var hasSectionOnDevice: Boolean,
        private var filter: AppListFilter
): MediatorLiveData<LoadResult>() {
    private var number = 0
    private var result = InstalledLoadResult(emptyList(), emptyList(), emptyList(), SparseArray())

    fun addAppsList(source: LiveData<AppsList>) {
        addSource(source, {
            result = InstalledLoadResult(
                result.recentlyInstalled,
                result.installedPackages,
                it ?: emptyList(),
                result.sections
            )
            update()
            removeSource(source)
        })
    }

    fun addInstalled(source: LiveData<InstalledResult>) {
        addSource(source, {
            result = InstalledLoadResult(
                    it?.first ?: emptyList(),
                    it?.second ?: emptyList(),
                    result.appsList,
                    result.sections
            )
            update()
            removeSource(source)
        })
    }

    private fun update() {
        number++
        if (number == 2) {
            val appsList = result.appsList
            val recentlyInstalled = result.recentlyInstalled
            val installedPackages = result.installedPackages
            val sections = SectionHeaderFactory(showRecentlyUpdated, hasSectionRecent, hasSectionOnDevice)
                    .create(appsList.size, filter.newCount, filter.recentlyUpdatedCount, filter.updatableNewCount, recentlyInstalled.isNotEmpty(), installedPackages.isNotEmpty())

            value = InstalledLoadResult(
                recentlyInstalled,
                installedPackages,
                appsList,
                sections
            )
        }
    }
}

class InstalledWatchListViewModel(application: android.app.Application) : WatchListViewModel(application) {
    var hasSectionRecent = false
    var hasSectionOnDevice = false

    override fun load(): LiveData<LoadResult> {
        val apps = loadApps()
        val installed = LiveDataTask(InstalledTaskWorker(context, sortId, titleFilter)).execute()

        val mediator = InstalledResultMediator(
            showRecentlyUpdated,
            hasSectionRecent,
            hasSectionOnDevice,
            filter
        )
        mediator.addAppsList(apps)
        mediator.addInstalled(installed)
        return mediator
    }

}