package com.anod.appwatcher.watchlist

import android.accounts.Account
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AccountSelectionRequest
import com.anod.appwatcher.accounts.AccountSelectionResult
import com.anod.appwatcher.compose.FilterMenuAction
import com.anod.appwatcher.compose.OpenDrawerIcon
import com.anod.appwatcher.compose.PlayStoreMyAppsIcon
import com.anod.appwatcher.compose.RefreshIcon
import com.anod.appwatcher.compose.SortMenuItem
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.navigation.SceneNavKey
import com.anod.appwatcher.navigation.asNavKey
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.tags.EditTagDialog
import info.anodsplace.framework.app.FoldableDeviceLayout
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.onScreenCommonAction
import info.anodsplace.framework.content.showToast
import info.anodsplace.framework.content.startActivity
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.AppPermissions
import info.anodsplace.permissions.toRequestInput

@Composable
fun MainScreenScene(prefs: Preferences, wideLayout: FoldableDeviceLayout, navigateBack: () -> Unit, navigateTo: (NavKey) -> Unit) {
    val mainViewModel: MainViewModel = viewModel()
    val listViewModel: WatchListStateViewModel = viewModel(factory =
        WatchListStateViewModel.Factory(
            defaultFilterId = prefs.defaultMainFilterId,
            wideLayout = wideLayout,
            collectRecentlyInstalledApps = prefs.showRecent,
            initialTag = Tag.empty
        ),
        key = SceneNavKey.Main.toString()
    )

    val mainState by mainViewModel.viewStates.collectAsState(initial = mainViewModel.viewState)
    val listState by listViewModel.viewStates.collectAsState(initial = listViewModel.viewState)
    val drawerValue = if (mainState.isDrawerOpen) DrawerValue.Open else DrawerValue.Closed
    val drawerState = rememberDrawerState(initialValue = drawerValue)
    val context = LocalContext.current

    val notificationPermissionRequest = rememberLauncherForActivityResult(AppPermissions.Request()) {
        val enabled = it[AppPermission.PostNotification.value] ?: false
        mainViewModel.handleEvent(MainViewEvent.NotificationPermissionResult(enabled))
    }

    val accountSelectionRequest = rememberLauncherForActivityResult(AccountSelectionRequest()) {
        mainViewModel.handleEvent(MainViewEvent.SetAccount(it))
    }

    LaunchedEffect(true) {
        mainViewModel.viewActions.collect { action ->
            if (action is MainViewAction.DrawerState) {
                if (action.isOpen) {
                    drawerState.open()
                } else {
                    drawerState.close()
                }
            } else {
                onMainAction(
                    action = action,
                    context = context,
                    accountSelectionRequest = accountSelectionRequest,
                    notificationPermissionRequest = notificationPermissionRequest,
                    navigateTo = navigateTo
                )
            }
        }
    }
    LaunchedEffect(true) {
        listViewModel.viewActions.collect { action ->
            context.onScreenCommonAction(action, navigateBack = navigateBack, navigateTo = { navigateTo(it.asNavKey) })
        }
    }
    val pagingSourceConfig = WatchListPagingSource.Config(
        filterId = listState.filterId,
        tagId = null,
        showRecentlyDiscovered = prefs.showRecentlyDiscovered,
        showOnDevice = prefs.showOnDevice,
        showRecentlyInstalled = prefs.showRecent
    )
    MainScreen(
        mainState = mainState,
        drawerState = drawerState,
        onMainEvent = mainViewModel::handleEvent,
        listState = listState,
        pagingSourceConfig = pagingSourceConfig,
        onListEvent = listViewModel::handleEvent,
        installedApps = listViewModel.installedApps
    )

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        mainViewModel.handleEvent(MainViewEvent.OnResume)
    }
}

