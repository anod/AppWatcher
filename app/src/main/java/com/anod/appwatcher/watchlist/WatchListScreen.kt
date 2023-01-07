package com.anod.appwatcher.watchlist

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.collectAsLazyPagingItems
import com.anod.appwatcher.R
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.tags.AppsTagDialog
import com.anod.appwatcher.tags.EditTagDialog
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.InstalledApps

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun WatchListScreen(
    screenState: WatchListSharedState,
    pagingSourceConfig: WatchListPagingSource.Config,
    onEvent: (WatchListEvent) -> Unit,
    topBarContent: @Composable (subtitle: String?, filterId: Int) -> Unit,
    installedApps: InstalledApps
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

    AppLog.d("Recomposition ${screenState.hashCode()}")

    Scaffold(
        topBar = { topBarContent(subtitle, filterIds[pagerState.currentPage]) }
    ) { paddingValues ->
        HorizontalPager(
            count = filterPagesTitles.size,
            state = pagerState,
            modifier = Modifier.padding(paddingValues),
            key = { filterIds[it] }
        ) { pageIndex ->
            val filterId = remember(pageIndex) { filterIds[pageIndex] }
            val pageConfig by remember(pagingSourceConfig, filterId) {
                derivedStateOf {
                    pagingSourceConfig.copy(
                        filterId = filterId,
                        showOnDevice = if (filterId == Filters.ALL) pagingSourceConfig.showOnDevice else false,
                        showRecentlyInstalled = if (filterId == Filters.ALL) pagingSourceConfig.showRecentlyInstalled else false,
                    )
                }
            }
            val pagerFactory = remember(pageConfig) {
                AppsWatchListPagerFactory(pageConfig, installedApps = installedApps)
            }
            pagerFactory.filterQuery = screenState.titleFilter
            val items = pagerFactory.pagingData.collectAsLazyPagingItems()

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

            AppLog.d("HorizontalPager Recomposition [page=${pageConfig.filterId}] ${items.hashCode()}")

            WatchListPage(
                items = items,
                isRefreshing = screenState.syncProgress?.isRefreshing == true,
                enablePullToRefresh = screenState.enablePullToRefresh,
                installedApps = installedApps,
                onEvent = { event -> onEvent(event) },
                recentlyInstalledApps = screenState.recentlyInstalledApps
            )
        }

        if (screenState.showAppTagDialog) {
            AppsTagDialog(
                tag = screenState.tag,
                onDismissRequest = { onEvent(WatchListEvent.AddAppToTag(show = false)) }
            )
        }

        if (screenState.showEditTagDialog) {
            EditTagDialog(
                tag = screenState.tag,
                onDismissRequest = { onEvent(WatchListEvent.EditTag(show = false)) }
            )
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (screenState.filterId != pagerState.currentPage) {
            onEvent(WatchListEvent.FilterById(filterId = pagerState.currentPage))
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