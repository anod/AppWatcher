package com.anod.appwatcher.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.CommonActivityAction
import com.anod.appwatcher.compose.SearchTopBar
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.search.MarketAppItem
import com.anod.appwatcher.search.RetryButton
import com.anod.appwatcher.tags.TagSelectionDialog
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.AppIconLoader
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.flow.Flow
import org.koin.java.KoinJavaComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryListScreen(
    screenState: HistoryListState,
    pagingDataFlow: Flow<PagingData<App>>,
    onEvent: (HistoryListEvent) -> Unit,
    installedApps: InstalledApps,
    appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get(),
    viewActions: Flow<HistoryListAction>,
    onActivityAction: (CommonActivityAction) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showSearchView by remember { mutableStateOf(false) }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            SearchTopBar(
                title = stringResource(id = R.string.purchase_history),
                searchQuery = screenState.nameFilter,
                hideSearchOnNavigation = false,
                onNavigation = { onEvent(HistoryListEvent.OnBackPress) },
                onValueChange = { onEvent(HistoryListEvent.OnNameFilter(it)) },
                onSearchSubmit = { showSearchView = false },
                showSearch = showSearchView
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            val items = pagingDataFlow.collectAsLazyPagingItems()
            when (items.loadState.refresh) {
                is LoadState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is LoadState.Error -> {
                    RetryButton(onRetryClick = {
                        items.refresh()
                    })
                }
                is LoadState.NotLoading -> {
                    val isEmpty = items.itemCount < 1
                    if (isEmpty) {
                        HistoryListEmpty()
                    } else {
                        HistoryListResults(
                                items = items,
                                screenState = screenState,
                                onEvent = onEvent,
                                installedApps = installedApps,
                                appIconLoader = appIconLoader
                        )
                    }
                }
            }
        }
    }

    var showTagList: App? by remember { mutableStateOf(null) }
    LaunchedEffect(key1 = viewActions) {
        viewActions.collect { action ->
            when (action) {
                is HistoryListAction.ShowTagSnackbar -> {
                    val result = snackbarHostState.showSnackbar(TagSnackbar.Visuals(action.info, context))
                    if (result == SnackbarResult.ActionPerformed) {
                        showTagList = action.info
                    }
                }
                is HistoryListAction.ActivityAction -> onActivityAction(action.action)
            }
        }
    }

    if (showTagList != null) {
        TagSelectionDialog(
            appId = showTagList!!.appId,
            appTitle = showTagList!!.title,
            onDismissRequest = {
                showTagList = null
            }
        )
    }
}

@Composable
fun HistoryListResults(items: LazyPagingItems<App>, screenState: HistoryListState, onEvent: (HistoryListEvent) -> Unit, installedApps: InstalledApps, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = WindowInsets.navigationBars.asPaddingValues()
    ) {
        items(
                items = items,
                key = { item -> item.hashCode() }
        ) { app ->
            if (app != null) { // TODO: Preload?
                val packageName = app.packageName
                val isWatched = remember(packageName, screenState.watchingPackages) {
                    screenState.watchingPackages.contains(packageName)
                }
                val packageInfo = remember { installedApps.packageInfo(packageName) }
                MarketAppItem(
                    app = app,
                    onClick = { onEvent(HistoryListEvent.SelectApp(app)) },
                    isWatched = isWatched,
                    isInstalled = packageInfo.isInstalled,
                    appIconLoader = appIconLoader
                )
            } else {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.inverseOnSurface))

            }
        }
    }
}

@Composable
fun HistoryListEmpty() {
    Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, end = 32.dp),
            contentAlignment = Alignment.Center
    ) {
        Text(
                text = stringResource(R.string.wish_list_is_empty),
                textAlign = TextAlign.Center
        )
    }
}