// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.accounts.Account
import android.content.pm.PackageManager
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingSource
import com.anod.appwatcher.watchlist.SectionHeaderFactory
import com.anod.appwatcher.watchlist.SectionItem
import com.anod.appwatcher.watchlist.WatchListPagingSource
import com.anod.appwatcher.watchlist.WatchListViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class InstalledViewModel(application: android.app.Application) : WatchListViewModel(application), KoinComponent {
    private val packageManager: PackageManager by inject()

    val account: Account?
        get() = getKoin().getOrNull()

    val changelogAdapter: ChangelogAdapter by inject { parametersOf(viewModelScope) }

    override fun createPagingSource(config: WatchListPagingSource.Config): PagingSource<Int, SectionItem> {
        return InstalledPagingSource(sortId, titleFilter, config, changelogAdapter, packageManager, database)
    }

    override fun createSectionHeaderFactory(config: WatchListPagingSource.Config) =
            SectionHeaderFactory.Empty()

}