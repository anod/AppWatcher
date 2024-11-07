package com.anod.appwatcher.watchlist

import android.content.pm.PackageManager
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.utils.prefs
import info.anodsplace.framework.content.InstalledApps
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppsWatchListPagerFactory(
    pagingSourceConfig: WatchListPagingSource.Config,
    private val installedApps: InstalledApps
) : WatchListPagerFactory(pagingSourceConfig), KoinComponent {
    private val database: AppsDatabase by inject()
    private val packageManager: PackageManager by inject()

    override fun createPagingSource(): WatchListPagingSource {
        return WatchListPagingSource(
            prefs = prefs,
            config = pagingSourceConfig,
            packageManager = packageManager,
            database = database,
            installedApps = installedApps
        ).also {
            it.filterQuery = filterQuery
        }
    }

    override fun createSectionHeaderFactory() = DefaultSectionHeaderFactory(pagingSourceConfig.showRecentlyDiscovered)
}