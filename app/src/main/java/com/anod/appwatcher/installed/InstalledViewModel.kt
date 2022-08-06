// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.accounts.Account
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.watchlist.FilterablePagingSource
import com.anod.appwatcher.watchlist.SectionHeaderFactory
import com.anod.appwatcher.watchlist.WatchListPagingSource
import com.anod.appwatcher.watchlist.WatchListViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class InstalledViewModel(pagingSourceConfig: WatchListPagingSource.Config) : WatchListViewModel(pagingSourceConfig), KoinComponent {

    class Factory(private val pagingSourceConfig: WatchListPagingSource.Config) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InstalledViewModel(pagingSourceConfig) as T
        }
    }

    private val packageManager: PackageManager by inject()

    val account: Account?
        get() = getKoin().getOrNull()

    val changelogAdapter: ChangelogAdapter by inject { parametersOf(viewModelScope) }

    override fun createPagingSource(): FilterablePagingSource {
        return InstalledPagingSource(viewState.titleFilter, prefs, config = viewState.pagingSourceConfig, changelogAdapter, packageManager, database)
    }

    override fun createSectionHeaderFactory() = SectionHeaderFactory.Empty()
}