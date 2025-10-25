package com.anod.appwatcher.search

import android.accounts.Account
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.ImageLoader
import com.anod.appwatcher.R
import com.anod.appwatcher.accounts.AccountSelectionDialogData
import com.anod.appwatcher.accounts.AccountSelectionRequest
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.SearchTopBar
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.navigation.SceneNavKey
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.PlainShowSnackbarData
import com.anod.appwatcher.utils.date.UploadDateParserCache
import finsky.api.Document
import finsky.protos.AppDetails
import finsky.protos.DocDetails
import finsky.protos.DocV2
import info.anodsplace.framework.app.FoldableDeviceLayout
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.content.ScreenCommonAction
import info.anodsplace.framework.content.onScreenCommonAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent

fun SceneNavKey.Search.toViewState(wideLayout: FoldableDeviceLayout) = SearchViewState(
    wideLayout = wideLayout,
    searchQuery = this.keyword,
    initiateSearch = this.initiateSearch,
    isPackageSearch = this.isPackageSearch,
    isShareSource = this.isShareSource,
    hasFocus = this.focus
)

@Composable
fun SearchResultsScreenScene(initialState: SearchViewState, navigateBack: () -> Unit = {}) {
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory(initialState))
    val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)
    val accountSelectionRequest = rememberLauncherForActivityResult(AccountSelectionRequest()) {
        viewModel.handleEvent(SearchViewEvent.SetAccount(it))
    }
    SearchResultsScreen(
        screenState = screenState,
        pagingDataFlow = { viewModel.pagingData },
        onEvent = viewModel::handleEvent,
        viewActions = viewModel.viewActions,
        onShowAccountDialog = { accountSelectionRequest.launch(it) },
        navigateBack = navigateBack
    )
}

