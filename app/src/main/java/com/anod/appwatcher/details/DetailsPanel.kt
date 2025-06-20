package com.anod.appwatcher.details

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.text.Spannable
import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ShareCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppInfoIcon
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BackArrowIconButton
import com.anod.appwatcher.compose.DeleteNotice
import com.anod.appwatcher.compose.DropdownMenuAction
import com.anod.appwatcher.compose.InstalledSignIcon
import com.anod.appwatcher.compose.OpenAppIcon
import com.anod.appwatcher.compose.PlayStoreAppIcon
import com.anod.appwatcher.compose.ShareIcon
import com.anod.appwatcher.compose.StoreVersionSignIcon
import com.anod.appwatcher.compose.TagIcon
import com.anod.appwatcher.compose.TranslateIcon
import com.anod.appwatcher.compose.UninstallIcon
import com.anod.appwatcher.compose.WatchedIcon
import com.anod.appwatcher.compose.rememberViwModeStoreOwner
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.tags.TagSelectionDialog
import com.anod.appwatcher.tags.TagSnackbar
import com.anod.appwatcher.utils.StoreIntent
import info.anodsplace.applog.AppLog
import info.anodsplace.compose.placeholder
import info.anodsplace.compose.toAnnotatedString
import info.anodsplace.framework.content.showToast
import info.anodsplace.framework.content.startActivity
import info.anodsplace.framework.text.Html
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private val iconSizeBig = 64.dp
private val iconSizeSmall = 32.dp

private val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

@Composable
fun DetailsPanel(app: App, onDismissRequest: () -> Unit) {
    val storeOwner = rememberViwModeStoreOwner()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val viewModel: DetailsViewModel = viewModel(
        key = "details-${app.appId}-${app.rowId}",
        viewModelStoreOwner = storeOwner,
        factory = DetailsViewModel.Factory(argApp = app, isSystemInDarkTheme = isSystemInDarkTheme)
    )

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
            onEvent = viewModel::handleEvent,
            modifier = Modifier.fillMaxSize(),
            viewActions = viewModel.viewActions,
            onDismissRequest = onDismissRequest,
        )
    }
}

@Composable
fun DetailsDialog(app: App, onDismissRequest: () -> Unit) {
    val storeOwner = rememberViwModeStoreOwner()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val viewModel: DetailsViewModel = viewModel(
        key = "details-${app.appId}-${app.rowId}",
        viewModelStoreOwner = storeOwner,
        factory = DetailsViewModel.Factory(argApp = app, isSystemInDarkTheme = isSystemInDarkTheme)
    )

    val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)
    val customPrimaryColor by remember(screenState.customPrimaryColor) {
        derivedStateOf { screenState.customPrimaryColor }
    }
    AppTheme(
        customPrimaryColor = customPrimaryColor?.let { Color(it) },
        updateSystemBars = false,
        useSurfaceAsPrimary = screenState.appIconState != AppIconState.Default
    ) {
        Dialog(onDismissRequest = onDismissRequest) {
            DetailsScreenContent(
                screenState = screenState,
                viewActions = viewModel.viewActions,
                onEvent = viewModel::handleEvent,
                onDismissRequest = onDismissRequest,
                modifier = Modifier.fillMaxHeight(fraction = 0.9f)
            )
        }
    }
}

private fun createAppChooser(appInfo: App, recentChange: AppChange, context: Context): Intent {
    val builder = ShareCompat.IntentBuilder(context)

    val changes =
        if (recentChange.details.isBlank()) "" else "${Html.parse(recentChange.details)}\n\n"
    val text = context.getString(
        R.string.share_text,
        changes,
        String.format(StoreIntent.URL_WEB_PLAY_STORE, appInfo.packageName)
    )

    builder.setSubject(
        context.getString(
            R.string.share_subject,
            appInfo.title,
            appInfo.versionName
        )
    )
    builder.setText(text)
    builder.setType("text/plain")
    return builder.createChooserIntent()
}

