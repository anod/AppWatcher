package com.anod.appwatcher.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.ImageLoader
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.TopBarSearchField
import com.anod.appwatcher.utils.AppIconLoader
import finsky.api.model.Document
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.java.KoinJavaComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(screenState: SearchViewState, pagingDataFlow: () -> Flow<PagingData<Document>>, onEvent: (SearchViewEvent) -> Unit, installedApps: InstalledApps, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
    Scaffold(
            topBar = {
                SmallTopAppBar(
                        title = {
                            TopBarSearchField(
                                    query = screenState.searchQuery,
                                    onValueChange = { onEvent(SearchViewEvent.SearchQueryChange(query = it)) },
                                    onSearchAction = { onEvent(SearchViewEvent.OnSearchEnter(it)) },
                                    requestFocus = true
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { onEvent(SearchViewEvent.OnBackPressed) }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                            }
                        }
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
                SearchStatus.Error -> RetryButton(query = screenState.searchQuery, onEvent = onEvent)
                SearchStatus.NoNetwork -> RetryButton(query = screenState.searchQuery, onEvent = onEvent)
                SearchStatus.NoResults -> EmptyResult(query = screenState.searchQuery)
                SearchStatus.SearchList -> {
                    val items = pagingDataFlow().collectAsLazyPagingItems()
                    SearchResultsPage(items = items, screenState = screenState, onEvent = onEvent, installedApps = installedApps, appIconLoader = appIconLoader)
                }
            }
        }
    }
}

@Composable
fun EmptyResult(query: String) {
    Text(
            modifier = Modifier
                    .padding(start = 32.dp, end = 32.dp),
            text = if (query.isNotEmpty()) stringResource(R.string.no_result_found, query) else stringResource(R.string.search_for_app),
            textAlign = TextAlign.Center
    )
}

@Composable
fun RetryButton(query: String, onEvent: (SearchViewEvent) -> Unit) {
    Column {
        Text(text = stringResource(id = R.string.problem_occurred))
        Button(onClick = { onEvent(SearchViewEvent.OnSearchEnter(query)) }) {
            Text(text = stringResource(id = R.string.retry))
        }
    }
}

@Composable
fun SearchSingleResult(document: Document, screenState: SearchViewState, onEvent: (SearchViewEvent) -> Unit, installedApps: InstalledApps, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
    val packageName = document.appDetails.packageName
    val isWatched = remember(packageName) {
        screenState.watchingPackages.contains(packageName)
    }
    MarketAppItem(
            document = document,
            onClick = { onEvent(SearchViewEvent.ItemClick(document)) },
            isWatched = isWatched,
            installedApps = installedApps,
            appIconLoader = appIconLoader
    )
}

@Composable
fun SearchResultsPage(items: LazyPagingItems<Document>, screenState: SearchViewState, onEvent: (SearchViewEvent) -> Unit, installedApps: InstalledApps, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
    LazyColumn(
            modifier = Modifier.fillMaxSize(),
    ) {
        items(
                items = items,
                key = { item -> item.hashCode() }
        ) { document ->
            if (document != null) { // TODO: Preload?
                val packageName = document.appDetails.packageName
                val isWatched = remember(packageName) {
                    screenState.watchingPackages.contains(packageName)
                }
                MarketAppItem(
                        document = document,
                        onClick = { onEvent(SearchViewEvent.ItemClick(document)) },
                        isWatched = isWatched,
                        installedApps = installedApps,
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
                screenState = SearchViewState(searchStatus = SearchStatus.NoResults),
                pagingDataFlow = { flowOf() },
                onEvent = { },
                installedApps = InstalledApps.StaticMap(emptyMap()),
                appIconLoader = appIconLoader
        )
    }
}