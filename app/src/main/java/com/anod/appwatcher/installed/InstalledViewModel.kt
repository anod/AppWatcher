// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import androidx.paging.PagingSource
import com.anod.appwatcher.watchlist.SectionHeaderFactory
import com.anod.appwatcher.watchlist.SectionItem
import com.anod.appwatcher.watchlist.WatchListPagingSource
import com.anod.appwatcher.watchlist.WatchListViewModel

class InstalledViewModel(application: android.app.Application) : WatchListViewModel(application) {
    var showSelection = false

    override fun createPagingSource(config: WatchListPagingSource.Config): PagingSource<Int, SectionItem> {
        return InstalledPagingSource(sortId, titleFilter, showSelection, context)
    }

    override fun createSectionHeaderFactory(config: WatchListPagingSource.Config) = SectionHeaderFactory.Empty()

}