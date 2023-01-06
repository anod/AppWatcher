package com.anod.appwatcher.watchlist

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.FilterMenuAction
import com.anod.appwatcher.compose.OpenDrawerIcon
import com.anod.appwatcher.compose.PlayStoreMyAppsIcon
import com.anod.appwatcher.compose.RefreshIcon
import com.anod.appwatcher.compose.SortMenuItem
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.tags.EditTagDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainState: MainViewState,
    onMainEvent: (MainViewEvent) -> Unit,
    listState: WatchListSharedState,
    pagingSourceConfig: WatchListPagingSource.Config,
    onListEvent: (WatchListSharedStateEvent) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            MainDrawer(
                mainState = mainState,
                onMainEvent = {
                    scope.launch {
                        if (it !is MainViewEvent.AddNewTagDialog) {
                            drawerState.close()
                        }
                        if (it is MainViewEvent.DrawerItemClick && it.id == DrawerItem.Id.Refresh) {
                            onListEvent(WatchListSharedStateEvent.Refresh)
                        } else {
                            onMainEvent(it)
                        }
                    }
                }
            )
        },
        drawerState = drawerState
    ) {
        WatchListScreen(
            screenState = listState,
            pagingSourceConfig = pagingSourceConfig,
            onEvent = onListEvent,
            topBarContent = { subtitle, filterId ->
                MainTopBar(
                    listState = listState,
                    subtitle = subtitle,
                    filterId = filterId,
                    onListEvent = {
                        if (it is WatchListSharedStateEvent.OnBackPressed) {
                            scope.launch {
                                drawerState.open()
                            }
                        } else {
                            onListEvent(it)
                        }
                    },
                )
            }
        )
    }

    if (mainState.showNewTagDialog) {
        EditTagDialog(
            tag = Tag.empty,
            onDismissRequest = { onMainEvent(MainViewEvent.AddNewTagDialog(show = false)) }
        )
    }
}

@Composable
fun MainTopBar(
    listState: WatchListSharedState,
    subtitle: String?,
    filterId: Int,
    onListEvent: (WatchListSharedStateEvent) -> Unit
) {
    WatchListTopBar(
        title = stringResource(id = R.string.app_name),
        subtitle = subtitle,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        filterQuery = listState.titleFilter,
        hideSearchOnNavigation = false,
        navigationIcon = { OpenDrawerIcon() },
        visibleActions = {
            FilterMenuAction(
                filterId = filterId,
                onFilterChange = { index ->
                    onListEvent(WatchListSharedStateEvent.FilterById(filterId = index))
                }
            )
        },
        dropdownActions = { dismiss, barBounds ->
            SortMenuItem(
                selectedSortId = listState.sortId,
                onChangeSort = { index ->
                    onListEvent(WatchListSharedStateEvent.ChangeSort(sortId = index))
                    dismiss()
                },
                barBounds = barBounds
            )

            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.menu_refresh)) },
                leadingIcon = { RefreshIcon() },
                onClick = {
                    onListEvent(WatchListSharedStateEvent.Refresh)
                    dismiss()
                }
            )

            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.play_store_my_apps)) },
                leadingIcon = { PlayStoreMyAppsIcon() },
                onClick = {
                    onListEvent(WatchListSharedStateEvent.PlayStoreMyApps)
                    dismiss()
                }
            )
        },
        onEvent = onListEvent
    )
}