private val headerHeightDp = 80.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsScreenContent(
    screenState: DetailsState,
    onEvent: (DetailsEvent) -> Unit,
    viewActions: Flow<DetailsAction>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(key1 = onEvent) {
        onEvent(DetailsEvent.LoadChangelog)
    }

    val appColorAvailable =
        (screenState.customPrimaryColor != null || screenState.appIconState is AppIconState.Default)
    val surfaceColor: Color = if (appColorAvailable) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val snackBarHostState = remember { SnackbarHostState() }

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = topAppBarState
    )

    val heightOffsetLimit = with(LocalDensity.current) { -headerHeightDp.toPx() }
    SideEffect {
        if (scrollBehavior.state.heightOffsetLimit != heightOffsetLimit) {
            scrollBehavior.state.heightOffsetLimit = heightOffsetLimit
        }
    }

    val actualHeaderHeightDp = LocalDensity.current.run {
        headerHeightDp + scrollBehavior.state.heightOffset.toDp()
    }

    val collapsedFraction = scrollBehavior.state.collapsedFraction

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = appColorAvailable,
                enter = fadeIn(),
                label = "HeaderBackground"
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp + actualHeaderHeightDp)
                        .align(Alignment.TopStart)
                        .background(color = surfaceColor)
                )
            }

            Column {
                DetailsTopAppBar(
                    titleVisibility = collapsedFraction,
                    screenState = screenState,
                    onEvent = onEvent,
                    containerColor = Color.Transparent
                )
                if (collapsedFraction < 1.0f) {
                    DetailsHeader(
                        screenState = screenState,
                        alpha = 1.0f - collapsedFraction,
                        heightDp = actualHeaderHeightDp,
                        containerColor = Color.Transparent
                    )
                }
                VersionDetails(screenState = screenState)

                AppLog.d("Details collecting changelogState ${screenState.changelogState}")

                when (screenState.changelogState) {
                    ChangelogLoadState.Initial -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            LinearProgressIndicator()
                        }
                    }

                    ChangelogLoadState.Complete -> DetailsChangelog(
                        screenState = screenState,
                        scrollBehaviour = scrollBehavior,
                        onEvent = onEvent
                    )

                    ChangelogLoadState.RemoteError -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = stringResource(id = R.string.problem_occurred))
                            Button(onClick = { onEvent(DetailsEvent.ReloadChangelog) }) {
                                Text(text = stringResource(id = R.string.retry))
                            }
                        }
                    }
                }
            }

            if (collapsedFraction < 0.7f) {
                SmallFloatingActionButton(
                    onClick = { onEvent(DetailsEvent.PlayStore) },
                    content = { PlayStoreAppIcon() },
                    modifier = Modifier
                        .alpha(1.0f - collapsedFraction)
                        .padding(top = 64.dp + actualHeaderHeightDp - 24.dp, end = 16.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }

    val context = LocalContext.current
    var showTagList: App? by remember { mutableStateOf(null) }
    LaunchedEffect(onDismissRequest) {
        viewActions.collect { action ->
            when (action) {
                DetailsAction.Dismiss -> {
                    onDismissRequest()
                }

                is DetailsAction.Share -> {
                    context.startActivity(
                        DetailsAction.StartActivity(
                            intent = createAppChooser(
                                action.app,
                                action.recentChange,
                                context
                            )
                        )
                    )
                }
                is DetailsAction.ShowTagSnackbar -> {
                    val result =
                        snackBarHostState.showSnackbar(TagSnackbar.Visuals(action.appInfo, context))
                    if (result == SnackbarResult.ActionPerformed) {
                        showTagList = action.appInfo
                    }
                }

                is DetailsAction.ShowToast -> context.showToast(action)
                is DetailsAction.StartActivity -> context.startActivity(action)
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
fun VersionDetails(screenState: DetailsState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
    ) {
        if (screenState.app != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .padding(end = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                val appPrice = screenState.app.price
                Text(
                    text = if (appPrice.isFree) stringResource(id = R.string.free) else appPrice.text,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 40.dp)
                        .padding(end = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )

                if (screenState.remoteVersionInfo != null) {
                    val versionInfo = screenState.remoteVersionInfo
                    if (versionInfo.isBeta) {
                        VersionInfoCell(text = stringResource(id = R.string.beta))
                    }
                    if (versionInfo.starRating > 0) {
                        VersionInfoCell(
                            text = stringResource(
                                id = R.string.rating_stars,
                                versionInfo.starRating
                            )
                        )
                    }
                    if (versionInfo.installationSize > 0) {
                        VersionInfoCell(
                            text = Formatter.formatShortFileSize(
                                LocalContext.current,
                                versionInfo.installationSize
                            )
                        )
                    }
                    if (versionInfo.targetSdkVersion > 0) {
                        var showSdk by remember(key1 = versionInfo) { mutableStateOf(false) }
                        if (versionInfo.androidVersion == null || showSdk) {
                            VersionInfoCell(
                                text = "SDK ${versionInfo.targetSdkVersion}",
                                modifier = Modifier.clickable(
                                    enabled = versionInfo.androidVersion != null,
                                    onClickLabel = stringResource(id = R.string.android_version),
                                    role = Role.Switch,
                                    onClick = { showSdk = false }
                                )
                            )
                        } else {
                            VersionInfoCell(
                                text = versionInfo.androidVersion,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Android,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(start = 2.dp, end = 2.dp)
                                            .size(18.dp)
                                    )
                                },
                                modifier = Modifier.clickable(
                                    enabled = true,
                                    onClickLabel = stringResource(id = R.string.target_sdk),
                                    role = Role.Switch,
                                    onClick = { showSdk = true }
                                )
                            )
                        }
                    }
                } else {
                    if (!screenState.remoteCallFinished) {
                        VersionInfoCell(
                            text = stringResource(id = R.string.beta),
                            placeholder = true
                        )
                        VersionInfoCell(
                            text = stringResource(id = R.string.rating_stars, 5.0f),
                            placeholder = true
                        )
                        VersionInfoCell(text = "50 MB", placeholder = true)
                        VersionInfoCell(text = "SDK 33", placeholder = true)
                    }
                }
            }
        }
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (screenState.packageInfo.isInstalled || screenState.isLocalApp) {
                InstalledSignIcon(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
                if (screenState.app != null) {
                    val appItemState = rememberAppItemState(
                        app = screenState.app,
                        recentFlag = false,
                        packageInfo = screenState.packageInfo
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
            } else {
                Spacer(modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun VersionInfoCell(
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    placeholder: Boolean = false
) {
    HorizontalDivider()
    if (leadingIcon != null) {
        Row(
            modifier = modifier
                .sizeIn(minWidth = 40.dp, maxWidth = 88.dp)
                .padding(horizontal = 4.dp)
                .placeholder(
                    visible = placeholder,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon()
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    } else {
        Text(
            text = text,
            modifier = modifier
                .sizeIn(minWidth = 40.dp, maxWidth = 88.dp)
                .padding(horizontal = 4.dp)
                .placeholder(
                    visible = placeholder,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HorizontalDivider(modifier: Modifier = Modifier, thickness: Dp = DividerDefaults.Thickness, color: Color = DividerDefaults.color) {
    val targetThickness = if (thickness == Dp.Hairline) {
        (1f / LocalDensity.current.density).dp
    } else {
        thickness
    }
    Box(
        modifier
            .fillMaxHeight(0.8f)
            .width(targetThickness)
            .background(color = color)
    )
}

@OptIn(ExperimentalTextApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DetailsChangelog(screenState: DetailsState, onEvent: (DetailsEvent) -> Unit, scrollBehaviour: TopAppBarScrollBehavior) {
    LazyColumn(
        modifier = Modifier
            .nestedScroll(scrollBehaviour.nestedScrollConnection)
            .fillMaxSize()
    ) {
        items(screenState.changelogs.size) { i ->
            val change = screenState.changelogs[i]
            SelectionContainer {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StoreVersionSignIcon(
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
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

                    val text = if (change.details.isEmpty()) {
                        AnnotatedString(stringResource(id = R.string.no_recent_changes))
                    } else {
                        val parsed = Html.parse(change.details)
                        (parsed.trim() as Spannable).toAnnotatedString(linkColor = MaterialTheme.colorScheme.primary)
                    }

                    ClickableText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LocalContentColor.current
                        ),
                        onClick = { offset ->
                            text.getLinkAnnotations(offset, offset)
                                .firstOrNull()
                                ?.let { annotation ->
                                    annotation.item as? LinkAnnotation.Url
                                }
                                ?.also { item ->
                                    onEvent(DetailsEvent.OpenUrl(item.url))
                                }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsHeader(
    screenState: DetailsState,
    alpha: Float,
    heightDp: Dp,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Row(
        modifier = Modifier
            .height(heightDp)
            .clipToBounds()
            .fillMaxWidth()
            .background(color = containerColor)
            .alpha(alpha)
    ) {
        DetailsAppIcon(
            appIconState = screenState.appIconState,
            modifier = Modifier
                .requiredSize(iconSizeBig)
                .padding(start = 16.dp)
        )
        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                if (screenState.app != null) {
                    Text(
                        text = screenState.title,
                        color = contentColor,
                        style = MaterialTheme.typography.headlineSmall,
                        fontSize = if (screenState.title.length >= 30) {
                            16.sp
                        } else {
                            18.sp
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
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
                        var showDiscoverDate by remember(key1 = screenState.app) { mutableStateOf(false) }
                        if (showDiscoverDate) {
                            Text(
                                text = dateFormat.format(Date(screenState.app.syncTime)),
                                color = contentColor,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable(
                                    onClickLabel = stringResource(id = R.string.last_update),
                                    role = Role.Switch,
                                    onClick = { showDiscoverDate = false }
                                )
                            )
                        } else {
                            Text(
                                text = screenState.app.uploadDate,
                                color = contentColor,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable(
                                    enabled = screenState.app.syncTime > 0,
                                    onClickLabel = stringResource(id = R.string.discovered_date),
                                    role = Role.Switch,
                                    onClick = { showDiscoverDate = true }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsAppIcon(appIconState: AppIconState, modifier: Modifier = Modifier) {
    AppLog.d("Details collecting appIconState $appIconState")
    when (appIconState) {
        is AppIconState.Loaded -> DetailsIconApp(
            bitmap = appIconState.drawable.bitmap,
            modifier = modifier
        )

        AppIconState.Default -> DetailsIconPlaceHolder(modifier = modifier)
        AppIconState.Initial -> DetailsIconPlaceHolder(modifier = modifier)
    }
}

@Composable
private fun DetailsIconPlaceHolder(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(id = R.drawable.ic_app_icon_placeholder),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = modifier
    )
}

@Composable
private fun DetailsIconApp(bitmap: Bitmap, modifier: Modifier = Modifier) {
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
    screenState: DetailsState,
    onEvent: (DetailsEvent) -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    var showDeleteNotice by remember { mutableStateOf(false) }
    val isTitleVisible by remember(titleVisibility) {
        derivedStateOf { titleVisibility > 0.0f }
    }
    TopAppBar(
        windowInsets = WindowInsets(0),
        title = {
            if (isTitleVisible) {
                Row(
                    modifier = Modifier.alpha(titleVisibility)
                ) {
                    DetailsAppIcon(
                        appIconState = screenState.appIconState,
                        modifier = Modifier.size(iconSizeSmall)
                    )
                    Column(
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        val hasUploadDate = screenState.app?.uploadDate?.isNotEmpty() == true
                        Text(
                            text = screenState.title,
                            color = contentColor,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = if (hasUploadDate) 1 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (screenState.app?.uploadDate?.isNotEmpty() == true) {
                            Text(
                                text = screenState.app.uploadDate,
                                color = contentColor,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Clip
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor,
        ),
        navigationIcon = {
            BackArrowIconButton(onClick = { onEvent(DetailsEvent.OnBackPressed) })
        },
        actions = {
            IconButton(
                onClick = {
                    if (screenState.fetchedRemoteDocument) {
                        onEvent(DetailsEvent.WatchAppToggle)
                    } else {
                        if (screenState.isWatched) {
                            showDeleteNotice = true
                        } else {
                            // Should show error
                            onEvent(DetailsEvent.WatchAppToggle)
                        }
                    }
                },
                enabled = screenState.isWatched || screenState.fetchedRemoteDocument
            ) {
                WatchedIcon(unwatch = screenState.isWatched)
            }

            if (screenState.tagsMenuItems.isNotEmpty()) {
                var showTagsMenu: Boolean by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { showTagsMenu = true },
                    enabled = screenState.isWatched
                ) {
                    TagIcon()
                }

                DropdownMenu(expanded = showTagsMenu, onDismissRequest = { showTagsMenu = false }) {
                    screenState.tagsMenuItems.forEach { (tag, checked) ->
                        DropdownMenuItem(
                            text = { Text(text = tag.name) },
                            leadingIcon = {
                                TagIcon(tint = Color(tag.color), contentDescription = tag.name)
                            },
                            trailingIcon = {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = {
                                        onEvent(
                                            DetailsEvent.UpdateTag(
                                                tag.id,
                                                checked
                                            )
                                        )
                                    }
                                )
                            },
                            onClick = { onEvent(DetailsEvent.UpdateTag(tag.id, checked)) }
                        )
                    }
                }
            }

            DropdownMenuAction { dismiss ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.share),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    },
                    leadingIcon = { ShareIcon() },
                    onClick = {
                        onEvent(DetailsEvent.Share)
                        dismiss()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.translate),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    },
                    enabled = screenState.changelogs.firstOrNull()?.details?.isNotEmpty() == true,
                    leadingIcon = { TranslateIcon() },
                    onClick = {
                        onEvent(DetailsEvent.Translate)
                        dismiss()
                    }
                )

                if (screenState.packageInfo.isInstalled) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = R.string.open),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        },
                        leadingIcon = { OpenAppIcon() },
                        onClick = {
                            onEvent(DetailsEvent.Open)
                            dismiss()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = R.string.uninstall),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        },
                        leadingIcon = { UninstallIcon() },
                        onClick = {
                            onEvent(DetailsEvent.Uninstall)
                            dismiss()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = R.string.app_info),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        },
                        leadingIcon = { AppInfoIcon() },
                        onClick = {
                            onEvent(DetailsEvent.App)
                            dismiss()
                        }
                    )
                }
            }
        }
    )

    if (showDeleteNotice) {
        DeleteNotice(
            onDelete = {
                onEvent(DetailsEvent.WatchAppToggle)
                showDeleteNotice = false
            },
            onDismissRequest = { showDeleteNotice = false }
        )
    }
}

@Preview
@Composable
private fun DetailsScreenPreview() {
    val screenState = DetailsState(
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
            syncTime = 0
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
        appLoadingState = AppLoadingState.Loaded,
        customPrimaryColor = Color.Blue.toArgb()
    )
    AppTheme(
        customPrimaryColor = Color.Blue
    ) {
        DetailsScreenContent(
            screenState = screenState,
            onEvent = { },
            modifier = Modifier,
            viewActions = flowOf(),
            onDismissRequest = { },
        )
    }
}

@Preview(locale = "ru")
@Composable
private fun VersionInfoPreview() {
    AppTheme(
        customPrimaryColor = Color.Blue
    ) {
        Surface {
            VersionDetails(
                screenState = DetailsState(
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
                        syncTime = 0
                    ),
                    changelogState = ChangelogLoadState.Complete,
                    changelogs = listOf(),
                    appLoadingState = AppLoadingState.Loaded,
                    remoteVersionInfo = AppVersionInfo(
                        isBeta = true,
                        installationSize = 9000000,
                        targetSdkVersion = 33,
                        starRating = 5.0f
                    )
                )
            )
        }
    }
}