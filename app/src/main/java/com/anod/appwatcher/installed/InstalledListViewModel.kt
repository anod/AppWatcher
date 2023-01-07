// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.content.pm.PackageManager
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.watchlist.FilterablePagingSource
import com.anod.appwatcher.watchlist.SectionHeaderFactory
import com.anod.appwatcher.watchlist.WatchListPagerFactory
import com.anod.appwatcher.watchlist.WatchListPagingSource
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class InstalledListPagerFactory(pagingSourceConfig: WatchListPagingSource.Config, coroutineScope: CoroutineScope) : WatchListPagerFactory(pagingSourceConfig), KoinComponent {

    private val packageManager: PackageManager by inject()
    private val database: AppsDatabase by inject()

    val changelogAdapter: ChangelogAdapter by inject { parametersOf(coroutineScope) }

    var sortId: Int = 0
        set(value) {
            field = value
            (pagingSource as? InstalledPagingSource)?.sortId = value
        }
    var selectionMode: Boolean = false
        set(value) {
            field = value
            (pagingSource as? InstalledPagingSource)?.selectionMode = value
        }

    override fun createPagingSource(): FilterablePagingSource {
        return InstalledPagingSource(
                changelogAdapter,
                packageManager,
                database
        ).also {
            it.sortId = sortId
            it.selectionMode = selectionMode
            it.filterQuery = filterQuery
        }
    }

    override fun createSectionHeaderFactory() = SectionHeaderFactory.Empty()
}