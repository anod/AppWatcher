package com.anod.appwatcher.wishlist

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
fun WishListScreen(
    screenState: WishListState,
    pagingDataFlow: Flow<PagingData<App>>,
    onEvent: (WishListEvent) -> Unit,
    installedApps: InstalledApps,
    appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get(),
    viewActions: Flow<WishListAction>,
    onActivityAction: (CommonActivityAction) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showSearchView by remember { mutableStateOf(false) }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                SearchTopBar(
                    title = stringResource(id = R.string.wishlist),
                    searchQuery = screenState.nameFilter,
                    hideSearchOnNavigation = false,
                    onNavigation = { onEvent(WishListEvent.OnBackPress) },
                    onValueChange = { onEvent(WishListEvent.OnNameFilter(it)) },
                    onSearchSubmit = { showSearchView = false },
                    showSearch = showSearchView
                )
            }
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
                        WishlistEmpty()
                    } else {
                        WishlistResults(
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
                is WishListAction.ShowTagSnackbar -> {
                    val result = snackbarHostState.showSnackbar(TagSnackbar.Visuals(action.info, context))
                    if (result == SnackbarResult.ActionPerformed) {
                        showTagList = action.info
                    }
                }
                is WishListAction.ActivityAction -> onActivityAction(action.action)
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
fun WishlistResults(items: LazyPagingItems<App>, screenState: WishListState, onEvent: (WishListEvent) -> Unit, installedApps: InstalledApps, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
    LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
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
                    onClick = { onEvent(WishListEvent.SelectApp(app)) },
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
fun WishlistEmpty() {
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