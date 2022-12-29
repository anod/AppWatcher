package com.anod.appwatcher.tags

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.anod.appwatcher.R
import com.anod.appwatcher.model.Filters
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
                            IconButton(onClick = { onEvent(WatchListSharedStateEvent.AddAppToTag(show = true)) }) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.menu_tag_apps))
                            }
                        },
                        dropdownActions = { dismiss ->
                            DropdownMenuItem(
                                    text = { Text(text = stringResource(id = R.string.menu_edit)) },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = R.string.menu_edit)) },
                                    onClick = {
                                        onEvent(WatchListSharedStateEvent.EditTag(screenState.tag))
                                        dismiss()
                                    }
                            )
                        },
                        onEvent = onEvent
                )
            }
    ) { paddingValues ->
        HorizontalPager(count = filterPagesTitles.size, state = pagerState, modifier = Modifier.padding(paddingValues)) { pageIndex ->
            val pageConfig = pagingSourceConfig.copy(filterId = filterIds[pageIndex])
            val viewModel: WatchListViewModel = viewModel(key = pageConfig.filterId.toString(), factory = AppsWatchListViewModel.Factory(pageConfig))
            val items = viewModel.pagingData.collectAsLazyPagingItems()

            LaunchedEffect(key1 = screenState.titleFilter, key2 = screenState.sortId) {
                AppLog.d("TagWatchListScreen: refresh [page=${pageConfig.filterId}] '${screenState.titleFilter}', ${screenState.sortId}")
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
    }

    LaunchedEffect(screenState.filterId) {
        if (screenState.filterId != pagerState.currentPage) {
            pagerState.scrollToPage(screenState.filterId)
        }
    }
}