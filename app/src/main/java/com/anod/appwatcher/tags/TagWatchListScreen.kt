package com.anod.appwatcher.tags

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.SortMenuItem
import com.anod.appwatcher.watchlist.WatchListPagingSource
import com.anod.appwatcher.watchlist.WatchListScreen
import com.anod.appwatcher.watchlist.WatchListSharedState
import com.anod.appwatcher.watchlist.WatchListSharedStateEvent
import com.anod.appwatcher.watchlist.WatchListTopBar

@Composable
fun TagWatchListScreen(screenState: WatchListSharedState, pagingSourceConfig: WatchListPagingSource.Config, onEvent: (WatchListSharedStateEvent) -> Unit) {

    WatchListScreen(
        screenState = screenState,
        pagingSourceConfig = pagingSourceConfig,
        onEvent = onEvent,
        topBarContent = { subtitle, filterPagesTitles ->
            WatchListTopBar(
                title = screenState.tag.name,
                subtitle = subtitle,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                filterQuery = screenState.titleFilter,
                visibleActions = {
                    IconButton(onClick = { onEvent(WatchListSharedStateEvent.AddAppToTag(show = true)) }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.menu_tag_apps))
                    }
                },
                dropdownActions = { dismiss, barBounds ->
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.menu_edit)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = R.string.menu_edit)) },
                        onClick = {
                            onEvent(WatchListSharedStateEvent.EditTag(show = true))
                            dismiss()
                        }
                    )

                    var topBarFilterMenu by remember { mutableStateOf(false) }
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.filter)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.FlashOn, contentDescription = stringResource(id = R.string.filter)) },
                        trailingIcon = { Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null) },
                        onClick = { topBarFilterMenu = true }
                    )

                    DropdownMenu(expanded = topBarFilterMenu, onDismissRequest = { topBarFilterMenu = false }) {
                        filterPagesTitles.forEachIndexed { index, title ->
                            DropdownMenuItem(text = { Text(text = title) }, onClick = {
                                onEvent(WatchListSharedStateEvent.FilterById(filterId = index))
                                topBarFilterMenu = false
                                dismiss()
                            })
                        }
                    }

                    SortMenuItem(
                        selectedSortId = screenState.sortId,
                        onChangeSort = { index ->
                            onEvent(WatchListSharedStateEvent.ChangeSort(sortId = index))
                            dismiss()
                        },
                        barBounds = barBounds
                    )
                },
                onEvent = onEvent
            )
        }
    )
}