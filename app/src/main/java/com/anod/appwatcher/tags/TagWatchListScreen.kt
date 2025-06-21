package com.anod.appwatcher.tags

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.EditIcon
import com.anod.appwatcher.compose.FilterMenuItem
import com.anod.appwatcher.compose.PinShortcutIcon
import com.anod.appwatcher.compose.SortMenuItem
import com.anod.appwatcher.compose.TagAppIconButton
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.navigation.TagWatchListNavKey
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.WatchListAction
import com.anod.appwatcher.watchlist.WatchListEvent
import com.anod.appwatcher.watchlist.WatchListPagingSource
import com.anod.appwatcher.watchlist.WatchListScreen
import com.anod.appwatcher.watchlist.WatchListSharedState
import com.anod.appwatcher.watchlist.WatchListStateViewModel
import com.anod.appwatcher.watchlist.WatchListTopBar
import info.anodsplace.framework.app.FoldableDeviceLayout
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.showToast
import info.anodsplace.framework.content.startActivity

@Composable
fun TagWatchListScreenScene(wideLayout: FoldableDeviceLayout, tag: Tag, navigateBack: () -> Unit, navigateTo: (NavKey) -> Unit) {
    val viewModel: WatchListStateViewModel = viewModel(factory =
        WatchListStateViewModel.Factory(
            defaultFilterId = Filters.ALL,
            wideLayout = wideLayout,
            collectRecentlyInstalledApps = false,
            initialTag = tag
        ),
        key = TagWatchListNavKey.toString()
    )
    val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)
    val context = LocalContext.current
    val customPrimaryColor by remember(screenState) {
        derivedStateOf { Color(screenState.tag.color) }
    }
    AppTheme(
        customPrimaryColor = customPrimaryColor,
        theme = viewModel.prefs.theme
    ) {
        val pagingSourceConfig = WatchListPagingSource.Config(
            filterId = screenState.filterId,
            tagId = screenState.tag.id,
            showRecentlyDiscovered = viewModel.prefs.showRecentlyDiscovered,
            showOnDevice = false,
            showRecentlyInstalled = false
        )

        TagWatchListScreen(
            screenState = screenState,
            pagingSourceConfig = pagingSourceConfig,
            onEvent = viewModel::handleEvent,
            installedApps = viewModel.installedApps
        )

        if (screenState.showAppTagDialog) {
            AppsTagDialog(
                tag = screenState.tag,
                onDismissRequest = { viewModel.handleEvent(WatchListEvent.AddAppToTag(show = false)) }
            )
        }

        if (screenState.showEditTagDialog) {
            EditTagDialog(
                tag = screenState.tag,
                onDismissRequest = { viewModel.handleEvent(WatchListEvent.EditTag(show = false)) }
            )
        }
    }

    LaunchedEffect(true) {
        viewModel.viewActions.collect {
            when (it) {
                is WatchListAction.SelectApp -> {}
                is WatchListAction.ShowToast -> context.showToast(it)
                is WatchListAction.StartActivity -> context.startActivity(it)
                WatchListAction.NavigateBack -> navigateBack()
                is WatchListAction.NavigateTo -> navigateTo(it.navKey)
            }
        }
    }
}

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