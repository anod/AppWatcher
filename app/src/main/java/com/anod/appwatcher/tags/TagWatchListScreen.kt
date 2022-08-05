package com.anod.appwatcher.tags

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.watchlist.WatchListPageArgs
import com.anod.appwatcher.watchlist.WatchListPagingSource
import com.anod.appwatcher.watchlist.WatchListSharedState
import com.anod.appwatcher.watchlist.WatchListSharedStateEvent
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import info.anodsplace.applog.AppLog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun TagWatchListScreen(screenState: WatchListSharedState, pagingSourceConfig: WatchListPagingSource.Config, onEvent: (WatchListSharedStateEvent) -> Unit) {
    var topBarMoreMenu by remember { mutableStateOf(false) }
    var topBarFilterMenu by remember { mutableStateOf(false) }
    var topBarSortMenu by remember { mutableStateOf(false) }
    var subtitle: String? by remember { mutableStateOf(null) }

    val filterTitles = listOf(
            stringResource(id = R.string.tab_all),
            stringResource(id = R.string.tab_installed),
            stringResource(id = R.string.tab_not_installed),
            stringResource(id = R.string.tab_updatable),
    )

    val pages = listOf(
            WatchListPageArgs(filterId = Filters.TAB_ALL, tag = screenState.tag),
            WatchListPageArgs(filterId = Filters.INSTALLED, tag = screenState.tag),
            WatchListPageArgs(filterId = Filters.UNINSTALLED, tag = screenState.tag),
            WatchListPageArgs(filterId = Filters.UPDATABLE, tag = screenState.tag),
    )
    val pagerState = rememberPagerState(initialPage = 0)
    subtitle = if (pagerState.currentPage > 0) {
        filterTitles[pagerState.currentPage]
    } else {
        null
    }

    AppLog.d("Recomposition: TagWatchListScreen")

    Scaffold(
            topBar = {
                SmallTopAppBar(
                        title = {
                            if (subtitle != null) {
                                Column {
                                    Text(screenState.tag.name, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall)
                                    Text(subtitle!!, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                                }
                            } else {
                                Text(screenState.tag.name, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                onEvent(WatchListSharedStateEvent.OnBackPressed)
                            }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                            }
                        },
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(id = R.string.menu_filter))
                            }
                            IconButton(onClick = {}) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.menu_tag_apps))
                            }

                            IconButton(onClick = {
                                topBarMoreMenu = true
                            }) {
                                Icon(imageVector = Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.more))
                            }

                            DropdownMenu(expanded = topBarMoreMenu, onDismissRequest = { topBarMoreMenu = false }) {
                                DropdownMenuItem(
                                        text = { Text(text = stringResource(id = R.string.menu_edit)) },
                                        leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = R.string.menu_edit)) },
                                        onClick = { }
                                )

                                DropdownMenuItem(
                                        text = { Text(text = stringResource(id = R.string.filter)) },
                                        leadingIcon = { Icon(imageVector = Icons.Default.FlashOn, contentDescription = stringResource(id = R.string.filter)) },
                                        trailingIcon = { Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null) },
                                        onClick = { topBarFilterMenu = true }
                                )

                                DropdownMenuItem(
                                        text = { Text(text = stringResource(id = R.string.sort)) },
                                        leadingIcon = { Icon(imageVector = Icons.Default.Sort, contentDescription = stringResource(id = R.string.sort)) },
                                        trailingIcon = { Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null) },
                                        onClick = { topBarSortMenu = true }
                                )
                            }

                            DropdownMenu(expanded = topBarFilterMenu, onDismissRequest = { topBarFilterMenu = false }) {
                                DropdownMenuItem(text = { Text(text = stringResource(id = R.string.all)) }, onClick = { })
                                DropdownMenuItem(text = { Text(text = stringResource(id = R.string.tab_installed)) }, onClick = { })
                                DropdownMenuItem(text = { Text(text = stringResource(id = R.string.tab_not_installed)) }, onClick = { })
                                DropdownMenuItem(text = { Text(text = stringResource(id = R.string.tab_updatable)) }, onClick = { })
                            }

                            DropdownMenu(expanded = topBarSortMenu, onDismissRequest = { topBarSortMenu = false }) {
                                val sortTitles = listOf(
                                        stringResource(id = R.string.sort_by_name_asc),
                                        stringResource(id = R.string.sort_by_name_desc),
                                        stringResource(id = R.string.sort_by_date_asc),
                                        stringResource(id = R.string.sort_by_date_desc),
                                )
                                sortTitles.forEachIndexed { index, sortTitle ->
                                    DropdownMenuItem(
                                            text = { Text(text = sortTitle) },
                                            leadingIcon = { Icon(imageVector = if (screenState.sortId == index) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, contentDescription = null) },
                                            onClick = {
                                                onEvent(WatchListSharedStateEvent.ChangeSort(sortId = index))
                                                topBarMoreMenu = false
                                                topBarSortMenu = false
                                            }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color(screenState.tag.color),
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                )
            }
    ) { paddingValues ->
        //HorizontalPager(count = pages.size, state = pagerState, modifier = Modifier.padding(paddingValues)) { pageIndex ->
        Box(modifier = Modifier.padding(paddingValues)) {
            WatchListPage(args = pages[0], pagingSourceConfig = pagingSourceConfig, sortId = screenState.sortId, onEvent = { event -> onEvent(WatchListSharedStateEvent.ListEvent(event)) })
        }
        //}
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme {
        TagWatchListScreen(WatchListSharedState(
                tag = Tag(0, "Android", Color.Cyan.value.toInt()),
                sortId = 0,
        ), pagingSourceConfig = WatchListPagingSource.Config(showRecentlyUpdated = true, showOnDevice = false, showRecentlyInstalled = false), onEvent = { })
    }
}