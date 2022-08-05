// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.accounts.Account
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingSource
import com.anod.appwatcher.watchlist.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class InstalledViewModel(args: WatchListPageArgs, pagingSourceConfig: WatchListPagingSource.Config) : WatchListViewModel(args, pagingSourceConfig), KoinComponent {

    class Factory(private val args: WatchListPageArgs, private val pagingSourceConfig: WatchListPagingSource.Config) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InstalledViewModel(args, pagingSourceConfig) as T
        }
    }

    private val packageManager: PackageManager by inject()

    val account: Account?
        get() = getKoin().getOrNull()

    val changelogAdapter: ChangelogAdapter by inject { parametersOf(viewModelScope) }

    override fun createPagingSource(): PagingSource<Int, SectionItem> {
        return InstalledPagingSource(prefs, viewState.titleFilter, config = viewState.pagingSourceConfig, changelogAdapter, packageManager, database)
    }

    override fun createSectionHeaderFactory() = SectionHeaderFactory.Empty()
}