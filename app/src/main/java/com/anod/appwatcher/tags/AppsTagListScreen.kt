package com.anod.appwatcher.tags

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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun AppsTagScreen(tag: Tag, sortIndex: Int, onEvent: (AppsTagScreenEvent) -> Unit) {
    var topBarMoreMenu by remember { mutableStateOf(false) }
    var topBarFilterMenu by remember { mutableStateOf(false) }
    var topBarSortMenu by remember { mutableStateOf(false) }
    var subtitle: String? by remember { mutableStateOf(null) }
    val titles = listOf(
            stringResource(id = R.string.tab_all),
            stringResource(id = R.string.tab_installed),
            stringResource(id = R.string.tab_not_installed),
            stringResource(id = R.string.tab_updatable),
    )
    val pages = listOf(
            WatchListPageArgs(sortId = sortIndex, filterId = Filters.TAB_ALL, tag = tag),
            WatchListPageArgs(sortId = sortIndex, filterId = Filters.INSTALLED, tag = tag),
            WatchListPageArgs(sortId = sortIndex, filterId = Filters.UNINSTALLED, tag = tag),
            WatchListPageArgs(sortId = sortIndex, filterId = Filters.UPDATABLE, tag = tag),
    )
    val pagerState = rememberPagerState(initialPage = 0)
    subtitle = if (pagerState.currentPage > 0) {
        titles[pagerState.currentPage]
    } else {
        null
    }

    Scaffold(
            topBar = {
                SmallTopAppBar(
                        title = {
                            if (subtitle != null) {
                                Column {
                                    Text(tag.name, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall)
                                    Text(subtitle!!, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                                }
                            } else {
                                Text(tag.name, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                onEvent(AppsTagScreenEvent.OnBackPressed)
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
                                DropdownMenuItem(text = { Text(text = stringResource(id = R.string.sort_by_name_asc)) }, onClick = { })
                                DropdownMenuItem(text = { Text(text = stringResource(id = R.string.sort_by_name_desc)) }, onClick = { })
                                DropdownMenuItem(text = { Text(text = stringResource(id = R.string.sort_by_date_asc)) }, onClick = { })
                                DropdownMenuItem(text = { Text(text = stringResource(id = R.string.sort_by_date_desc)) }, onClick = { })
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color(tag.color),
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                )
            }
    ) { paddingValues ->
        HorizontalPager(count = pages.size, state = pagerState, modifier = Modifier.padding(paddingValues)) { pageIndex ->
            AppsTagListPage(args = pages[pageIndex], onEvent = { event -> onEvent(AppsTagScreenEvent.ListEvent(event)) })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme {
        AppsTagScreen(Tag(0, "Android", Color.Cyan.value.toInt()), sortIndex = 0, onEvent = { })
    }
}