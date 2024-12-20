package com.anod.appwatcher.tags

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.EditIcon
import com.anod.appwatcher.compose.FilterMenuItem
import com.anod.appwatcher.compose.PinShortcutIcon
import com.anod.appwatcher.compose.SortMenuItem
import com.anod.appwatcher.compose.TagAppIconButton
import com.anod.appwatcher.watchlist.WatchListEvent
import com.anod.appwatcher.watchlist.WatchListPagingSource
import com.anod.appwatcher.watchlist.WatchListScreen
import com.anod.appwatcher.watchlist.WatchListSharedState
import com.anod.appwatcher.watchlist.WatchListTopBar
import info.anodsplace.framework.content.InstalledApps

@Composable
fun TagWatchListScreen(
    screenState: WatchListSharedState,
    pagingSourceConfig: WatchListPagingSource.Config,
    onEvent: (WatchListEvent) -> Unit,
    installedApps: InstalledApps
) {
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
        },
        installedApps = installedApps,
        listContext = "tag-${screenState.tag.id}"
    )
}

@Composable
fun TagWatchListTopBar(
    screenState: WatchListSharedState,
    filterId: Int,
    subtitle: String?,
    onEvent: (WatchListEvent) -> Unit,
) {
    WatchListTopBar(
        title = if (screenState.tag.isEmpty) stringResource(R.string.untagged) else screenState.tag.name,
        subtitle = subtitle,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        filterQuery = screenState.titleFilter,
        showSearch = screenState.showSearch,
        visibleActions = {
            if (!screenState.tag.isEmpty) {
                TagAppIconButton(onClick = { onEvent(WatchListEvent.AddAppToTag(show = true)) })
            }
        },
        dropdownActions = { dismiss, barBounds ->
            if (!screenState.tag.isEmpty) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.menu_edit)) },
                    leadingIcon = { EditIcon() },
                    onClick = {
                        onEvent(WatchListEvent.EditTag(show = true))
                        dismiss()
                    }
                )
            }

            FilterMenuItem(
                filterId = filterId,
                onFilterChange = { index ->
                    onEvent(WatchListEvent.FilterById(filterId = index))
                    dismiss()
                },
                barBounds = barBounds
            )

            SortMenuItem(
                selectedSortId = screenState.sortId,
                onChangeSort = { index ->
                    onEvent(WatchListEvent.ChangeSort(sortId = index))
                    dismiss()
                },
                barBounds = barBounds
            )

            if (screenState.isRequestPinShortcutSupported) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.pin_shortcut)) },
                    leadingIcon = { PinShortcutIcon() },
                    onClick = {
                        onEvent(WatchListEvent.PinTagShortcut)
                        dismiss()
                    }
                )
            }
        },
        onEvent = onEvent
    )
}