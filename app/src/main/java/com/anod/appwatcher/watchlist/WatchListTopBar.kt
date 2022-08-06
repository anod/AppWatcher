package com.anod.appwatcher.watchlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.anod.appwatcher.R

@OptIn(ExperimentalMaterial3Api::class)
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
        onEvent: (WatchListSharedStateEvent) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    var showSearchView by remember { mutableStateOf(filterQuery.isNotBlank()) }
    var topBarMoreMenu by remember { mutableStateOf(false) }
    var topBarFilterMenu by remember { mutableStateOf(false) }
    var topBarSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(showSearchView) {
        if (showSearchView) {
            focusRequester.requestFocus()
        }
    }

    SmallTopAppBar(
            title = {
                if (showSearchView) {
                    var searchValue by remember { mutableStateOf(filterQuery) }
                    TextField(
                            modifier = Modifier.focusRequester(focusRequester),
                            value = searchValue,
                            onValueChange = {
                                searchValue = it
                                onEvent(WatchListSharedStateEvent.FilterByTitle(query = it))
                            },
                            placeholder = {
                                Text(text = stringResource(id = R.string.search))
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(id = R.string.menu_filter))
                            },
                            keyboardOptions = KeyboardOptions.Default,
                            keyboardActions = KeyboardActions.Default,
                            singleLine = true,
                            colors = TextFieldDefaults.textFieldColors(
                                    containerColor = containerColor,
                                    textColor = contentColor,
                                    focusedLeadingIconColor = contentColor,
                                    unfocusedLeadingIconColor = contentColor.copy(alpha = 0.4f),
                                    placeholderColor = contentColor.copy(alpha = 0.4f),
                                    focusedTrailingIconColor = contentColor,
                                    unfocusedTrailingIconColor = contentColor.copy(alpha = 0.4f),
                            )
                    )
                } else {
                    if (subtitle != null) {
                        Column {
                            Text(title, color = contentColor, style = MaterialTheme.typography.headlineSmall)
                            Text(subtitle, color = contentColor, style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        Text(title, color = contentColor, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (showSearchView) {
                        showSearchView = false
                    } else {
                        onEvent(WatchListSharedStateEvent.OnBackPressed)
                    }
                }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                }
            },
            actions = {
                if (!showSearchView) {
                    IconButton(onClick = {
                        showSearchView = true
                    }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(id = R.string.menu_filter))
                    }
                }

                visibleActions()

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
                    filterTitles.forEachIndexed { index, title ->
                        DropdownMenuItem(text = { Text(text = title) }, onClick = {
                            onEvent(WatchListSharedStateEvent.FilterById(filterId = index))
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
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor,
            )
    )
}