private fun onMainAction(
    action: MainViewAction,
    context: Context,
    accountSelectionRequest: ManagedActivityResultLauncher<Account?, AccountSelectionResult>,
    notificationPermissionRequest: ActivityResultLauncher<AppPermissions.Request.Input>,
    navigateTo: (NavKey) -> Unit
) {
    when (action) {
        is MainViewAction.NavigateTo -> {
            when (action.id) {
                DrawerItem.Id.Add -> navigateTo(SceneNavKey.Search())
                DrawerItem.Id.Installed -> navigateTo(SceneNavKey.Installed(importMode = false))
                DrawerItem.Id.Refresh -> {}
                DrawerItem.Id.Settings -> navigateTo(SceneNavKey.Settings)
                DrawerItem.Id.Wishlist -> navigateTo(SceneNavKey.WishList)
                DrawerItem.Id.Purchases -> navigateTo(SceneNavKey.PurchaseHistory)
            }
        }
        is MainViewAction.NavigateToTag -> navigateTo(SceneNavKey.TagWatchList(tag = action.tag))
        MainViewAction.RequestNotificationPermission -> notificationPermissionRequest.launch(AppPermission.PostNotification.toRequestInput())
        is MainViewAction.ChooseAccount -> accountSelectionRequest.launch(action.currentAccount)
        is MainViewAction.ShowToast -> context.showToast(action)
        is MainViewAction.DrawerState -> { }
        is MainViewAction.StartActivity -> context.startActivity(action)
    }
}
@Composable
fun MainScreen(
    mainState: MainViewState,
    onMainEvent: (MainViewEvent) -> Unit,
    listState: WatchListSharedState,
    pagingSourceConfig: WatchListPagingSource.Config,
    onListEvent: (WatchListEvent) -> Unit,
    installedApps: InstalledApps,
    drawerState: DrawerState
) {
    ModalNavigationDrawer(
        drawerContent = {
            MainDrawer(
                mainState = mainState,
                onMainEvent = {
                    if (it !is MainViewEvent.AddNewTagDialog) {
                        onMainEvent(MainViewEvent.DrawerState(isOpen = false))
                    }
                    if (it is MainViewEvent.DrawerItemClick && it.id == DrawerItem.Id.Refresh) {
                        onListEvent(WatchListEvent.Refresh)
                    } else {
                        onMainEvent(it)
                    }
                }
            )
        },
        gesturesEnabled = true,
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
                        if (it is WatchListEvent.OnBackPressed) {
                            if (listState.showSearch) {
                                onListEvent(WatchListEvent.ShowSearch(show = false))
                            } else {
                                onMainEvent(MainViewEvent.DrawerState(isOpen = true))
                            }
                        } else {
                            onListEvent(it)
                        }
                    },
                )
            },
            installedApps = installedApps,
            listContext = "main"
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
    onListEvent: (WatchListEvent) -> Unit
) {
    WatchListTopBar(
        title = stringResource(id = R.string.app_name),
        subtitle = subtitle,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        filterQuery = listState.titleFilter,
        showSearch = listState.showSearch,
        navigationIcon = { OpenDrawerIcon() },
        visibleActions = {
            FilterMenuAction(
                filterId = filterId,
                onFilterChange = { index ->
                    onListEvent(WatchListEvent.FilterById(filterId = index))
                }
            )
        },
        dropdownActions = { dismiss, barBounds ->
            SortMenuItem(
                selectedSortId = listState.sortId,
                onChangeSort = { index ->
                    onListEvent(WatchListEvent.ChangeSort(sortId = index))
                    dismiss()
                },
                barBounds = barBounds
            )

            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.menu_refresh)) },
                leadingIcon = { RefreshIcon() },
                onClick = {
                    onListEvent(WatchListEvent.Refresh)
                    dismiss()
                }
            )

            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.play_store_my_apps)) },
                leadingIcon = { PlayStoreMyAppsIcon() },
                onClick = {
                    onListEvent(WatchListEvent.PlayStoreMyApps)
                    dismiss()
                }
            )
        },
        onEvent = onListEvent
    )
}