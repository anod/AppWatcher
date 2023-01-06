package com.anod.appwatcher.tags

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.TagAppIconButton
import com.anod.appwatcher.compose.EditIcon
import com.anod.appwatcher.compose.FilterMenuItem
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
        topBarContent = { subtitle, filterId ->
            TagWatchListTopBar(
                screenState = screenState,
                filterId = filterId,
                subtitle = subtitle,
                onEvent = onEvent,
            )
        }
    )
}

@Composable
fun TagWatchListTopBar(
    screenState: WatchListSharedState,
    filterId: Int,
    subtitle: String?,
    onEvent: (WatchListSharedStateEvent) -> Unit,
) {
    WatchListTopBar(
        title = screenState.tag.name,
        subtitle = subtitle,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        filterQuery = screenState.titleFilter,
        hideSearchOnNavigation = true,
        visibleActions = {
            TagAppIconButton(onClick = { onEvent(WatchListSharedStateEvent.AddAppToTag(show = true)) })
        },
        dropdownActions = { dismiss, barBounds ->
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.menu_edit)) },
                leadingIcon = { EditIcon() },
                onClick = {
                    onEvent(WatchListSharedStateEvent.EditTag(show = true))
                    dismiss()
                }
            )

            FilterMenuItem(
                filterId = filterId,
                onFilterChange = { index ->
                    onEvent(WatchListSharedStateEvent.FilterById(filterId = index))
                    dismiss()
                },
                barBounds = barBounds
            )

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
