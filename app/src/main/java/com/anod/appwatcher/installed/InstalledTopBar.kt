package com.anod.appwatcher.installed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Ballot
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.SearchTopBar
import com.anod.appwatcher.compose.SortMenuAction

@Composable
fun InstalledTopBar(
    title: String,
    filterQuery: String,
    sortId: Int,
    selectionMode: Boolean,
    onEvent: (InstalledListEvent) -> Unit
) {
    val showSearchView by remember { mutableStateOf(filterQuery.isNotBlank()) }

    SearchTopBar(
        title = title,
        searchQuery = filterQuery,
        showSearch = showSearchView && !selectionMode,
        initialSearchFocus = true,
        onValueChange = { onEvent(InstalledListEvent.FilterByTitle(query = it)) },
        onNavigation = {
            if (selectionMode) {
                onEvent(InstalledListEvent.SwitchImportMode(selectionMode = false))
            } else {
                onEvent(InstalledListEvent.OnBackPressed)
            }
        },
        actions = {
            if (selectionMode) {
                IconButton(onClick = { onEvent(InstalledListEvent.SetSelection(all = false)) }) {
                    Icon(imageVector = Icons.Default.Deselect, contentDescription = stringResource(id = R.string.none))
                }

                IconButton(onClick = { onEvent(InstalledListEvent.SetSelection(all = true)) }) {
                    Icon(imageVector = Icons.Default.SelectAll, contentDescription = stringResource(id = R.string.all))
                }
            } else {
                IconButton(onClick = { onEvent(InstalledListEvent.SwitchImportMode(selectionMode = true)) }) {
                    Icon(imageVector = Icons.Default.Ballot, contentDescription = stringResource(id = R.string.import_mode))
                }

                SortMenuAction(
                    selectedSortId = sortId,
                    onChangeSort = { index ->
                        onEvent(InstalledListEvent.ChangeSort(sortId = index))
                    }
                )
            }
        }
    )
}