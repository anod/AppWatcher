package com.anod.appwatcher.tags

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ImageLoader
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppIconImage
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.SearchTopBar
import com.anod.appwatcher.compose.rememberViwModeStoreOwner
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.SelectionState
import info.anodsplace.compose.ButtonsPanel
import org.koin.java.KoinJavaComponent.getKoin

@Composable
fun AppsTagDialog(tag: Tag, onDismissRequest: () -> Unit) {
    val storeOwner = rememberViwModeStoreOwner()
    val viewModel: AppsTagViewModel = viewModel(
        viewModelStoreOwner = storeOwner,
        factory = AppsTagViewModel.Factory(tag)
    )
    val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)

    Dialog(onDismissRequest = onDismissRequest) {
        AppsTagScreen(
            screenState = screenState,
            onEvent = viewModel::handleEvent
        )
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.viewActions.collect { action ->
            when (action) {
                AppsTagScreenAction.Dismiss -> onDismissRequest()
            }
        }
    }
}

@Composable
fun AppsTagScreen(screenState: AppsTagScreenState, onEvent: (AppsTagScreenEvent) -> Unit, appIconLoader: AppIconLoader = getKoin().get(),) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SearchTopBar(
                title = screenState.tag.name,
                hideSearchOnNavigation = false,
                onValueChange = { onEvent(AppsTagScreenEvent.FilterByTitle(it)) },
                showSearch = true,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onNavigation = { onEvent(AppsTagScreenEvent.Dismiss) },
                actions = {
                    IconButton(onClick = { onEvent(AppsTagScreenEvent.SetSelection(all = false)) }) {
                        Icon(imageVector = Icons.Default.Deselect, contentDescription = stringResource(id = R.string.none))
                    }

                    IconButton(onClick = { onEvent(AppsTagScreenEvent.SetSelection(all = true)) }) {
                        Icon(imageVector = Icons.Default.SelectAll, contentDescription = stringResource(id = R.string.all))
                    }
                }
            )
            LazyColumn(
                modifier = Modifier.weight(1.0f)
            ) {
                items(screenState.apps.size) { i ->
                    AppRow(
                        app = screenState.apps[i],
                        selection = screenState.selection,
                        onEvent = onEvent,
                        appIconLoader = appIconLoader
                    )
                }
            }
            ButtonsPanel(
                actionText = stringResource(id = R.string.tag),
                onDismissRequest = { onEvent(AppsTagScreenEvent.Dismiss) },
                onAction = { onEvent(AppsTagScreenEvent.Import) }
            )
        }
    }
}

@Composable
private fun AppRow(
    app: App,
    selection: SelectionState,
    onEvent: (AppsTagScreenEvent) -> Unit,
    appIconLoader: AppIconLoader = getKoin().get(),
) {
    ListItem(
        modifier = Modifier.clickable(onClick = { onEvent(AppsTagScreenEvent.Toggle(appId = app.appId)) }),
        headlineContent = {
            Text(
                text = app.title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = {
            AppIconImage(
                app = app,
                contentDescription = app.title,
                appIconLoader = appIconLoader
            )
        },
        trailingContent = {
            Checkbox(
                checked = selection.contains(app.appId),
                onCheckedChange = { onEvent(AppsTagScreenEvent.Toggle(appId = app.appId)) }
            )
        }
    )
}

@Preview
@Composable
private fun AppsTagScreenPreview() {
    val tag = Tag(1, "Banana", Color.Magenta.toArgb())
    val appIconLoader = AppIconLoader.Simple(
        LocalContext.current, ImageLoader.Builder(LocalContext.current).build()
    )
    AppTheme(
        customPrimaryColor = Color(tag.color),
    ) {
        AppsTagScreen(
            screenState = AppsTagScreenState(
                tag = tag,
                apps = listOf(
                    App(
                        rowId = -1,
                        appId = "package3",
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
                    )
                )
            ),
            onEvent = {},
            appIconLoader = appIconLoader
        )
    }
}