package com.anod.appwatcher.tags

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.anod.appwatcher.R
import com.anod.appwatcher.watchlist.*

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
                            onEvent(WatchListSharedStateEvent.EditTag(show = true))
                            dismiss()
                        }
                    )
                },
                onEvent = onEvent
            )
        }
    )
}