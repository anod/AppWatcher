package com.anod.appwatcher.watchlist

import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import coil.ImageLoader
import coil.compose.AsyncImage
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.Amber800
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.database.entities.generateTitle
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.utils.AppIconLoader
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.text.Html
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun WatchListPage(pagingSourceConfig: WatchListPagingSource.Config, sortId: Int, titleQuery: String, onEvent: (WatchListEvent) -> Unit) {
    val viewModel: WatchListViewModel = viewModel(factory = AppsWatchListViewModel.Factory(pagingSourceConfig))
    val items = viewModel.pagingData.collectAsLazyPagingItems()

    LaunchedEffect(key1 = sortId) {
        AppLog.d("Refresh list items - sort changed")
        items.refresh()
    }

    var currentQuery by remember { mutableStateOf(titleQuery) }
    LaunchedEffect(titleQuery) {
        if (currentQuery != titleQuery) {
            viewModel.handleEvent(WatchListEvent.FilterByTitle(titleQuery, true))
            AppLog.d("Refresh list items - title query changed '$titleQuery'")
            currentQuery = titleQuery
            items.refresh()
        }
    }

    AppLog.d("Recomposition: WatchListPage [${pagingSourceConfig.hashCode()}, ${sortId}, ${items.hashCode()}, ${viewModel.hashCode()}, ${titleQuery}, ${currentQuery}]")

    LazyColumn {
        if (items.itemCount == 0) {
            item {
                EmptyItem(Modifier.padding(top = 128.dp))
            }
        } else {
            itemsIndexed(
                    items = items,
                    key = { _, item -> item.hashCode() }
            ) { index, item ->
                if (item != null) { // TODO: Preload?
                    WatchListSectionItem(item, index, onEvent, installedApps = viewModel.installedApps)
                } else {
                    Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(MaterialTheme.colorScheme.inverseOnSurface))

                }
            }
        }
    }
}

