package com.anod.appwatcher.watchlist

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.anod.appwatcher.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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
        drawerContent = { WatchListDrawer(
            mainState = mainState,
            onMainEvent = onMainEvent
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
                    filterTitles = filterPagesTitles,
                    filterQuery = listState.titleFilter,
                    sortId = listState.sortId,
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