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

class InstalledViewModel(sortIndex: Int, pagingSourceConfig: WatchListPagingSource.Config) : WatchListViewModel(pagingSourceConfig), KoinComponent {

    class Factory(private val sortIndex: Int, private val pagingSourceConfig: WatchListPagingSource.Config) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InstalledViewModel(sortIndex, pagingSourceConfig) as T
        }
    }

    private val packageManager: PackageManager by inject()

    val account: Account?
        get() = getKoin().getOrNull()

    val changelogAdapter: ChangelogAdapter by inject { parametersOf(viewModelScope) }
    var selectionMode = false
    var sortId: Int = sortIndex
        private set

    fun changeSortId(sortId: Int, reload: Boolean) {
        this.sortId = sortId
        if (reload) {
            emitAction(WatchListAction.Reload)
        }
    }

    override fun createPagingSource(): FilterablePagingSource {
        return InstalledPagingSource(
                viewState.titleFilter,
                sortIndex = sortId,
                selectionMode = selectionMode,
                changelogAdapter,
                packageManager,
                database
        )
    }

    override fun createSectionHeaderFactory() = SectionHeaderFactory.Empty()
}