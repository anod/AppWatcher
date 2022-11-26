package com.anod.appwatcher.watchlist

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
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
import com.anod.appwatcher.compose.SearchTopBar

@Composable
fun WatchListTopBar(
        title: String,
        subtitle: String?,
        containerColor: Color,
        contentColor: Color,
        filterQuery: String,
        filterTitles: List<String>,
        sortId: Int,
        visibleActions: @Composable () -> Unit,
        dropdownActions: @Composable (dismiss: () -> Unit) -> Unit,
        onEvent: (WatchListSharedStateEvent) -> Unit
) {

    val showSearchView by remember { mutableStateOf(filterQuery.isNotBlank()) }
    var topBarMoreMenu by remember { mutableStateOf(false) }
    var topBarFilterMenu by remember { mutableStateOf(false) }
    var topBarSortMenu by remember { mutableStateOf(false) }

    SearchTopBar(
        title = title,
        subtitle = subtitle,
        searchQuery = filterQuery,
        showSearch = showSearchView,
        initialSearchFocus = true,
        containerColor = containerColor,
        contentColor = contentColor,
        onValueChange = { onEvent(WatchListSharedStateEvent.FilterByTitle(query = it)) },
        onSearchAction = { onEvent(WatchListSharedStateEvent.OnSearch(it)) },
        onNavigation = { onEvent(WatchListSharedStateEvent.OnBackPressed) },
        actions = {

                visibleActions()

                IconButton(onClick = {
                    topBarMoreMenu = true
                }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.more))
                }

                DropdownMenu(expanded = topBarMoreMenu, onDismissRequest = { topBarMoreMenu = false }) {
                    dropdownActions(dismiss = { topBarMoreMenu = false })

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
                    filterTitles.forEachIndexed { index, title ->
                        DropdownMenuItem(text = { Text(text = title) }, onClick = {
                            onEvent(WatchListSharedStateEvent.FilterById(filterId = index))
                            topBarMoreMenu = false
                            topBarFilterMenu = false
                        })
                    }
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
                                leadingIcon = { Icon(imageVector = if (sortId == index) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, contentDescription = null) },
                                onClick = {
                                    onEvent(WatchListSharedStateEvent.ChangeSort(sortId = index))
                                    topBarMoreMenu = false
                                    topBarSortMenu = false
                                }
                        )
                    }
                }
            }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DefaultPreview() {
    AppTheme(
            customPrimaryColor = Color.Cyan
    ) {
        Scaffold(
                topBar = {
                    WatchListTopBar(
                            title = "What will happen when title is too long",
                            subtitle = "What will happen when subtitle is too long",
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            filterTitles = listOf(),
                            filterQuery = "",
                            sortId = 0,
                            visibleActions = {
                                IconButton(onClick = { }) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.menu_tag_apps))
                                }
                            },
                            dropdownActions = { dismiss ->
                                DropdownMenuItem(
                                        text = { Text(text = stringResource(id = R.string.menu_edit)) },
                                        leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = R.string.menu_edit)) },
                                        onClick = {
                                            dismiss()
                                        }
                                )
                            },
                            onEvent = { }
                    )
                }
        ) { paddingValues ->
            Box(Modifier.padding(paddingValues)) {
            }
        }
    }
}