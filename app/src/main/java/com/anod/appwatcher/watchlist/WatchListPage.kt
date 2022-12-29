package com.anod.appwatcher.watchlist

import android.content.Context
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemsIndexed
import coil.ImageLoader
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.Amber800
import com.anod.appwatcher.compose.AppIcon
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.database.entities.generateTitle
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.SelectionState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.text.Html
import org.koin.java.KoinJavaComponent.getKoin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WatchListPage(
    items: LazyPagingItems<SectionItem>,
    isRefreshing: Boolean,
    enablePullToRefresh: Boolean,
    installedApps: InstalledApps,
    onEvent: (WatchListEvent) -> Unit,
    selection: SelectionState = SelectionState(),
    selectionMode: Boolean = false
) {

    AppLog.d("Recomposition: WatchListPage [${items.hashCode()}, ${selection.hashCode()}, ${selectionMode}]")

    val isEmpty = items.loadState.source.refresh is LoadState.NotLoading && items.itemCount < 1
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    SwipeRefresh(modifier = Modifier.fillMaxSize(),
        state = swipeRefreshState,
        swipeEnabled = enablePullToRefresh,
        onRefresh = { onEvent(WatchListEvent.Refresh) }) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (isEmpty) {
                item {
                    EmptyItem(onEvent = onEvent, modifier = Modifier.padding(top = 128.dp))
                }
            } else {
                itemsIndexed(items = items, key = { _, item -> item.hashCode() }) { index, item ->
                    if (item != null) { // TODO: Preload?
                        WatchListSectionItem(
                            modifier = Modifier.animateItemPlacement(),
                            item = item,
                            index = index,
                            onEvent = onEvent,
                            selection = selection,
                            selectionMode = selectionMode,
                            installedApps = installedApps
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
    installedApps: InstalledApps,
    modifier: Modifier = Modifier,
    selection: SelectionState = SelectionState(),
    selectionMode: Boolean = false,
    appIconLoader: AppIconLoader = getKoin().get(),
) {
    when (item) {
        is SectionItem.Header -> when (item.type) {
            is SectionHeader.RecentlyInstalled -> SectionHeader(item.type,
                onClick = { onEvent(WatchListEvent.ItemClick(item, index)) })

            else -> SectionHeader(
                item.type, onClick = null
            )
        }

        is SectionItem.App -> AppItem(
            modifier = modifier,
            item = item.appListItem,
            isLocalApp = item.isLocal,
            onClick = { onEvent(WatchListEvent.ItemClick(item, index)) },
            onLongClick = { onEvent(WatchListEvent.ItemLongClick(item, index)) },
            selection = selection,
            selectionMode = selectionMode,
            installedApps = installedApps,
            appIconLoader = appIconLoader
        )

        is SectionItem.OnDevice -> AppItem(
            modifier = modifier,
            item = item.appListItem,
            isLocalApp = true,
            onClick = { onEvent(WatchListEvent.ItemClick(item, index)) },
            onLongClick = { onEvent(WatchListEvent.ItemLongClick(item, index)) },
            selection = selection,
            selectionMode = selectionMode,
            installedApps = installedApps,
            appIconLoader = appIconLoader
        )

        is SectionItem.Recent -> RecentItem()
        is SectionItem.Empty -> EmptyItem(onEvent = onEvent)
    }
}

@Composable
fun SectionHeader(item: SectionHeader, onClick: (() -> Unit)? = null) {
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
                imageVector = Icons.Default.ArrowRight,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SectionHeaderText(text: String) {
    Text(
        text = text.toUpperCase(locale = Locale.current),
        maxLines = 1,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

private val newLineRegex = Regex("\n+")

data class AppItemState(
    val color: Color, val text: String, val installed: Boolean, val showRecent: Boolean
)

@Composable
fun ChangelogText(text: String, noNewDetails: Boolean) {
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
fun AppItem(
    item: AppListItem,
    isLocalApp: Boolean,
    onClick: (() -> Unit),
    onLongClick: (() -> Unit),
    installedApps: InstalledApps,
    modifier: Modifier = Modifier,
    selection: SelectionState = SelectionState(),
    selectionMode: Boolean = false,
    appIconLoader: AppIconLoader = getKoin().get(),
) {
    val view = LocalView.current
    val context = LocalContext.current
    val app = item.app
    val title: String by remember { mutableStateOf(app.generateTitle(view.resources).toString()) }
    val changesHtml: String by remember {
        if (item.changeDetails?.isNotBlank() == true) {
            mutableStateOf(
                Html.parse(item.changeDetails).toString().replace(newLineRegex, "\n")
                    .removePrefix(app.versionName + "\n").removePrefix(app.versionName + ":\n")
            )
        } else mutableStateOf("")
    }
    val textColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val packageInfo by remember {
        mutableStateOf(installedApps.packageInfo(app.packageName))
    }
    val itemSelection by remember(app.packageName, selectionMode, selection) {
        mutableStateOf(getPackageSelection(app.packageName, selectionMode, selection))
    }
    val appItemState by remember {
        mutableStateOf(
            calcAppItemState(
                app, item.recentFlag, textColor, primaryColor, packageInfo, context
            )
        )
    }

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
                Box {
                    AppIcon(
                        app = app,
                        contentDescription = title,
                        appIconLoader = appIconLoader
                    )
                    SelectedIcon(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        itemSelection = itemSelection
                    )
                }
            } else {
                AppIcon(
                    app = app,
                    contentDescription = title,
                    appIconLoader = appIconLoader
                )
            }
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
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
//                    val versionText: String by remember {
//                        mutableStateOf(formatVersionText(app.versionName, app.versionNumber, 0, context))
//                    }
//                    VersionText(text = versionText, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
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
fun SelectedIcon(modifier: Modifier, itemSelection: AppViewHolder.Selection) {
    AnimatedVisibility(
        modifier = modifier,
        visible = itemSelection == AppViewHolder.Selection.Selected,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        label = "SelectedIconVisibility"

    ) {
        Icon(
            modifier = modifier.size(18.dp),
            painter = painterResource(id = R.drawable.ic_check_circle_selected_18dp),
            contentDescription = stringResource(id = coil.compose.base.R.string.selected),
            tint = Color.Unspecified
        )
    }
}

@Composable
fun Changelog(isLocalApp: Boolean, changesHtml: String, noNewDetails: Boolean) {
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

private fun getPackageSelection(
    packageName: String, selectionMode: Boolean, selection: SelectionState
): AppViewHolder.Selection {
    return if (selectionMode) {
        if (selection.contains(packageName)) AppViewHolder.Selection.Selected else AppViewHolder.Selection.NotSelected
    } else AppViewHolder.Selection.None
}

private fun calcAppItemState(
    app: App,
    recentFlag: Boolean,
    textColor: Color,
    primaryColor: Color,
    packageInfo: InstalledApps.Info,
    context: Context
): AppItemState {
    var color = textColor
    var installed = false
    val text = when {
        app.versionNumber == 0 -> {
            color = Amber800
            context.getString(R.string.updates_not_available)
        }

        packageInfo.isInstalled -> {
            installed = true
            when {
                app.versionNumber > packageInfo.versionCode -> {
                    color = Amber800
                    formatVersionText(
                        packageInfo.versionName, packageInfo.versionCode, app.versionNumber, context
                    )
                }

                app.status == AppInfoMetadata.STATUS_UPDATED || recentFlag -> {
                    color = primaryColor
                    formatVersionText(packageInfo.versionName, packageInfo.versionCode, 0, context)
                }

                else -> {
                    formatVersionText(packageInfo.versionName, packageInfo.versionCode, 0, context)
                }
            }
        }

        else -> {
            if (app.status == AppInfoMetadata.STATUS_UPDATED || recentFlag) {
                color = primaryColor
            }
            if (app.price.isFree) {
                context.getString(R.string.free)
            } else {
                app.price.text
            }
        }
    }

    val showRecent = when {
        app.status == AppInfoMetadata.STATUS_UPDATED || recentFlag -> true
        else -> false
    }

    return AppItemState(color, text, installed, showRecent)
}

@Composable
fun RecentItem() {
    Text(text = "RecentItem")
}

@Composable
fun EmptyItem(
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

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun WatchListEmptyPreview() {
    val appIconLoader = AppIconLoader.Simple(
        LocalContext.current, ImageLoader.Builder(LocalContext.current).build()
    )
    val installedApps = InstalledApps.StaticMap(mapOf())
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
                        installedApps = installedApps,
                        appIconLoader = appIconLoader
                    )
                }
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun WatchListPreview() {
    val appIconLoader = AppIconLoader.Simple(
        LocalContext.current, ImageLoader.Builder(LocalContext.current).build()
    )
    val installedApps = InstalledApps.StaticMap(
        mapOf(
            "package2" to InstalledApps.Info(
                versionName = "very long long version name consectetur adipiscing elit",
                versionCode = 11223300
            ),
            "package3" to InstalledApps.Info(versionName = "version name", versionCode = 11223300)
        )
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
                    updateTime = 0
                ),
                changeDetails = "Nunc aliquam egestas diam, id bibendum massa. Duis vitae lorem nunc. Integer eu elit urna. Phasellus pretium enim ut felis consequat elementum. Cras feugiat sed purus consequat mollis. Vivamus ut urna a augue facilisis aliquam. Cras eget ipsum ex.",
                noNewDetails = true,
                recentFlag = true
            ), isLocal = false
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
                    updateTime = 0
                ),
                changeDetails = "Nunc aliquam egestas diam, id bibendum massa. Duis vitae lorem nunc. Integer eu elit urna. Phasellus pretium enim ut felis consequat elementum. Cras feugiat sed purus consequat mollis. Vivamus ut urna a augue facilisis aliquam. Cras eget ipsum ex.",
                noNewDetails = true,
                recentFlag = true
            ), isLocal = true
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
                    updateTime = 0
                ),
                changeDetails = "Nunc aliquam egestas diam, id bibendum massa. Duis vitae lorem nunc. Integer eu elit urna. Phasellus pretium enim ut felis consequat elementum. Cras feugiat sed purus consequat mollis. Vivamus ut urna a augue facilisis aliquam. Cras eget ipsum ex.",
                noNewDetails = false,
                recentFlag = true
            ), isLocal = true
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
                    updateTime = 0
                ),
                changeDetails = "Nunc aliquam egestas diam, id bibendum massa. Duis vitae lorem nunc. Integer eu elit urna. Phasellus pretium enim ut felis consequat elementum. Cras feugiat sed purus consequat mollis. Vivamus ut urna a augue facilisis aliquam. Cras eget ipsum ex.",
                noNewDetails = false,
                recentFlag = true
            ), isLocal = false
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
                        installedApps = installedApps,
                        appIconLoader = appIconLoader
                    )
                }
            }
        }
    }
}