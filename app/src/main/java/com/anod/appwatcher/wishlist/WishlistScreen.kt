package com.anod.appwatcher.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.SearchTopBar
import com.anod.appwatcher.search.MarketAppItem
import com.anod.appwatcher.search.RetryButton
import com.anod.appwatcher.utils.AppIconLoader
import finsky.api.model.Document
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.flow.Flow
import org.koin.java.KoinJavaComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishListScreen(
        screenState: WishListState,
        pagingDataFlow: Flow<PagingData<Document>>,
        onEvent: (WishListEvent) -> Unit,
        installedApps: InstalledApps,
        appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()
) {

    Scaffold(
            topBar = {
                SearchTopBar(
                        title = stringResource(id = R.string.wishlist),
                        searchQuery = screenState.nameFilter,
                        onNavigation = { onEvent(WishListEvent.OnBackPress) },
                        onValueChange = { onEvent(WishListEvent.OnNameFilter(it)) },
                        showSearch = false
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
}

@Composable
fun WishlistResults(items: LazyPagingItems<Document>, screenState: WishListState, onEvent: (WishListEvent) -> Unit, installedApps: InstalledApps, appIconLoader: AppIconLoader = KoinJavaComponent.getKoin().get()) {
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
                        onClick = { onEvent(WishListEvent.ItemClick(document)) },
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