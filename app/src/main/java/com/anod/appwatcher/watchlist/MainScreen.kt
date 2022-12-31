package com.anod.appwatcher.watchlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.SortMenuItem
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
        drawerContent = { MainDrawer(
            mainState = mainState,
            onMainEvent = {
                scope.launch {
                    drawerState.close()
                    if (it is MainViewEvent.DrawerItemClick && it.id == DrawerItem.Id.Refresh) {
                        onListEvent(WatchListSharedStateEvent.Refresh)
                    } else {
                        onMainEvent(it)
                    }
                }
            }
        ) },
        drawerState = drawerState
    ) {
        WatchListScreen(
            screenState = listState,
            pagingSourceConfig = pagingSourceConfig,
            onEvent = onListEvent,
            topBarContent = { subtitle, filterPagesTitles ->
                var topBarFilterMenu by remember { mutableStateOf(false) }

                WatchListTopBar(
                    title = stringResource(id = R.string.app_name),
                    subtitle = subtitle,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    filterQuery = listState.titleFilter,
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(id = R.string.menu)
                        )
                    },
                    visibleActions = {
                        IconButton(onClick = { topBarFilterMenu = true }) {
                            Icon(imageVector = Icons.Default.FlashOn, contentDescription = stringResource(id = R.string.filter))
                        }

                        DropdownMenu(expanded = topBarFilterMenu, onDismissRequest = { topBarFilterMenu = false }) {
                            filterPagesTitles.forEachIndexed { index, title ->
                                DropdownMenuItem(text = { Text(text = title) }, onClick = {
                                    onListEvent(WatchListSharedStateEvent.FilterById(filterId = index))
                                    topBarFilterMenu = false
                                })
                            }
                        }
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
                            leadingIcon = { Icon(imageVector = Icons.Default.Refresh, contentDescription = stringResource(id = R.string.menu_refresh)) },
                            onClick = {
                                onListEvent(WatchListSharedStateEvent.Refresh)
                                dismiss()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.play_store_my_apps)) },
                            leadingIcon = { Icon(imageVector = Icons.Default.Store, contentDescription = stringResource(id = R.string.play_store_my_apps)) },
                            onClick = {
                                onListEvent(WatchListSharedStateEvent.PlayStoreMyApps)
                                dismiss()
                            }
                        )

                    },
                    onEvent = {
                        if (it is WatchListSharedStateEvent.OnBackPressed) {
                            scope.launch {
                                drawerState.open()
                            }
                        } else {
                            onListEvent(it)
                        }
                    }
                )


            }
        )
    }
}