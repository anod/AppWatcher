package com.anod.appwatcher.watchlist

import android.content.pm.PackageManager
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.utils.PackageStateProvider
import com.anod.appwatcher.utils.prefs
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppsWatchListPagerFactory(pagingSourceConfig: WatchListPagingSource.Config, private val packageStates: PackageStateProvider, cacheScope: CoroutineScope) :
    WatchListPagerFactory(pagingSourceConfig, cacheScope),
    KoinComponent {
    private val database: AppsDatabase by inject()
    private val packageManager: PackageManager by inject()

    override fun createPagingSource(): WatchListPagingSource = WatchListPagingSource(
        prefs = prefs,
        config = pagingSourceConfig,
        packageManager = packageManager,
        database = database,
        packageStates = packageStates
    ).also {
        it.filterQuery = filterQuery
    }

    override fun createSectionHeaderFactory() = DefaultSectionHeaderFactory(pagingSourceConfig.showRecentlyDiscovered)
}