// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.accounts.Account
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.watchlist.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class InstalledListViewModel(pagingSourceConfig: WatchListPagingSource.Config) : WatchListViewModel(pagingSourceConfig), KoinComponent {

    class Factory(private val pagingSourceConfig: WatchListPagingSource.Config) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InstalledListViewModel(pagingSourceConfig) as T
        }
    }

    private val packageManager: PackageManager by inject()
    val changelogAdapter: ChangelogAdapter by inject { parametersOf(viewModelScope) }
    private val account: Account?
        get() = getKoin().getOrNull()

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
    var filterQuery: String = ""
        set(value) {
            field = value
            (pagingSource as? InstalledPagingSource)?.filterQuery = value
        }

    override fun createPagingSource(): FilterablePagingSource {
        val pagingSource =  InstalledPagingSource(
                viewState.titleFilter,
                changelogAdapter,
                packageManager,
                database
        )
        pagingSource.sortId = sortId
        pagingSource.selectionMode = selectionMode
        pagingSource.filterQuery = filterQuery
        return pagingSource
    }

    override fun createSectionHeaderFactory() = SectionHeaderFactory.Empty()
}