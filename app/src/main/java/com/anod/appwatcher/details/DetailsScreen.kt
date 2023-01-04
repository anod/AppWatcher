package com.anod.appwatcher.details

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ShareCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.CommonActivityAction
import com.anod.appwatcher.compose.DropdownMenuAction
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.tags.TagSelectionDialog
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.StoreIntent
import info.anodsplace.applog.AppLog
import info.anodsplace.compose.AutoSizeText
import info.anodsplace.compose.toHtmlAnnotatedString
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.text.Html
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private val iconSizeBig = 64.dp
private val iconSizeSmall = 32.dp

@Composable
fun DetailsScreen(appId: String, rowId: Int, detailsUrl: String, onDismissRequest: () -> Unit, onCommonActivityAction: (action: CommonActivityAction) -> Unit) {
    val viewModel: DetailsViewModel = viewModel(factory = DetailsViewModel.Factory(
        argAppId = appId,
        argRowId = rowId,
        argDetailsUrl = detailsUrl
    ))

    val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)
    val customPrimaryColor by remember(screenState.customPrimaryColor) {
        derivedStateOf { screenState.customPrimaryColor }
    }
    AppTheme(
        customPrimaryColor = customPrimaryColor?.let { Color(it) },
        updateSystemBars = false,
        useSurfaceAsPrimary = true
    ) {
        DetailsScreenContent(
            screenState = screenState,
            onEvent = { viewModel.handleEvent(it) },
            installedApps = viewModel.installedApps,
            modifier = Modifier.fillMaxSize(),
            viewActions = viewModel.viewActions,
            onDismissRequest = onDismissRequest,
            onCommonActivityAction = onCommonActivityAction
        )
    }
}

@Composable
fun DetailsDialog(appId: String, rowId: Int, detailsUrl: String, onDismissRequest: () -> Unit, onCommonActivityAction: (action: CommonActivityAction) -> Unit) {
    val viewModel: DetailsViewModel = viewModel(key = "details-$appId-$rowId", factory = DetailsViewModel.Factory(
        argAppId = appId,
        argRowId = rowId,
        argDetailsUrl = detailsUrl
    ))

    val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)
    val customPrimaryColor by remember(screenState.customPrimaryColor) {
        derivedStateOf { screenState.customPrimaryColor }
    }
    AppTheme(
        customPrimaryColor = customPrimaryColor?.let { Color(it) },
        updateSystemBars = false,
        useSurfaceAsPrimary = true
    ) {
        Dialog(onDismissRequest = onDismissRequest) {
            DetailsScreenContent(
                screenState = screenState,
                viewActions = viewModel.viewActions,
                onEvent = { viewModel.handleEvent(it) },
                onCommonActivityAction = { onCommonActivityAction (it) },
                installedApps = viewModel.installedApps,
                onDismissRequest = onDismissRequest,
                modifier = Modifier.fillMaxHeight(fraction = 0.9f)
            )
        }
    }
}

