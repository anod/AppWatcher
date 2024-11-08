package com.anod.appwatcher.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.paging.compose.itemKey
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.SearchTopBar
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.search.ListItem
import com.anod.appwatcher.search.MarketAppItem
import com.anod.appwatcher.search.RetryButton
import com.anod.appwatcher.tags.TagSelectionDialog
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.AppIconLoader
import info.anodsplace.framework.content.CommonActivityAction
import kotlinx.coroutines.flow.Flow
import org.koin.java.KoinJavaComponent

@Composable
fun HistoryListScreen(
    screenState: HistoryListState,
    pagingDataFlow: Flow<PagingData<ListItem>>,
    onEvent: (HistoryListEvent) -> Unit,
    viewActions: Flow<HistoryListAction>,
    onActivityAction: (CommonActivityAction) -> Unit,
    appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get(),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showSearchView by remember { mutableStateOf(false) }
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues())
            )
        },
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
                is LoadState.Loading -> ListLoadProgress()
                is LoadState.Error -> {
                    RetryButton(onRetryClick = {
                        items.refresh()
                    })
                }
                is LoadState.NotLoading -> {
                    val isEmpty = items.itemCount < 1
                    if (isEmpty) {
                        if (items.loadState.append is LoadState.Loading) {
                            ListLoadProgress()
                        } else {
                            HistoryListEmpty()
                        }
                    } else {
                        HistoryListResults(
                            items = items,
                            onEvent = onEvent,
                            appIconLoader = appIconLoader
                        )
                    }
                }
            }
        }
    }

    var showTagList: App? by remember { mutableStateOf(null) }
    LaunchedEffect(key1 = viewActions, key2 = onActivityAction) {
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
private fun ListLoadProgress() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun HistoryListResults(items: LazyPagingItems<ListItem>, onEvent: (HistoryListEvent) -> Unit, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = WindowInsets.navigationBars.asPaddingValues()
    ) {
        items(
            count = items.itemCount,
            key = items.itemKey { "history-${it.stableKey}" }
        ) { index ->
            val item = items[index]
            if (item != null) {
                MarketAppItem(
                    app = item.app,
                    onClick = { onEvent(HistoryListEvent.SelectApp(item.app)) },
                    isWatched = item.isWatched,
                    isInstalled = item.isInstalled,
                    appIconLoader = appIconLoader
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.inverseOnSurface)
                )
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