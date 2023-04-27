package com.anod.appwatcher.watchlist

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.ImageLoader
import coil.compose.AsyncImage
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppIconImage
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.WatchedIcon
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.details.rememberAppItemState
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.SelectionState
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import info.anodsplace.framework.content.InstalledApps
import org.koin.java.KoinJavaComponent.getKoin


enum class AppItemSelection {
    None, Disabled, NotSelected, Selected;

    val enabled: Boolean
        get() = this != None && this != Disabled
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WatchListPage(
    items: LazyPagingItems<SectionItem>,
    isRefreshing: Boolean,
    enablePullToRefresh: Boolean,
    listContext: String,
    onEvent: (WatchListEvent) -> Unit,
    selection: SelectionState = SelectionState(),
    selectionMode: Boolean = false,
    recentlyInstalledApps: List<App>? = null,
) {
    val isEmpty = items.loadState.source.refresh is LoadState.NotLoading && items.itemCount < 1
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    val recentlyInstalledAppsHashCode = remember(recentlyInstalledApps) { recentlyInstalledApps?.hashCode() ?: 0 }
    SwipeRefresh(modifier = Modifier.fillMaxSize(),
        state = swipeRefreshState,
        swipeEnabled = enablePullToRefresh,
        onRefresh = { onEvent(WatchListEvent.Refresh) }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = WindowInsets.navigationBars.asPaddingValues()
        ) {
            if (isEmpty) {
                item(contentType = "empty-state") {
                    EmptyItem(onEvent = onEvent, modifier = Modifier.padding(top = 128.dp))
                }
            } else {
                items(
                    count = items.itemCount,
                    key = { index ->
                        when (val item = items.peek(index)) {
                            null -> "null-$index"
                            is SectionItem.Recent -> "$listContext-${item.sectionKey}-$recentlyInstalledAppsHashCode"
                            else -> "$listContext-${item.sectionKey}"
                        }
                    },
                    contentType = { index ->
                        items.peek(index)?.contentType
                    }
                ) { index ->
                    val item = items[index]
                    if (item != null) { // TODO: Preload?
                        WatchListSectionItem(
                            modifier = Modifier.animateItemPlacement(),
                            item = item,
                            index = index,
                            onEvent = onEvent,
                            selection = selection,
                            selectionMode = selectionMode,
                            recentlyInstalledApps = recentlyInstalledApps
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
    }

}

@Composable
fun WatchListSectionItem(
    item: SectionItem,
    index: Int,
    onEvent: (WatchListEvent) -> Unit,
    modifier: Modifier = Modifier,
    selection: SelectionState = SelectionState(),
    selectionMode: Boolean = false,
    appIconLoader: AppIconLoader = getKoin().get(),
    recentlyInstalledApps: List<App>? = null,
) {
    when (item) {
        is SectionItem.Header -> when (item.type) {
            is SectionHeader.RecentlyInstalled -> SectionHeader(
                item.type,
                onClick = { onEvent(WatchListEvent.SectionHeaderClick(item.type)) }
            )

            else -> SectionHeader(
                item.type, onClick = null
            )
        }

        is SectionItem.App -> AppItem(
            modifier = modifier,
            item = item.appListItem,
            changesHtml = item.changesHtml,
            isLocalApp = item.isLocal,
            onClick = { onEvent(WatchListEvent.AppClick(item.appListItem.app, index)) },
            onLongClick = { onEvent(WatchListEvent.AppLongClick(item.appListItem.app, index)) },
            selection = selection,
            selectionMode = selectionMode,
            packageInfo = item.packageInfo,
            appIconLoader = appIconLoader
        )

        is SectionItem.OnDevice -> AppItem(
            modifier = modifier,
            item = item.appListItem,
            changesHtml = item.changesHtml,
            isLocalApp = true,
            onClick = { onEvent(WatchListEvent.AppClick(item.appListItem.app, index)) },
            onLongClick = { onEvent(WatchListEvent.AppLongClick(item.appListItem.app, index)) },
            selection = selection,
            selectionMode = selectionMode,
            packageInfo = item.packageInfo,
            appIconLoader = appIconLoader
        )

        is SectionItem.Recent -> RecentItem(onEvent = onEvent, recentApps = recentlyInstalledApps)
        is SectionItem.Empty -> EmptyItem(onEvent = onEvent)
    }
}

@Composable
private fun SectionHeader(item: SectionHeader, onClick: (() -> Unit)? = null) {
    val text = when (item) {
        is SectionHeader.New -> stringResource(R.string.new_updates)
        is SectionHeader.RecentlyUpdated -> stringResource(R.string.recently_updated)
        is SectionHeader.Watching -> stringResource(R.string.watching)
        is SectionHeader.RecentlyInstalled -> stringResource(R.string.recently_installed)
        is SectionHeader.OnDevice -> stringResource(R.string.downloaded)
    }
    if (onClick == null) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionHeaderText(text = text)
        }
    } else {
        Row(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionHeaderText(text = text)
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SectionHeaderText(text: String) {
    Text(
        text = text.toUpperCase(locale = Locale.current),
        maxLines = 1,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun ChangelogText(text: String, noNewDetails: Boolean) {
    Text(
        text = text,
        modifier = Modifier
            .alpha(if (noNewDetails) 0.4f else 1.0f)
            .padding(top = 4.dp),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 16.sp
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppItem(
    item: AppListItem,
    changesHtml: String,
    isLocalApp: Boolean,
    onClick: (() -> Unit),
    onLongClick: (() -> Unit),
    packageInfo: InstalledApps.Info,
    modifier: Modifier = Modifier,
    selection: SelectionState = SelectionState(),
    selectionMode: Boolean = false,
    appIconLoader: AppIconLoader = getKoin().get(),
) {
    val app = item.app
    val itemSelection by remember(app.packageName, selectionMode, selection) {
        derivedStateOf { getPackageSelection(app.packageName, selectionMode, selection) }
    }
    val appItemState = rememberAppItemState(
        app, item.recentFlag, packageInfo
    )

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    enabled = true, onClick = onClick, onLongClick = onLongClick
                )
                .heightIn(min = 68.dp)
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
        ) {

            if (selectionMode) {
                val status = selection.getExtra(app.packageName)["status"]
                Box {
                    AppIconImage(
                        app = app,
                        contentDescription = app.title,
                        appIconLoader = appIconLoader
                    )
                    SelectedIcon(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .placeholder(
                                visible = status == "p",
                                color = MaterialTheme.colorScheme.primary
                            ),
                        itemSelection = itemSelection,
                        tint = if (status == "e") MaterialTheme.colorScheme.error else Color.Unspecified
                    )
                }
            } else {
                AppIconImage(
                    app = app,
                    contentDescription = app.title,
                    appIconLoader = appIconLoader
                )
            }
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(text = app.title, style = MaterialTheme.typography.bodyLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top
                ) {
                    if (isLocalApp && app.rowId > 0) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = stringResource(id = R.string.watched),
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    if (appItemState.installed || isLocalApp) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = stringResource(id = R.string.installed),
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    Text(
                        text = appItemState.text,
                        color = appItemState.color,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (app.uploadDate.isNotEmpty()) {
                        Text(
                            text = app.uploadDate,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                if (isLocalApp || appItemState.showRecent) {
                    Changelog(
                        isLocalApp = isLocalApp,
                        changesHtml = changesHtml,
                        noNewDetails = item.noNewDetails
                    )
                }
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 72.dp)
                .align(alignment = Alignment.BottomEnd),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SelectedIcon(modifier: Modifier, itemSelection: AppItemSelection, tint: Color = Color.Unspecified) {
    AnimatedVisibility(
        modifier = modifier,
        visible = itemSelection == AppItemSelection.Selected,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        label = "SelectedIconVisibility"

    ) {
        Icon(
            modifier = modifier.size(18.dp),
            painter = painterResource(id = R.drawable.ic_check_circle_selected_18dp),
            contentDescription = stringResource(id = coil.compose.base.R.string.selected),
            tint = tint
        )
    }
}

@Composable
private fun Changelog(isLocalApp: Boolean, changesHtml: String, noNewDetails: Boolean) {
    if (isLocalApp) {
        ChangelogText(text = changesHtml, noNewDetails = noNewDetails)
    } else {
        if (changesHtml.isBlank()) {
            ChangelogText(
                text = stringResource(id = R.string.no_recent_changes), noNewDetails = true
            )
        } else {
            ChangelogText(text = changesHtml, noNewDetails = noNewDetails)
        }
    }
}

@Composable
private fun RecentItem(
    recentApps: List<App>? = null,
    onEvent: (WatchListEvent) -> Unit,
    appIconLoader: AppIconLoader = getKoin().get(),
) {
    RecentItemRow(
        loading = recentApps == null,
        recentApps = recentApps ?: emptyList(),
        onEvent = onEvent,
        appIconLoader = appIconLoader
    )
}

@Composable
private fun RecentItemRow(
    loading: Boolean,
    recentApps: List<App>,
    onEvent: (WatchListEvent) -> Unit,
    appIconLoader: AppIconLoader = getKoin().get()
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 96.dp)
            .fillMaxWidth()
            .padding(start = 6.dp, end = 8.dp)
            .horizontalScroll(scrollState)
    ) {
        val placeholderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        if (loading) {
            (0..4).forEach { _ ->
                RecentItemAppCard(
                    app = null,
                    onClick = {},
                    placeholderColor = placeholderColor,
                    appIconLoader = appIconLoader
                )
            }
        } else {
            recentApps.forEachIndexed { index, app ->
                RecentItemAppCard(
                    app = app,
                    onClick = { onEvent(WatchListEvent.AppClick(app, index)) },
                    placeholderColor = placeholderColor,
                    appIconLoader = appIconLoader
                )
            }
        }
    }
}

@Composable
private fun RecentItemAppCard(app: App?, onClick: (() -> Unit), placeholderColor: Color, appIconLoader: AppIconLoader = getKoin().get()) {
    Card(
        modifier = Modifier
            .defaultMinSize(minHeight = 116.dp)
            .width(96.dp)
            .padding(start = 2.dp, end = 2.dp, top = 2.dp, bottom = 2.dp)
            .clickable(enabled = app != null, onClick = onClick)
    ) {
        if (app == null) {
            AsyncImage(
                model = R.drawable.ic_app_icon_placeholder,
                contentDescription = "",
                imageLoader = appIconLoader.coilLoader,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally)
                    .size(56.dp)
                    .placeholder(
                        visible = true,
                        color = placeholderColor,
                        shape = CircleShape,
//                        highlight = PlaceholderHighlight.fade(),
                    )
            )
        } else {
            AppIconImage(
                app = app,
                contentDescription = app.title,
                size = 56.dp,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally),
                appIconLoader = appIconLoader
            )
        }
        Text(
            text = app?.title ?: "",
            maxLines = 2,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 2.dp)
                .align(Alignment.CenterHorizontally)
                .defaultMinSize(minHeight = 40.dp)
                .placeholder(
                    visible = app == null,
                    color = placeholderColor,
                    //   highlight = PlaceholderHighlight.fade(),
                ),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            overflow = TextOverflow.Ellipsis
        )
        if (app != null && app.rowId > 0) {
            WatchedIcon(
                unwatch = false,
                modifier = Modifier
                    .size(20.dp)
                    .padding(start = 8.dp, bottom = 4.dp),
                contentDescription = stringResource(id = R.string.watched)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .padding(start = 8.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun EmptyItem(
    onEvent: (WatchListEvent) -> Unit,
    modifier: Modifier = Modifier,
    button1Text: @Composable () -> Unit = { Text(text = stringResource(id = R.string.search_for_an_app)) },
    button2Text: @Composable (() -> Unit)? = { Text(text = stringResource(id = R.string.import_installed)) },
    button3Text: @Composable (() -> Unit)? = { Text(text = stringResource(id = R.string.share_from_play_store)) },
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.ic_empty_box), contentDescription = null)
        Text(
            text = stringResource(id = R.string.watch_list_is_empty),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 24.dp)
        )
        Button(
            onClick = { onEvent(WatchListEvent.EmptyButton(1)) },
            modifier = Modifier
                .defaultMinSize(minWidth = 188.dp)
                .padding(top = 8.dp)
        ) {
            button1Text()
        }
        if (button2Text != null) {
            Button(
                onClick = { onEvent(WatchListEvent.EmptyButton(2)) },
                modifier = Modifier
                    .defaultMinSize(minWidth = 188.dp)
                    .padding(top = 8.dp)
            ) {
                button2Text()
            }
        }
        if (button3Text != null) {
            Button(
                onClick = { onEvent(WatchListEvent.EmptyButton(3)) },
                modifier = Modifier
                    .defaultMinSize(minWidth = 188.dp)
                    .padding(top = 8.dp)
            ) {
                button3Text()
            }
        }
    }
}

private fun getPackageSelection(
    packageName: String, selectionMode: Boolean, selection: SelectionState
): AppItemSelection {
    return if (selectionMode) {
        if (selection.contains(packageName)) AppItemSelection.Selected else AppItemSelection.NotSelected
    } else AppItemSelection.None
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun WatchListEmptyPreview() {
    val appIconLoader = AppIconLoader.Simple(
        LocalContext.current, ImageLoader.Builder(LocalContext.current).build()
    )
    val items = listOf(SectionItem.Empty)

    AppTheme(
        customPrimaryColor = Color.Yellow
    ) {
        Surface {
            LazyColumn {
                item {
                    Row {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 140.dp, height = 40.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Text("On primary", color = MaterialTheme.colorScheme.onPrimary)
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(width = 140.dp, height = 40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text(
                                "On container", color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                itemsIndexed(items) { index, item ->
                    WatchListSectionItem(
                        item = item,
                        index = index,
                        onEvent = {},
                        appIconLoader = appIconLoader
                    )
                }
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun WatchListPreview() {
    val appIconLoader = AppIconLoader.Simple(
        LocalContext.current, ImageLoader.Builder(LocalContext.current).build()
    )

    val items = listOf(
        SectionItem.Header(type = SectionHeader.RecentlyUpdated), SectionItem.App(
            AppListItem(
                app = App(
                    rowId = -1,
                    appId = "appId0",
                    packageName = "package0",
                    versionNumber = 11223344,
                    versionName = "very long long version name",
                    title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                    uploadTime = 0,
                    uploadDate = "20 Sept, 2017 yo",
                    appType = "app",
                    creator = "Banana man",
                    detailsUrl = "url",
                    iconUrl = "",
                    price = Price("", "", 0),
                    status = 0,
                    syncTime = 0
                ),
                changeDetails = "Nunc aliquam egestas diam, id bibendum massa. Duis vitae lorem nunc. Integer eu elit urna. Phasellus pretium enim ut felis consequat elementum. Cras feugiat sed purus consequat mollis. Vivamus ut urna a augue facilisis aliquam. Cras eget ipsum ex.",
                noNewDetails = true,
                recentFlag = true
            ),
            isLocal = false,
            packageInfo = InstalledApps.Info(0, "")
        ), SectionItem.App(
            AppListItem(
                app = App(
                    rowId = -1,
                    appId = "appId1",
                    packageName = "package1",
                    versionNumber = 11223344,
                    versionName = "very long long version name",
                    title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                    uploadTime = 0,
                    uploadDate = "20 Sept, 2017 yo",
                    appType = "app",
                    creator = "Banana man",
                    detailsUrl = "url",
                    iconUrl = "",
                    price = Price("", "", 0),
                    status = 0,
                    syncTime = 0
                ),
                changeDetails = "Nunc aliquam egestas diam, id bibendum massa. Duis vitae lorem nunc. Integer eu elit urna. Phasellus pretium enim ut felis consequat elementum. Cras feugiat sed purus consequat mollis. Vivamus ut urna a augue facilisis aliquam. Cras eget ipsum ex.",
                noNewDetails = true,
                recentFlag = true
            ),
            isLocal = true,
            packageInfo = InstalledApps.Info(0, "")
        ), SectionItem.App(
            AppListItem(
                app = App(
                    rowId = 22,
                    appId = "appId2",
                    packageName = "package2",
                    versionNumber = 11223300,
                    versionName = "very long long version name",
                    title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                    uploadTime = 0,
                    uploadDate = "20 Sept, 2017 yo",
                    appType = "app",
                    creator = "Banana man",
                    detailsUrl = "url",
                    iconUrl = "",
                    price = Price("", "", 0),
                    status = 0,
                    syncTime = 0
                ),
                changeDetails = "Nunc aliquam egestas diam, id bibendum massa. Duis vitae lorem nunc. Integer eu elit urna. Phasellus pretium enim ut felis consequat elementum. Cras feugiat sed purus consequat mollis. Vivamus ut urna a augue facilisis aliquam. Cras eget ipsum ex.",
                noNewDetails = false,
                recentFlag = true
            ),
            isLocal = true,
            packageInfo = InstalledApps.Info(
                versionName = "very long long version name consectetur adipiscing elit",
                versionCode = 11223300
            )
        ), SectionItem.App(
            AppListItem(
                app = App(
                    rowId = -1,
                    appId = "appId3",
                    packageName = "package3",
                    versionNumber = 11223344,
                    versionName = "very long long version name",
                    title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                    uploadTime = 0,
                    uploadDate = "20 Sept, 2017 yo",
                    appType = "app",
                    creator = "Banana man",
                    detailsUrl = "url",
                    iconUrl = "",
                    price = Price("", "", 0),
                    status = 0,
                    syncTime = 0
                ),
                changeDetails = "Nunc aliquam egestas diam, id bibendum massa. Duis vitae lorem nunc. Integer eu elit urna. Phasellus pretium enim ut felis consequat elementum. Cras feugiat sed purus consequat mollis. Vivamus ut urna a augue facilisis aliquam. Cras eget ipsum ex.",
                noNewDetails = false,
                recentFlag = true
            ),
            isLocal = false,
            packageInfo = InstalledApps.Info(versionName = "version name", versionCode = 11223300)
        )
    )
    val selectionState = SelectionState()
    selectionState.selectKey("package3", true)
    AppTheme(
        customPrimaryColor = Color.Yellow
    ) {
        Surface {
            LazyColumn {
                itemsIndexed(items) { index, item ->
                    WatchListSectionItem(
                        item = item,
                        index = index,
                        onEvent = {},
                        selection = selectionState,
                        selectionMode = true,
                        appIconLoader = appIconLoader
                    )
                }
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun WatchListPreviewRecent() {
    val appIconLoader = AppIconLoader.Simple(
        LocalContext.current, ImageLoader.Builder(LocalContext.current).build()
    )

    AppTheme {
        Surface {
            RecentItemRow(
                loading = false,
                recentApps = listOf(
                    App(
                        rowId = -1,
                        appId = "appId3",
                        packageName = "package3",
                        versionNumber = 11223344,
                        versionName = "very long long version name",
                        title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                        uploadTime = 0,
                        uploadDate = "20 Sept, 2017 yo",
                        appType = "app",
                        creator = "Banana man",
                        detailsUrl = "url",
                        iconUrl = "",
                        price = Price("", "", 0),
                        status = 0,
                        syncTime = 0
                    ),
                    App(
                        rowId = 22,
                        appId = "appId2",
                        packageName = "package2",
                        versionNumber = 11223300,
                        versionName = "very long long version name",
                        title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                        uploadTime = 0,
                        uploadDate = "20 Sept, 2017 yo",
                        appType = "app",
                        creator = "Banana man",
                        detailsUrl = "url",
                        iconUrl = "",
                        price = Price("", "", 0),
                        status = 0,
                        syncTime = 0
                    ),
                    App(
                        rowId = 22,
                        appId = "appId2",
                        packageName = "package2",
                        versionNumber = 11223300,
                        versionName = "Short",
                        title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                        uploadTime = 0,
                        uploadDate = "20 Sept, 2017 yo",
                        appType = "app",
                        creator = "Banana man",
                        detailsUrl = "url",
                        iconUrl = "",
                        price = Price("", "", 0),
                        status = 0,
                        syncTime = 0
                    ),
                    App(
                        rowId = -1,
                        appId = "appId3",
                        packageName = "package3",
                        versionNumber = 11223344,
                        versionName = "Short",
                        title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                        uploadTime = 0,
                        uploadDate = "20 Sept, 2017 yo",
                        appType = "app",
                        creator = "Banana man",
                        detailsUrl = "url",
                        iconUrl = "",
                        price = Price("", "", 0),
                        status = 0,
                        syncTime = 0
                    ),
                ),
                onEvent = { },
                appIconLoader = appIconLoader
            )
        }
    }
}


@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun WatchListPreviewRecentLoading() {
    val appIconLoader = AppIconLoader.Simple(
        LocalContext.current, ImageLoader.Builder(LocalContext.current).build()
    )

    AppTheme {
        Surface {
            RecentItemRow(
                loading = true,
                recentApps = listOf(),
                onEvent = { },
                appIconLoader = appIconLoader
            )
        }
    }
}