private fun createAppChooser(appInfo: App, recentChange: AppChange, context: Context): Intent {
    val builder = ShareCompat.IntentBuilder(context)

    val changes = if (recentChange.details.isBlank()) "" else "${recentChange.details}\n\n"
    val text = context.getString(R.string.share_text, changes, String.format(StoreIntent.URL_WEB_PLAY_STORE, appInfo.packageName))

    builder.setSubject(context.getString(R.string.share_subject, appInfo.title, appInfo.versionName))
    builder.setText(text)
    builder.setType("text/plain")
    return builder.createChooserIntent()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun DetailsScreenContent(
    screenState: DetailsScreenState,
    onEvent: (DetailsScreenEvent) -> Unit,
    installedApps: InstalledApps,
    modifier: Modifier,
    viewActions: Flow<DetailsScreenAction>,
    onDismissRequest: () -> Unit,
    onCommonActivityAction: (CommonActivityAction) -> Unit
) {
    LaunchedEffect(key1 = true) {
        onEvent(DetailsScreenEvent.LoadChangelog)
    }

    val titleVisibility by remember { mutableStateOf(0.0f) }

    val surfaceColor: Color = if (screenState.customPrimaryColor != null)
        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface

    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = screenState.customPrimaryColor != null,
                enter = scaleIn(
                    animationSpec = tween(100, 0, FastOutSlowInEasing),
                    initialScale = 0f,
                    transformOrigin = TransformOrigin(0.1f, 0.8f)
                ),
                label = "HeaderBackground"
            ) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(144.dp) // 60 + 64
                    .align(Alignment.TopStart)
                    .background(color = surfaceColor)
                )
            }

            Column {
                DetailsTopAppBar(
                    titleVisibility = titleVisibility,
                    screenState = screenState,
                    onEvent = onEvent,
                    containerColor = Color.Transparent
                )
                DetailsHeader(
                    screenState = screenState,
                    containerColor = Color.Transparent
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 60.dp, top = 4.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (screenState.isInstalled || screenState.isLocalApp) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = stringResource(id = R.string.installed),
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    if (screenState.app != null) {
                        val appItemState = rememberAppItemState(
                            app = screenState.app,
                            recentFlag = false,
                            installedApps = installedApps
                        )
                        Text(
                            text = appItemState.text,
                            color = appItemState.color,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                when (screenState.changelogState) {
                    ChangelogLoadState.Initial -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            LinearProgressIndicator()
                        }
                    }
                    ChangelogLoadState.Complete -> DetailsChangelog(screenState = screenState)
                    ChangelogLoadState.RemoteError -> {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = stringResource(id = R.string.problem_occurred))
                            Button(onClick = { onEvent(DetailsScreenEvent.ReloadChangelog) }) {
                                Text(text = stringResource(id = R.string.retry))
                            }
                        }
                    }
                }
            }

            SmallFloatingActionButton(
                onClick = { onEvent(DetailsScreenEvent.PlayStore) },
                content = {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = stringResource(id = R.string.open_play_store))
                },
                modifier = Modifier
                    .padding(top = 118.dp, end = 16.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }

    val context = LocalContext.current
    var showTagList: AppInfo? by remember { mutableStateOf(null) }
    LaunchedEffect(key1 = true) {
        viewActions.collect { action ->
            when (action) {
                DetailsScreenAction.Dismiss -> {
                    onDismissRequest()
                }
                DetailsScreenAction.Share -> {
                    if (screenState.app != null) {
                        onCommonActivityAction(
                            CommonActivityAction.StartActivity(
                                intent = createAppChooser(
                                    screenState.app,
                                    screenState.changelogs.firstOrNull() ?: AppChange.empty,
                                    context
                                )
                            )
                        )
                    }
                }
                is DetailsScreenAction.ActivityAction -> onCommonActivityAction(action.action)
                is DetailsScreenAction.ShowTagSnackbar -> {
                    val result = snackBarHostState.showSnackbar(TagSnackbar.Visuals(action.appInfo, context))
                    if (result == SnackbarResult.ActionPerformed) {
                        showTagList = action.appInfo
                    }
                }
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
private fun DetailsChangelog(screenState: DetailsScreenState) {
    AppLog.d("Details collecting changelogState $screenState.changelogState")
    LazyColumn {
        items(screenState.changelogs.size) { i ->
            val change = screenState.changelogs[i]
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${change.versionName} (${change.versionCode})",
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = change.uploadDate,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                SelectionContainer(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    val text = if (LocalView.current.isInEditMode) {
                        if (change.details.isEmpty())
                            AnnotatedString(stringResource(id = R.string.no_recent_changes))
                        else
                            AnnotatedString(Html.parse(change.details).toString())
                    } else {
                        if (change.details.isEmpty())
                            AnnotatedString(stringResource(id = R.string.no_recent_changes))
                        else
                            change.details.toHtmlAnnotatedString()
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsHeader(
    screenState: DetailsScreenState,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Row(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .background(color = containerColor)
    ) {
        DetailsAppIcon(
            appIconState = screenState.appIconState,
            modifier = Modifier
                .size(iconSizeBig)
                .padding(start = 16.dp)
        )
        SelectionContainer {
            Column(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp)
            ) {
                if (screenState.app != null) {
                    AutoSizeText(
                        text = screenState.title,
                        color = contentColor,
                        style = MaterialTheme.typography.headlineSmall,
                        maxFontSize = 24.sp,
                        maxLines = 1,
                        // overflow = TextOverflow.Ellipsis
                    )
                    if (screenState.app.creator.isNotEmpty()) {
                        Text(
                            text = screenState.app.creator,
                            color = contentColor,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (screenState.app.uploadDate.isNotEmpty()) {
                        Text(
                            text = screenState.app.uploadDate,
                            color = contentColor,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsAppIcon(appIconState: AppIconState, modifier: Modifier) {
    AppLog.d("Details collecting appIconState $appIconState")
    when (appIconState) {
        is AppIconState.Loaded -> DetailsIconApp(bitmap = appIconState.drawable.bitmap, modifier = modifier)
        AppIconState.Default -> DetailsIconPlaceHolder(modifier = modifier)
        AppIconState.Initial -> DetailsIconPlaceHolder(modifier = modifier)
    }
}

@Composable
private fun DetailsIconPlaceHolder(modifier: Modifier) {
    Icon(
        painter = painterResource(id = R.drawable.ic_app_icon_placeholder),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = modifier
    )
}

@Composable
private fun DetailsIconApp(bitmap: Bitmap, modifier: Modifier) {
    Icon(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsTopAppBar(
    titleVisibility: Float,
    screenState: DetailsScreenState,
    onEvent: (DetailsScreenEvent) -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    TopAppBar(
        title = {
            if (titleVisibility > 0) {
                Row(
                    modifier = Modifier.alpha(titleVisibility)
                ) {
                    DetailsAppIcon(appIconState = screenState.appIconState, modifier = Modifier.size(iconSizeSmall))
                    Text(
                        text = screenState.title,
                        color = contentColor,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor,
        ),
        navigationIcon = {
            IconButton(onClick = {
                onEvent(DetailsScreenEvent.OnBackPressed)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.back)
                )
            }
        },
        actions = {
            IconButton(
                onClick = { onEvent(DetailsScreenEvent.WatchAppToggle) },
                enabled = screenState.fetchedRemoteDocument
            ) {
                Icon(
                    imageVector = if (screenState.isWatched) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = stringResource(id = if (screenState.isWatched) R.string.watched else R.string.menu_add)
                )
            }

            if (screenState.tagsMenuItems.isNotEmpty()) {
                var showTagsMenu: Boolean by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { showTagsMenu = true },
                    enabled = screenState.fetchedRemoteDocument
                ) {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = stringResource(id = R.string.tag)
                    )
                }

                DropdownMenu(expanded = showTagsMenu, onDismissRequest = { showTagsMenu = false }) {
                    screenState.tagsMenuItems.forEach { (tag, checked) ->
                        DropdownMenuItem(
                            text = { Text(text = tag.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Label,
                                    contentDescription = tag.name,
                                    tint = Color(tag.color)
                                )
                            },
                            trailingIcon = {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { onEvent(DetailsScreenEvent.UpdateTag(tag.id, checked)) }
                                )
                            },
                            onClick = { onEvent(DetailsScreenEvent.UpdateTag(tag.id, checked)) }
                        )
                    }
                }
            }

            DropdownMenuAction { dismiss ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.share)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(id = R.string.share))
                    },
                    onClick = {
                        onEvent(DetailsScreenEvent.Share)
                        dismiss()
                    }
                )

                if (screenState.isInstalled) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.open)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = stringResource(id = R.string.open))
                        },
                        onClick = {
                            onEvent(DetailsScreenEvent.Open)
                            dismiss()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.uninstall)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(id = R.string.uninstall))
                        },
                        onClick = {
                            onEvent(DetailsScreenEvent.Uninstall)
                            dismiss()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = R.string.app_info)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Info, contentDescription = stringResource(id = R.string.app_info))
                        },
                        onClick = {
                            onEvent(DetailsScreenEvent.AppInfo)
                            dismiss()
                        }
                    )
                }
            }
        }
    )
}

@Preview
@Composable
private fun DetailsScreenPreview() {
    val screenState = DetailsScreenState(
        appId = "test.id",
        title = "Test title long app name",
        rowId = 22,
        detailsUrl = "open",
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
        changelogState = ChangelogLoadState.Complete,
        changelogs = listOf(
            AppChange(
                appId = "package2",
                versionCode = 11223301,
                versionName = "very long long version name 1122000",
                details = "",
                uploadDate = "Apr 18, 2021",
                noNewDetails = true
            ),
            AppChange(
                appId = "package2",
                versionCode = 11223300,
                versionName = "very long long version name 11223300",
                details = """
                    ⚠️ Fixed black screen which appeared after connecting.<br>⚠️ <b>Fixed minor bug when switching to selfie mode.</b><br>⭐Added some great premium features.<br>⭐Updated translations for Russian, Hebrew and Arabic.
                """.trimIndent(),
                uploadDate = "Apr 18, 2021",
                noNewDetails = false
            ),
            AppChange(
                appId = "package2",
                versionCode = 11223111,
                versionName = "very long long version name 11223111",
                details = """
                    ⚠️ Fixed black screen which appeared after connecting.<br>⚠️ <b>Fixed minor bug when switching to selfie mode.</b><br>⭐Added some great premium features.<br>⭐Updated translations for Russian, Hebrew and Arabic.
                """.trimIndent(),
                uploadDate = "Apr 18, 2021",
                noNewDetails = true
            ),
            AppChange(
                appId = "package2",
                versionCode = 1122000,
                versionName = "very long long version name 1122000",
                details = """
                    • Bug fixes<br>  See the complete release notes at https://developers.google.com/android/management/release-notes
                """.trimIndent(),
                uploadDate = "Apr 18, 2021",
                noNewDetails = true
            )
        ),
        appLoadingState = AppLoadingState.Loaded
    )
    AppTheme(
        customPrimaryColor = Color.Blue
    ) {
        DetailsScreenContent(
            screenState = screenState,
            onEvent = { },
            installedApps = InstalledApps.StaticMap(emptyMap()),
            modifier = Modifier,
            viewActions = flowOf(),
            onDismissRequest = { },
            onCommonActivityAction = { }
        )
    }
}