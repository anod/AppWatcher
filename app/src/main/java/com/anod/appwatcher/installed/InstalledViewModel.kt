// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.accounts.Account
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingSource
import com.anod.appwatcher.provide
import com.anod.appwatcher.watchlist.SectionHeaderFactory
import com.anod.appwatcher.watchlist.SectionItem
import com.anod.appwatcher.watchlist.WatchListPagingSource
import com.anod.appwatcher.watchlist.WatchListViewModel

class InstalledViewModel(application: android.app.Application) : WatchListViewModel(application) {

    val account: Account?
        get() = context.provide.prefs.account

    val changelogAdapter: ChangelogAdapter by lazy {
        ChangelogAdapter(context, viewModelScope, account)
    }

    override fun createPagingSource(config: WatchListPagingSource.Config): PagingSource<Int, SectionItem> {
        return InstalledPagingSource(sortId, titleFilter, config, changelogAdapter, context)
    }

    override fun createSectionHeaderFactory(config: WatchListPagingSource.Config) =
        SectionHeaderFactory.Empty()

}