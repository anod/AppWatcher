package com.anod.appwatcher.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.ImageLoader
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.SearchTopBar
import com.anod.appwatcher.utils.AppIconLoader
import finsky.api.model.Document
import finsky.protos.AppDetails
import finsky.protos.DocDetails
import finsky.protos.DocV2
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.java.KoinJavaComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(screenState: SearchViewState, pagingDataFlow: () -> Flow<PagingData<Document>>, onEvent: (SearchViewEvent) -> Unit, installedApps: InstalledApps, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                SearchTopBar(
                        title = stringResource(id = R.string.search),
                        showSearch = true,
                        onValueChange = { onEvent(SearchViewEvent.SearchQueryChange(query = it)) },
                        onSearchAction = { onEvent(SearchViewEvent.OnSearchEnter(it)) },
                        initialSearchFocus = !screenState.initiateSearch,
                        searchQuery = screenState.searchQuery,
                        onNavigation = { onEvent(SearchViewEvent.OnBackPressed) }
                )
            }
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
                    SearchSingleResult(searchStatus.document, screenState = screenState, onEvent = onEvent, installedApps = installedApps, appIconLoader = appIconLoader)
                }
                is SearchStatus.Error -> RetryButton(onRetryClick = { onEvent(SearchViewEvent.OnSearchEnter(searchStatus.query)) })
                is SearchStatus.NoNetwork -> RetryButton(onRetryClick = { onEvent(SearchViewEvent.OnSearchEnter(searchStatus.query)) })
                is SearchStatus.NoResults -> EmptyResult(query = searchStatus.query)
                is SearchStatus.SearchList -> {
                    val pagingData = remember(searchStatus.query) { pagingDataFlow() }
                    val items = pagingData.collectAsLazyPagingItems()
                    val isEmpty = items.loadState.source.refresh is LoadState.NotLoading && items.itemCount < 1
                    if (isEmpty) {
                        EmptyResult(query = searchStatus.query)
                    } else {
                        SearchResultsPage(items = items, screenState = screenState, onEvent = onEvent, installedApps = installedApps, appIconLoader = appIconLoader)
                    }
                }
            }
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
fun RetryButton(onRetryClick: () -> Unit) {
    Column(
            modifier = Modifier
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
fun SearchSingleResult(document: Document, screenState: SearchViewState, onEvent: (SearchViewEvent) -> Unit, installedApps: InstalledApps, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
    val packageName = document.appDetails.packageName
    val isWatched = remember(packageName, screenState.watchingPackages) {
        screenState.watchingPackages.contains(packageName)
    }
    val packageInfo = remember { installedApps.packageInfo(packageName) }
    MarketAppItem(
            document = document,
            onClick = { onEvent(SearchViewEvent.ItemClick(document)) },
            isWatched = isWatched,
            isInstalled = packageInfo.isInstalled,
            appIconLoader = appIconLoader
    )
}

@Composable
fun SearchResultsPage(items: LazyPagingItems<Document>, screenState: SearchViewState, onEvent: (SearchViewEvent) -> Unit, installedApps: InstalledApps, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
    LazyColumn(
            modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
    ) {
        items(
                items = items,
                key = { item -> item.hashCode() }
        ) { document ->
            if (document != null) { // TODO: Preload?
                val packageName = document.appDetails.packageName
                val isWatched = remember(packageName, screenState.watchingPackages) {
                    screenState.watchingPackages.contains(packageName)
                }
                val packageInfo = remember { installedApps.packageInfo(packageName) }
                MarketAppItem(
                        document = document,
                        onClick = { onEvent(SearchViewEvent.ItemClick(document)) },
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
@Preview
fun LoadingStatePreview() {
    val appIconLoader = AppIconLoader.Simple(
            LocalContext.current,
            ImageLoader.Builder(LocalContext.current).build()
    )
    AppTheme {
        SearchResultsScreen(
                screenState = SearchViewState(searchStatus = SearchStatus.Loading),
                pagingDataFlow = { flowOf() },
                onEvent = { },
                installedApps = InstalledApps.StaticMap(emptyMap()),
                appIconLoader = appIconLoader
        )
    }
}

@Composable
@Preview
fun EmptyStatePreview() {
    val appIconLoader = AppIconLoader.Simple(
            LocalContext.current,
            ImageLoader.Builder(LocalContext.current).build()
    )
    AppTheme {
        SearchResultsScreen(
                screenState = SearchViewState(searchStatus = SearchStatus.NoResults(query = "")),
                pagingDataFlow = { flowOf() },
                onEvent = { },
                installedApps = InstalledApps.StaticMap(emptyMap()),
                appIconLoader = appIconLoader
        )
    }
}

@Composable
@Preview
fun RetryStatePreview() {
    val appIconLoader = AppIconLoader.Simple(
            LocalContext.current,
            ImageLoader.Builder(LocalContext.current).build()
    )
    AppTheme {
        SearchResultsScreen(
                screenState = SearchViewState(searchStatus = SearchStatus.Error("")),
                pagingDataFlow = { flowOf() },
                onEvent = { },
                installedApps = InstalledApps.StaticMap(emptyMap()),
                appIconLoader = appIconLoader
        )
    }
}


@Composable
@Preview
fun SearchSingleResultPreview() {
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
    AppTheme {
        SearchResultsScreen(
                screenState = SearchViewState(searchQuery = "info.anodsplace.appwatcher", searchStatus = SearchStatus.DetailsAvailable(document = doc)),
                pagingDataFlow = { flowOf() },
                onEvent = { },
                installedApps = InstalledApps.StaticMap(mapOf(
                        "info.anodsplace.appwatcher" to InstalledApps.Info(versionCode = 15000, versionName = "1.5.0")
                )),
                appIconLoader = appIconLoader
        )
    }
}