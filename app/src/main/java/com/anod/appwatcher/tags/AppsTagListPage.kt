package com.anod.appwatcher.tags

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.anod.appwatcher.watchlist.*
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.text.Html
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun AppsTagListPage(args: WatchListPageArgs, onEvent: (WatchListEvent) -> Unit) {
    val viewModel: WatchListViewModel = viewModel(factory = AppsWatchListViewModel.Factory(args))
    val config by remember {
        mutableStateOf(WatchListPagingSource.Config(
                showRecentlyUpdated = viewModel.prefs.showRecentlyUpdated,
                showOnDevice = false,
                showRecentlyInstalled = false
        ))
    }
    val items = viewModel.load(config, initialKey = null).collectAsLazyPagingItems()

    LazyColumn {
        itemsIndexed(items) { index, item ->
            if (item != null) { // TODO: Preload?
                WatchListSectionItem(item, index, onEvent, installedApps = viewModel.installedApps)
            }
        }
    }
}

@Composable
fun WatchListSectionItem(item: SectionItem, index: Int, onEvent: (WatchListEvent) -> Unit, installedApps: InstalledApps, appIconLoader: AppIconLoader = getKoin().get()) {
    when (item) {
        is SectionItem.Header -> when (item.type) {
            is RecentlyInstalledHeader -> SectionHeader(item.type, onClick = { onEvent(WatchListEvent.ItemClick(item, index)) })
            else -> SectionHeader(item.type, onClick = null)
        }
        is SectionItem.App -> AppItem(index, item.appListItem, isLocalApp = item.isLocal, selection = AppViewHolder.Selection.None, installedApps = installedApps, appIconLoader = appIconLoader)
        is SectionItem.OnDevice -> AppItem(index, item.appListItem, isLocalApp = true, selection = AppViewHolder.Selection.None, installedApps = installedApps, appIconLoader = appIconLoader)
        is SectionItem.Recent -> RecentItem()
        is SectionItem.Empty -> EmptyItem()
    }
}

@Composable
fun SectionHeader(item: SectionHeader, onClick: (() -> Unit)? = null) {
    val text = when (item) {
        is NewHeader -> stringResource(R.string.new_updates)
        is RecentlyUpdatedHeader -> stringResource(R.string.recently_updated)
        is WatchingHeader -> stringResource(R.string.watching)
        is RecentlyInstalledHeader -> stringResource(R.string.recently_installed)
        is OnDeviceHeader -> stringResource(R.string.downloaded)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppItem(index: Int, item: AppListItem, isLocalApp: Boolean, selection: AppViewHolder.Selection, installedApps: InstalledApps, appIconLoader: AppIconLoader = getKoin().get()) {
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
            headlineText = {
                Text(text = title)
            },
            supportingText = {
                Column {
                    Row {
                        if (isLocalApp) {
                            Icon(painter = painterResource(id = R.drawable.ic_stat_communication_stay_primary_portrait), contentDescription = null)
                            val versionText: String by remember {
                                mutableStateOf(formatVersionText(app.versionName, app.versionNumber, 0, context))
                            }
                            Text(text = versionText, color = MaterialTheme.colorScheme.primary)
                        } else {
                            if (appItemState.iconRes != 0) {
                                Icon(painter = painterResource(id = appItemState.iconRes), contentDescription = null)
                            }
                            Text(text = appItemState.text, color = appItemState.color)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (app.uploadDate.isNotEmpty()) {
                            Text(text = app.uploadDate)
                        }
                    }
                    if (appItemState.showRecent) {
                        if (isLocalApp) {
                            if (changesHtml.isNotBlank()) {
                                Text(text = changesHtml, modifier = Modifier.alpha(if (item.noNewDetails) 0.4f else 1.0f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        } else {
                            if (changesHtml.isBlank()) {
                                Text(text = stringResource(id = R.string.no_recent_changes), modifier = Modifier.alpha(0.4f))
                            } else {
                                Text(text = changesHtml, modifier = Modifier.alpha(if (item.noNewDetails) 0.4f else 1.0f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
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
fun EmptyItem() {
    Text(text = "EmptyItem")
}

@Preview(showBackground = true)
@Composable
fun WatchListPreview() {
    val appIconLoader = AppIconLoader.Simple(
            LocalContext.current,
            ImageLoader.Builder(LocalContext.current).build()
    )
    val installedApps = InstalledApps.StaticMap(emptyMap())
    val items = listOf(
            SectionItem.Header(type = RecentlyUpdatedHeader),
            SectionItem.App(AppListItem(
                    app = App(
                            rowId = -1,
                            appId = "appId",
                            packageName = "package",
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
            )
    )

    AppTheme {
        LazyColumn {
            itemsIndexed(items) { index, item ->
                WatchListSectionItem(item = item, index = index, onEvent = {}, installedApps = installedApps, appIconLoader = appIconLoader)
            }
        }
    }
}