@Composable
fun SearchResultsScreen(
    screenState: SearchViewState,
    pagingDataFlow: () -> Flow<PagingData<ListItem>>,
    onEvent: (SearchViewEvent) -> Unit,
    viewActions: Flow<ScreenCommonAction>,
    onShowAccountDialog: (account: Account?) -> Unit = { },
    navigateBack: () -> Unit = {},
    appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get(),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(WindowInsets.navigationBars.asPaddingValues())
            )
        },
        topBar = {
            SearchTopBar(
                title = stringResource(id = R.string.search),
                showSearch = true,
                hideSearchOnNavigation = false,
                onValueChange = { onEvent(SearchViewEvent.SearchQueryChange(query = it)) },
                onSearchSubmit = { onEvent(SearchViewEvent.OnSearchEnter(it)) },
                initialSearchFocus = !screenState.initiateSearch,
                searchQuery = screenState.searchQuery,
                onNavigation = { onEvent(SearchViewEvent.OnBackPressed) }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        val searchStatus = screenState.searchStatus
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = if (searchStatus is SearchStatus.DetailsAvailable || searchStatus is SearchStatus.SearchList) Alignment.TopStart else Alignment.Center
        ) {
            when (searchStatus) {
                SearchStatus.Loading -> {
                    CircularProgressIndicator()
                }
                is SearchStatus.DetailsAvailable -> {
                    SearchSingleResult(searchStatus.listItem, onEvent = onEvent, appIconLoader = appIconLoader)
                }
                is SearchStatus.Error -> RetryButton(onRetryClick = { onEvent(SearchViewEvent.OnSearchEnter(searchStatus.query)) })
                is SearchStatus.NoNetwork -> RetryButton(onRetryClick = { onEvent(SearchViewEvent.OnSearchEnter(searchStatus.query)) })
                is SearchStatus.NoResults -> EmptyResult(query = searchStatus.query)
                is SearchStatus.SearchList -> {
                    val pagingData = remember(searchStatus.query) { pagingDataFlow() }
                    val items = pagingData.collectAsLazyPagingItems()
                    val isError = items.loadState.source.refresh is LoadState.Error
                    val isEmpty = (items.loadState.source.refresh is LoadState.NotLoading && items.itemCount < 1)
                    if (isError) {
                        RetryButton(onRetryClick = { onEvent(SearchViewEvent.OnSearchEnter(searchStatus.query)) }, fillMaxSize = true)
                    } else if (isEmpty) {
                        EmptyResult(query = searchStatus.query)
                    } else {
                        SearchResultsPage(items = items, onEvent = onEvent, appIconLoader = appIconLoader)
                    }
                }
            }
        }
    }

    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = true, key2 = onShowAccountDialog) {
        viewActions.collect { action ->
            context.onScreenCommonAction(
                action,
                navigateBack = navigateBack,
                navigateTo = { },
                showSnackbar = {
                    if (it is PlainShowSnackbarData) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = it.message,
                                duration = it.duration
                            )
                            if (it.exitScreen) {
                                navigateBack()
                            }
                        }
                    }
                },
                showDialog = { dialogData ->
                    if (dialogData is AccountSelectionDialogData) {
                        onShowAccountDialog(dialogData.currentAccount)
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyResult(query: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 32.dp, end = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (query.isNotEmpty()) stringResource(R.string.no_result_found, query) else stringResource(R.string.search_for_app),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RetryButton(onRetryClick: () -> Unit, fillMaxSize: Boolean = false) {
    Column(
        modifier = Modifier
            .apply {
                if (fillMaxSize) fillMaxSize()
            }
            .padding(start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.problem_occurred),
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetryClick) {
            Text(text = stringResource(id = R.string.retry))
        }
    }
}

@Composable
fun SearchSingleResult(listItem: ListItem, onEvent: (SearchViewEvent) -> Unit, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
    MarketAppItem(
        app = listItem.app,
        onClick = { onEvent(SearchViewEvent.SelectApp(listItem.app)) },
        isWatched = listItem.isWatched,
        isInstalled = listItem.isInstalled,
        appIconLoader = appIconLoader
    )
}

@Composable
fun SearchResultsPage(items: LazyPagingItems<ListItem>, onEvent: (SearchViewEvent) -> Unit, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
    ) {
        items(
            count = items.itemCount,
            key = items.itemKey { "search-${it.stableKey}" }
        ) { index ->
            val item = items[index]
            if (item != null) { // TODO: Preload?
                MarketAppItem(
                    app = item.app,
                    onClick = { onEvent(SearchViewEvent.SelectApp(app = item.app)) },
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
@Preview
private fun LoadingStatePreview() {
    val appIconLoader = AppIconLoader.Simple(
        LocalContext.current,
        ImageLoader.Builder(LocalContext.current).build()
    )
    AppTheme {
        SearchResultsScreen(
            screenState = SearchViewState(searchStatus = SearchStatus.Loading),
            pagingDataFlow = { flowOf() },
            onEvent = { },
            viewActions = flowOf(),
            appIconLoader = appIconLoader,
        )
    }
}

@Composable
@Preview
private fun EmptyStatePreview() {
    val appIconLoader = AppIconLoader.Simple(
        LocalContext.current,
        ImageLoader.Builder(LocalContext.current).build()
    )
    AppTheme {
        SearchResultsScreen(
            screenState = SearchViewState(searchStatus = SearchStatus.NoResults(query = "")),
            pagingDataFlow = { flowOf() },
            onEvent = { },
            viewActions = flowOf(),
            appIconLoader = appIconLoader,
        )
    }
}

@Composable
@Preview
private fun RetryStatePreview() {
    val appIconLoader = AppIconLoader.Simple(
        LocalContext.current,
        ImageLoader.Builder(LocalContext.current).build()
    )
    AppTheme {
        SearchResultsScreen(
            screenState = SearchViewState(searchStatus = SearchStatus.Error("")),
            pagingDataFlow = { flowOf() },
            onEvent = { },
            viewActions = flowOf(),
            appIconLoader = appIconLoader,
        )
    }
}

@Composable
@Preview
private fun SearchSingleResultPreview() {
    val appIconLoader = AppIconLoader.Simple(
        LocalContext.current,
        ImageLoader.Builder(LocalContext.current).build()
    )
    val doc = Document(
        doc = DocV2.newBuilder().run {
            title = "App Watcher"
            creator = "Me"
            details = DocDetails.newBuilder().run {
                appDetails = AppDetails.newBuilder().run {
                    uploadDate = "25 Aug 2022"
                    packageName = "info.anodsplace.appwatcher"
                    build()
                }
                build()
            }
            build()
        }
    )
    val app = App(doc, UploadDateParserCache())
    AppTheme {
        SearchResultsScreen(
            screenState = SearchViewState(
                searchQuery = "info.anodsplace.appwatcher", searchStatus = SearchStatus.DetailsAvailable(
                    listItem = ListItem(
                        document = doc,
                        app = app,
                        installedInfo = InstalledApps.Info(versionCode = 15000, versionName = "1.5.0")
                    )
                )
            ),
            pagingDataFlow = { flowOf() },
            onEvent = { },
            viewActions = flowOf(),
            appIconLoader = appIconLoader,
        )
    }
}