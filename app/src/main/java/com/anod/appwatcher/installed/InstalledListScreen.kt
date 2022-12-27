package com.anod.appwatcher.installed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.anod.appwatcher.R
import com.anod.appwatcher.watchlist.ListState
import com.anod.appwatcher.watchlist.WatchListPage
import com.anod.appwatcher.watchlist.WatchListPagingSource
import info.anodsplace.applog.AppLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledListScreen(
    screenState: InstalledListSharedState,
    pagingSourceConfig: WatchListPagingSource.Config,
    onEvent: (InstalledListSharedEvent) -> Unit
) {
    Scaffold(
            topBar = {
                InstalledTopBar(
                        title = if (screenState.selectionMode)
                                if (screenState.selection.defaultSelected)
                                    stringResource(id = R.string.all_selected, screenState.selection.selectedCount)
                                else
                                    stringResource(id = R.string.number_selected, screenState.selection.selectedCount)
                            else
                                stringResource(id = R.string.installed),
                        selectionMode = screenState.selectionMode,
                        filterQuery = screenState.titleFilter,
                        sortId = screenState.sortId,
                        onEvent = onEvent
                )
            },
            floatingActionButton = {
                if (screenState.selectionMode) {
                    val enabled = screenState.importStatus is ImportStatus.NotStarted || screenState.importStatus is ImportStatus.Finished
                    ExtendedFloatingActionButton(
                            text = { if (enabled) { Text(text = stringResource(id = R.string.import_action)) } },
                            icon = { if (!enabled) {
                                CircularProgressIndicator()
                            } },
                            onClick = { if (enabled) { onEvent(InstalledListSharedEvent.Import) } }
                    )
                }
            }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            val viewModel: InstalledListViewModel = viewModel(factory = InstalledListViewModel.Factory(pagingSourceConfig))
            viewModel.sortId = screenState.sortId
            viewModel.selectionMode = screenState.selectionMode

            AppLog.d("Recomposition: InstalledListScreen [${viewModel.sortId}, ${viewModel.hashCode()}, '${screenState.titleFilter}', ${screenState.selection.hashCode()}, ${screenState.selectionMode}]")

            val items = viewModel.pagingData.collectAsLazyPagingItems()

            val changelogUpdated by viewModel.changelogAdapter.updated.collectAsState(initial = false)
            
            LaunchedEffect(key1 = changelogUpdated, key2 = screenState.refreshRequest, key3 = screenState.packageChanged) {
                AppLog.d("InstalledListScreen: refresh [$changelogUpdated, ${screenState.refreshRequest}, ${screenState.packageChanged}]")
                items.refresh()
            }
            
            AppLog.d("Recomposition: InstalledListScreen items [${items.hashCode()}]")

            WatchListPage(
                    items = items,
                    sortId = screenState.sortId,
                    titleQuery = screenState.titleFilter,
                    isRefreshing = screenState.refreshRequest > 0 && items.loadState.refresh is LoadState.Loading,
                    enablePullToRefresh = viewModel.prefs.enablePullToRefresh,
                    selection = screenState.selection,
                    selectionMode = screenState.selectionMode,
                    installedApps = viewModel.installedApps,
                    onEvent = { event -> onEvent(InstalledListSharedEvent.ListEvent(event)) }
            )
        }
    }
}