@Composable
fun WatchListSectionItem(item: SectionItem, index: Int, onEvent: (WatchListEvent) -> Unit, installedApps: InstalledApps, appIconLoader: AppIconLoader = getKoin().get()) {
    when (item) {
        is SectionItem.Header -> when (item.type) {
            is SectionHeader.RecentlyInstalled -> SectionHeader(item.type, onClick = { onEvent(WatchListEvent.ItemClick(item, index)) })
            else -> SectionHeader(item.type, onClick = null)
        }
        is SectionItem.App -> AppItem(item.appListItem, isLocalApp = item.isLocal, selection = AppViewHolder.Selection.None, onClick = { onEvent(WatchListEvent.ItemClick(item, index)) }, installedApps = installedApps, appIconLoader = appIconLoader)
        is SectionItem.OnDevice -> AppItem(item.appListItem, isLocalApp = true, selection = AppViewHolder.Selection.None, onClick = { onEvent(WatchListEvent.ItemClick(item, index)) }, installedApps = installedApps, appIconLoader = appIconLoader)
        is SectionItem.Recent -> RecentItem()
        is SectionItem.Empty -> EmptyItem()
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
            Icon(imageVector = Icons.Default.ArrowRight, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
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

data class AppItemState(val color: Color, val text: String, val iconRes: Int, val showRecent: Boolean)

@Composable
fun VersionText(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
            text = text,
            color = color,
            modifier = modifier,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall
    )
}

@Composable
fun ChangelogText(text: String, noNewDetails: Boolean) {
    Text(
            text = text,
            modifier = Modifier
                    .alpha(if (noNewDetails) 0.4f else 1.0f)
                    .padding(top = 4.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 14.sp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppItem(item: AppListItem, isLocalApp: Boolean, selection: AppViewHolder.Selection, onClick: (() -> Unit), installedApps: InstalledApps, appIconLoader: AppIconLoader = getKoin().get()) {
    val view = LocalView.current
    val context = LocalContext.current
    val app = item.app
    val title: String by remember { mutableStateOf(app.generateTitle(view.resources).toString()) }
    val changesHtml: String by remember {
        if (item.changeDetails?.isNotBlank() == true) {
            mutableStateOf(Html.parse(item.changeDetails)
                    .toString()
                    .replace(newLineRegex, "\n")
                    .removePrefix(app.versionName + "\n")
                    .removePrefix(app.versionName + ":\n")
            )
        } else mutableStateOf("")
    }
    val textColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val packageInfo by remember {
        mutableStateOf(installedApps.packageInfo(app.packageName))
    }
    val appItemState by remember {
        mutableStateOf(calcAppItemState(app, item.recentFlag, textColor, primaryColor, packageInfo, context))
    }

    ListItem(
            modifier = Modifier.clickable(enabled = true, onClick = onClick),
            headlineText = {
                Text(text = title)
            },
            overlineText = { },
            supportingText = {
                Column {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLocalApp) {
                            Icon(painter = painterResource(id = R.drawable.ic_stat_communication_stay_primary_portrait), contentDescription = null)
                            val versionText: String by remember {
                                mutableStateOf(formatVersionText(app.versionName, app.versionNumber, 0, context))
                            }
                            VersionText(text = versionText, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        } else {
                            if (appItemState.iconRes != 0) {
                                Icon(painter = painterResource(id = appItemState.iconRes), contentDescription = null)
                            }
                            VersionText(text = appItemState.text, color = appItemState.color, modifier = Modifier.weight(1f))
                        }
                        if (app.uploadDate.isNotEmpty()) {
                            Text(text = app.uploadDate, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (appItemState.showRecent) {
                        if (isLocalApp) {
                            if (changesHtml.isNotBlank()) {
                                ChangelogText(text = changesHtml, noNewDetails = item.noNewDetails)
                            }
                        } else {
                            if (changesHtml.isBlank()) {
                                ChangelogText(text = stringResource(id = R.string.no_recent_changes), noNewDetails = true)
                            } else {
                                ChangelogText(text = changesHtml, noNewDetails = item.noNewDetails)
                            }
                        }
                    }

                    Divider(modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                    )
                }
            },
            leadingContent = {
                val imageRequest = remember {
                    mutableStateOf(appIconLoader.request(app.iconUrl))
                }
                AsyncImage(
                        model = imageRequest.value,
                        contentDescription = title,
                        imageLoader = appIconLoader.coilLoader,
                        modifier = Modifier.size(40.dp),
                        placeholder = painterResource(id = R.drawable.ic_app_icon_placeholder)
                )
            },
    )
}

private fun calcAppItemState(app: App, recentFlag: Boolean, textColor: Color, primaryColor: Color, packageInfo: InstalledApps.Info, context: Context): AppItemState {
    var color = textColor
    var iconRes = 0
    var text = ""
    when {
        app.versionNumber == 0 -> {
            color = Amber800
            text = context.getString(R.string.updates_not_available)
        }
        packageInfo.isInstalled -> {
            iconRes = R.drawable.ic_stat_communication_stay_primary_portrait
            when {
                app.versionNumber > packageInfo.versionCode -> {
                    color = Amber800
                    text = formatVersionText(packageInfo.versionName, packageInfo.versionCode, app.versionNumber, context)
                }
                app.status == AppInfoMetadata.STATUS_UPDATED || recentFlag -> {
                    color = primaryColor
                    text = formatVersionText(packageInfo.versionName, packageInfo.versionCode, 0, context)
                }
                else -> {
                    text = formatVersionText(packageInfo.versionName, packageInfo.versionCode, 0, context)
                }
            }
        }
        else -> {
            text = if (app.price.isFree) {
                context.getString(R.string.free)
            } else {
                app.price.text
            }

            if (app.status == AppInfoMetadata.STATUS_UPDATED || recentFlag) {
                color = primaryColor
            }
        }
    }

    // Recent changes
    val showRecent = when {
        app.status == AppInfoMetadata.STATUS_UPDATED || recentFlag -> {
            true
        }
        else -> {
            false
        }
    }

    return AppItemState(color, text, iconRes, showRecent)
}

@Composable
fun RecentItem() {
    Text(text = "RecentItem")
}

@Composable
fun EmptyItem(
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
        Text(text = stringResource(id = R.string.watch_list_is_empty), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 24.dp))
        Button(onClick = { }, modifier = Modifier
                .defaultMinSize(minWidth = 188.dp)
                .padding(top = 8.dp)) {
            button1Text()
        }
        if (button2Text != null) {
            Button(onClick = { }, modifier = Modifier
                    .defaultMinSize(minWidth = 188.dp)
                    .padding(top = 8.dp)) {
                button2Text()
            }
        }
        if (button3Text != null) {
            Button(onClick = { }, modifier = Modifier
                    .defaultMinSize(minWidth = 188.dp)
                    .padding(top = 8.dp)) {
                button3Text()
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun WatchListEmptyPreview() {
    val appIconLoader = AppIconLoader.Simple(
            LocalContext.current,
            ImageLoader.Builder(LocalContext.current).build()
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
                                        .background(MaterialTheme.colorScheme.primary)) {
                            Text("On primary", color = MaterialTheme.colorScheme.onPrimary)
                        }

                        Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                        .size(width = 140.dp, height = 40.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer)) {
                            Text("On container", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }

                itemsIndexed(items) { index, item ->
                    WatchListSectionItem(item = item, index = index, onEvent = {}, installedApps = installedApps, appIconLoader = appIconLoader)
                }
            }
        }
    }

}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun WatchListPreview() {
    val appIconLoader = AppIconLoader.Simple(
            LocalContext.current,
            ImageLoader.Builder(LocalContext.current).build()
    )
    val installedApps = InstalledApps.StaticMap(mapOf(
            "package2" to InstalledApps.Info(versionName = "very long long version name consectetur adipiscing elit", versionCode = 11223300),
            "package3" to InstalledApps.Info(versionName = "version name", versionCode = 11223300)
    ))
    val items = listOf(
            SectionItem.Header(type = SectionHeader.RecentlyUpdated),
            SectionItem.App(AppListItem(
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
                    recentFlag = true),
                    isLocal = false
            ),
            SectionItem.App(AppListItem(
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
                    recentFlag = true),
                    isLocal = true
            ),
            SectionItem.App(AppListItem(
                    app = App(
                            rowId = -1,
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
                    recentFlag = true),
                    isLocal = false
            ),
            SectionItem.App(AppListItem(
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
                    recentFlag = true),
                    isLocal = false
            )
    )

    AppTheme(
            customPrimaryColor = Color.Yellow
    ) {
        Surface {
            LazyColumn {
                itemsIndexed(items) { index, item ->
                    WatchListSectionItem(item = item, index = index, onEvent = {}, installedApps = installedApps, appIconLoader = appIconLoader)
                }
            }
        }
    }
}