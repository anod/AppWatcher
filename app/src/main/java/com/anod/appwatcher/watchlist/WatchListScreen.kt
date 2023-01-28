package com.anod.appwatcher.watchlist

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
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
import androidx.lifecycle.Lifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.anod.appwatcher.R
import com.anod.appwatcher.model.Filters
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import info.anodsplace.applog.AppLog
import info.anodsplace.compose.LifecycleEffect
import info.anodsplace.framework.content.InstalledApps

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun WatchListScreen(
    screenState: WatchListSharedState,
    pagingSourceConfig: WatchListPagingSource.Config,
    onEvent: (WatchListEvent) -> Unit,
    topBarContent: @Composable (subtitle: String?, filterId: Int) -> Unit,
    listContext: String,
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

    Scaffold(
        topBar = { topBarContent(subtitle, filterIds[pagerState.currentPage]) },
        contentWindowInsets = WindowInsets.statusBars
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

            LifecycleEffect { event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        AppLog.d("ON_RESUME $refreshKey")
                        items.refresh()
                    }
                    else -> { }
                }
            }

            WatchListPage(
                items = items,
                isRefreshing = screenState.syncProgress?.isRefreshing == true,
                enablePullToRefresh = screenState.enablePullToRefresh,
                installedApps = installedApps,
                onEvent = { event -> onEvent(event) },
                recentlyInstalledApps = screenState.recentlyInstalledApps,
                listContext = "$listContext-filterId-$filterId"
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