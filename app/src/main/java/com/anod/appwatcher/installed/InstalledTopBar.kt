package com.anod.appwatcher.installed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Ballot
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.SearchTopBar
import com.anod.appwatcher.compose.SortDropdownMenu

@Composable
fun InstalledTopBar(
        title: String,
        filterQuery: String,
        sortId: Int,
        selectionMode: Boolean,
        onEvent: (InstalledListSharedEvent) -> Unit
) {
    val showSearchView by remember { mutableStateOf(filterQuery.isNotBlank()) }
    var topBarSortMenu by remember { mutableStateOf(false) }

    SearchTopBar(
            title = title,
            searchQuery = filterQuery,
            showSearch = showSearchView && !selectionMode,
            initialSearchFocus = true,
            onValueChange = { onEvent(InstalledListSharedEvent.FilterByTitle(query = it)) },
            onSearchAction = { },
            onNavigation = {
                if (selectionMode) {
                    onEvent(InstalledListSharedEvent.SwitchImportMode(selectionMode = false))
                } else {
                    onEvent(InstalledListSharedEvent.OnBackPressed)
                }
            },
            actions = {
                if (selectionMode) {
                    IconButton(onClick = { onEvent(InstalledListSharedEvent.SetSelection(all = false)) }) {
                        Icon(imageVector = Icons.Default.Deselect, contentDescription = stringResource(id = R.string.none))
                    }

                    IconButton(onClick = { onEvent(InstalledListSharedEvent.SetSelection(all = true)) }) {
                        Icon(imageVector = Icons.Default.SelectAll, contentDescription = stringResource(id = R.string.all))
                    }
                } else {
                    IconButton(onClick = { onEvent(InstalledListSharedEvent.SwitchImportMode(selectionMode = true)) }) {
                        Icon(imageVector = Icons.Default.Ballot, contentDescription = stringResource(id = R.string.import_mode))
                    }

                    IconButton(onClick = { topBarSortMenu = true }) {
                        Icon(imageVector = Icons.Default.Sort, contentDescription = stringResource(id = R.string.sort))
                    }

                    SortDropdownMenu(
                            selectedSortId = sortId,
                            onChangeSort = { index ->
                                onEvent(InstalledListSharedEvent.ChangeSort(sortId = index))
                                topBarSortMenu = false
                            },
                            expanded = topBarSortMenu,
                            onDismissRequest = { topBarSortMenu = false }
                    )
                }
            }
    )
}