package com.anod.appwatcher.watchlist

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.anod.appwatcher.R
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.tags.AppsTagDialog
import com.anod.appwatcher.tags.EditTagDialog
import com.anod.appwatcher.utils.prefs
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import info.anodsplace.applog.AppLog


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun WatchListScreen(
    screenState: WatchListSharedState,
    pagingSourceConfig: WatchListPagingSource.Config,
    onEvent: (WatchListSharedStateEvent) -> Unit,
    topBarContent: @Composable (String?, List<String>) -> Unit
) {
    var subtitle: String? by remember { mutableStateOf(null) }
    val filterPagesTitles = listOf(
        stringResource(id = R.string.tab_all),
        stringResource(id = R.string.tab_installed),
        stringResource(id = R.string.tab_not_installed),
        stringResource(id = R.string.tab_updatable),
    )

    val filterIds = listOf(
        Filters.ALL,
        Filters.INSTALLED,
        Filters.UNINSTALLED,
        Filters.UPDATABLE
    )

    val pagerState = rememberPagerState(initialPage = screenState.filterId)
    subtitle = if (pagerState.currentPage > 0) {
        filterPagesTitles[pagerState.currentPage]
    } else {
        null
    }

    AppLog.d("Recomposition $screenState")

    Scaffold(
        topBar = { topBarContent(subtitle, filterPagesTitles) }
    ) { paddingValues ->
        HorizontalPager(count = filterPagesTitles.size, state = pagerState, modifier = Modifier.padding(paddingValues)) { pageIndex ->
            val filterId = filterIds[pageIndex]
            val pageConfig = pagingSourceConfig.copy(
                filterId = filterId,
                showOnDevice = if (filterId == Filters.ALL) pagingSourceConfig.showOnDevice else false,
                showRecentlyInstalled = if (filterId == Filters.ALL) pagingSourceConfig.showRecentlyInstalled else false,
            )
            val viewModel: WatchListViewModel = viewModel(key = pageConfig.filterId.toString(), factory = AppsWatchListViewModel.Factory(pageConfig))
            val items = viewModel.pagingData.collectAsLazyPagingItems()

            val refreshKey = remember(screenState) {
                RefreshKey(
                    titleFilter = screenState.titleFilter,
                    tagAppsChange = screenState.tagAppsChange,
                    sortId = screenState.sortId,
                    dbAppsChange = screenState.dbAppsChange
                )
            }

            LaunchedEffect(refreshKey) {
                AppLog.d("Refresh $refreshKey")
                items.refresh()
            }

            AppLog.d("TagWatchListScreen: Recomposition [page=${pageConfig.filterId}] ${items.hashCode()}")

            WatchListPage(
                items = items,
                isRefreshing = screenState.listState is ListState.SyncStarted,
                enablePullToRefresh = viewModel.prefs.enablePullToRefresh,
                installedApps = viewModel.installedApps,
                onEvent = { event -> onEvent(WatchListSharedStateEvent.ListEvent(event)) }
            )
        }

        if (screenState.showAppTagDialog) {
            AppsTagDialog(
                tag = screenState.tag,
                onDismissRequest = { onEvent(WatchListSharedStateEvent.AddAppToTag(show = false)) }
            )
        }

        if (screenState.showEditTagDialog) {
            EditTagDialog(
                tag = screenState.tag,
                onDismissRequest = { onEvent(WatchListSharedStateEvent.EditTag(show = false)) }
            )
        }
    }

    LaunchedEffect(screenState.filterId) {
        if (screenState.filterId != pagerState.currentPage) {
            pagerState.scrollToPage(screenState.filterId)
        }
    }
}

private data class RefreshKey(
    val titleFilter: String,
    val sortId: Int,
    val tagAppsChange: Int,
    val dbAppsChange: Int,
)