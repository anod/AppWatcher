package com.anod.appwatcher.watchlist

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.collectAsLazyPagingItems
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.Filters
import info.anodsplace.applog.AppLog

@Composable
fun WatchListScreen(
    screenState: WatchListSharedState,
    listPagerFactory: (filterId: Int, tag: Tag) -> WatchListPagerFactory,
    onEvent: (WatchListEvent) -> Unit,
    topBarContent: @Composable (subtitle: String?, filterId: Int) -> Unit,
    listContext: String
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

    val pagerState = rememberPagerState(
        initialPage = screenState.filterId,
        initialPageOffsetFraction = 0f,
        pageCount = { filterPagesTitles.size }
    )
    subtitle = if (pagerState.currentPage > 0) {
        filterPagesTitles[pagerState.currentPage]
    } else {
        null
    }

//    Scaffold(
//        topBar = { topBarContent(subtitle, filterIds[pagerState.currentPage]) },
//        contentWindowInsets = WindowInsets.statusBars.union(WindowInsets.displayCutout.only(WindowInsetsSides.Start))
//    ) { paddingValues ->
//        HorizontalPager(
//            state = pagerState,
//            modifier = Modifier
//                .padding(paddingValues)
//                .padding(start = 1.dp, end = 1.dp),
//            key = { filterIds[it] }
//        ) { pageIndex ->
    val pageIndex = 0
            val filterId = remember(pageIndex) { filterIds[pageIndex] }
    val pagerFactory = remember(listPagerFactory, filterId, screenState.tag) { listPagerFactory(filterId, screenState.tag) }
    AppLog.d("[Paging] pagerFactory:${pagerFactory.hashCode()} filterId:$filterId, listPagerFactory:${listPagerFactory.hashCode()}, screenState:${screenState.tag}")
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
                AppLog.d("Refresh RefreshKey:$refreshKey")
                items.refresh()
            }

            WatchListPage(
                items = items,
                isRefreshing = screenState.syncProgress?.isRefreshing == true,
                enablePullToRefresh = screenState.enablePullToRefresh,
                onEvent = onEvent,
                recentlyInstalledApps = screenState.recentlyInstalledApps,
                listContext = "$listContext-rr:${screenState.refreshRequest}-f:$filterId-rk:$refreshKey"
            )
//        }
//    }

    val latestOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(pagerState.currentPage) {
        if (screenState.filterId != pagerState.currentPage) {
            latestOnEvent(WatchListEvent.FilterById(filterId = pagerState.currentPage))
        }
    }

    LaunchedEffect(screenState.filterId) {
        if (screenState.filterId != pagerState.currentPage) {
            pagerState.scrollToPage(screenState.filterId)
        }
    }
}

data class RefreshKey(
    val titleFilter: String,
    val sortId: Int,
    val tagAppsChange: Int,
    val dbAppsChange: Int
) {
    override fun toString() = "$titleFilter-$sortId-$tagAppsChange-$dbAppsChange"
}