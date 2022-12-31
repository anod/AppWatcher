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
import com.anod.appwatcher.compose.SortDropdownMenu
import com.anod.appwatcher.compose.SortMenuItem

@Composable
fun WatchListTopBar(
    title: String,
    subtitle: String?,
    containerColor: Color,
    contentColor: Color,
    filterQuery: String,
    visibleActions: @Composable () -> Unit,
    dropdownActions: @Composable ((dismiss: () -> Unit) -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    onEvent: (WatchListSharedStateEvent) -> Unit
) {

    val showSearchView by remember { mutableStateOf(filterQuery.isNotBlank()) }

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
        navigationIcon = navigationIcon,
        actions = {
            visibleActions()

            if (dropdownActions != null) {
                var topBarMoreMenu by remember { mutableStateOf(false) }

                IconButton(onClick = { topBarMoreMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(id = R.string.more)
                    )
                }

                DropdownMenu(
                    expanded = topBarMoreMenu,
                    onDismissRequest = { topBarMoreMenu = false }
                ) {
                    dropdownActions(dismiss = { topBarMoreMenu = false })
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
                    filterQuery = "",
                    visibleActions = {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(id = R.string.menu_tag_apps)
                            )
                        }
                    },
                    dropdownActions = { dismiss ->
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.menu_edit)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(id = R.string.menu_edit)
                                )
                            },
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