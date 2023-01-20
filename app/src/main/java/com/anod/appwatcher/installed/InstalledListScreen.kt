package com.anod.appwatcher.installed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.anod.appwatcher.R
import com.anod.appwatcher.watchlist.WatchListPage
import com.anod.appwatcher.watchlist.WatchListPagingSource
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.InstalledApps

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledListScreen(
    screenState: InstalledListState,
    pagingSourceConfig: WatchListPagingSource.Config,
    onEvent: (InstalledListEvent) -> Unit,
    installedApps: InstalledApps
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
                        onClick = { if (enabled) { onEvent(InstalledListEvent.Import) } }
                )
            }
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            val scope = rememberCoroutineScope()
            val pagerFactory: InstalledListPagerFactory = remember(pagingSourceConfig, scope) {
                InstalledListPagerFactory(pagingSourceConfig, scope)
            }
            pagerFactory.sortId = screenState.sortId
            pagerFactory.selectionMode = screenState.selectionMode
            pagerFactory.filterQuery = screenState.titleFilter

            val items = pagerFactory.pagingData.collectAsLazyPagingItems()
            val changelogUpdated by pagerFactory.changelogAdapter.updated.collectAsState(initial = false)
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
                enablePullToRefresh = screenState.enablePullToRefresh,
                selection = screenState.selection,
                selectionMode = screenState.selectionMode,
                installedApps = installedApps,
                onEvent = { event -> onEvent(InstalledListEvent.ListEvent(event)) }
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

