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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.anod.appwatcher.R
import com.anod.appwatcher.watchlist.ListState
import com.anod.appwatcher.watchlist.WatchListEvent
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
    AppLog.d("Recomposition $screenState")

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
            viewModel.filterQuery = screenState.titleFilter

            val items = viewModel.pagingData.collectAsLazyPagingItems()
            val changelogUpdated by viewModel.changelogAdapter.updated.collectAsState(initial = false)
            val refreshKey = remember(screenState, changelogUpdated) {
                RefreshKey(
                        changelogUpdated = changelogUpdated,
                        refreshRequest = screenState.refreshRequest,
                        packageChanged = screenState.packageChanged,
                        titleFilter = screenState.titleFilter,
                        selectionMode = screenState.selectionMode,
                        sortId = screenState.sortId
                )
            }

            LaunchedEffect(key1 = refreshKey) {
                AppLog.d("Refresh $refreshKey")
                items.refresh()
            }

            AppLog.d("Recomposition [${items.hashCode()}] $refreshKey")

            WatchListPage(
                    items = items,
                    isRefreshing = items.loadState.refresh is LoadState.Loading && (screenState.refreshRequest > 0 || items.itemCount < 1),
                    enablePullToRefresh = viewModel.prefs.enablePullToRefresh,
                    selection = screenState.selection,
                    selectionMode = screenState.selectionMode,
                    installedApps = viewModel.installedApps,
                    onEvent = { event -> onEvent(InstalledListSharedEvent.ListEvent(event)) }
            )
        }
    }
}

private data class RefreshKey(
        val changelogUpdated: Boolean,
        val refreshRequest: Int,
        val packageChanged: String,
        val titleFilter: String,
        val selectionMode: Boolean,
        val sortId: Int
)

