package com.anod.appwatcher.installed

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.anod.appwatcher.utils.combineLatest
import com.anod.appwatcher.watchlist.LoadResult
import com.anod.appwatcher.watchlist.SectionHeaderFactory
import com.anod.appwatcher.watchlist.WatchListViewModel
import info.anodsplace.framework.content.InstalledPackage

/**
 * @author Alex Gavrishev
 * @date 14/04/2018
 */

typealias InstalledPairRow = Pair<String, Int>
typealias InstalledResult = Pair<List<InstalledPairRow>, List<InstalledPackage>>

class InstalledWatchListViewModel(application: android.app.Application) : WatchListViewModel(application) {
    var hasSectionRecent = false
    var hasSectionOnDevice = false

    val installed: LiveData<InstalledResult> = liveData {
        emit(InstalledTaskWorker(context, sortId, titleFilter).run())
    }

    override var result: LiveData<LoadResult> = appsList.combineLatest(installed).map { pair ->
        val appsList = pair.first.first
        val filter = pair.first.second
        val installed = pair.second
        val recentlyInstalled = installed.first
        val installedPackages = installed.second

        val sections = SectionHeaderFactory(showRecentlyUpdated, hasSectionRecent, hasSectionOnDevice)
                .create(appsList.size, filter.newCount, filter.recentlyUpdatedCount, filter.updatableNewCount, recentlyInstalled.isNotEmpty(), installedPackages.isNotEmpty())

        InstalledLoadResult(
                recentlyInstalled,
                installedPackages,
                appsList,
                sections
        )
    }
}