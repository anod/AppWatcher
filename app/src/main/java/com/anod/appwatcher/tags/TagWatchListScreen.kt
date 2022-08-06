package com.anod.appwatcher.tags

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.watchlist.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import info.anodsplace.applog.AppLog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun TagWatchListScreen(screenState: WatchListSharedState, pagingSourceConfig: WatchListPagingSource.Config, onEvent: (WatchListSharedStateEvent) -> Unit) {
    var subtitle: String? by remember { mutableStateOf(null) }
    val filterPagesTitles = listOf(
            stringResource(id = R.string.tab_all),
            stringResource(id = R.string.tab_installed),
            stringResource(id = R.string.tab_not_installed),
            stringResource(id = R.string.tab_updatable),
    )

    val pagerState = rememberPagerState(initialPage = screenState.filterId)
    subtitle = if (pagerState.currentPage > 0) {
        filterPagesTitles[pagerState.currentPage]
    } else {
        null
    }

    AppLog.d("Recomposition: TagWatchListScreen")

    Scaffold(
            topBar = {
                WatchListTopBar(
                        title = screenState.tag.name,
                        subtitle = subtitle,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        filterTitles = filterPagesTitles,
                        filterQuery = screenState.titleFilter,
                        sortId = screenState.sortId,
                        visibleActions = {
                            IconButton(onClick = { onEvent(WatchListSharedStateEvent.AddAppToTag(screenState.tag)) }) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.menu_tag_apps))
                            }
                        },
                        onEvent = onEvent
                )
            }
    ) { paddingValues ->
        HorizontalPager(count = filterPagesTitles.size, state = pagerState, modifier = Modifier.padding(paddingValues)) { _ ->
            WatchListPage(pagingSourceConfig = pagingSourceConfig, sortId = screenState.sortId, titleQuery = screenState.titleFilter, onEvent = { event -> onEvent(WatchListSharedStateEvent.ListEvent(event)) })
        }
    }

    LaunchedEffect(screenState.filterId) {
        if (screenState.filterId != pagerState.currentPage) {
            pagerState.scrollToPage(screenState.filterId)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme {
        TagWatchListScreen(
                screenState = WatchListSharedState(
                        tag = Tag(0, "Android", Color.Cyan.value.toInt()),
                        filterId = 0,
                        sortId = 0
                ),
                pagingSourceConfig = WatchListPagingSource.Config(
                        tag = Tag(0, "Android", Color.Cyan.value.toInt()),
                        filterId = 0,
                        showRecentlyUpdated = true,
                        showOnDevice = false,
                        showRecentlyInstalled = false
                ),
                onEvent = { }
        )
    }
}