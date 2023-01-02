package com.anod.appwatcher.details

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.DropdownMenuAction
import com.anod.appwatcher.databinding.ViewDetailsContainerBinding
import info.anodsplace.applog.AppLog

private val iconSizeBig = 64.dp
private val iconSizeSmall = 32.dp

@Composable
fun DetailsScreen(appId: String, rowId: Int, detailsUrl: String) {
    val localContext = LocalContext.current
    AndroidViewBinding(factory = ViewDetailsContainerBinding::inflate) {
        val supportFragmentManager = (localContext as FragmentActivity).supportFragmentManager
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.detailsContainerView, DetailsFragment.newInstance(appId, detailsUrl, rowId))
        }
    }
}

@Composable
private fun DetailsScreenContent(screenState: DetailsScreenState, onEvent: (DetailsScreenEvent) -> Unit) {
    val titleVisibility by remember { mutableStateOf(0.0f) }
    Surface {
        Column {
            DetailsTopAppBar(
                titleVisibility = titleVisibility,
                screenState = screenState,
                onEvent = onEvent
            )
            DetailsHeader(
                screenState = screenState,
                onEvent = onEvent
            )
            Text(text = "Version")

            if (screenState.appLoadingState is AppLoadingState.NotFound) {
                Column {
                    Text(text = stringResource(id = R.string.problem_occurred))
                    Button(onClick = { }) {
                        Text(text = stringResource(id = R.string.retry))
                    }
                }
            } else {
                DetailsChangelog(screenState = screenState)
            }
        }
    }
}

@Composable
private fun DetailsChangelog(screenState: DetailsScreenState) {
    AppLog.d("Details collecting changelogState $screenState.changelogState")
    when (screenState.changelogState) {
        ChangelogLoadState.Complete -> {

        }
        ChangelogLoadState.Initial -> {

        }
        ChangelogLoadState.LocalComplete -> {

        }
        is ChangelogLoadState.RemoteComplete -> {

        }
    }
}

@Composable
private fun DetailsHeader(screenState: DetailsScreenState, onEvent: (DetailsScreenEvent) -> Unit) {
    Row {
        DetailsAppIcon(appIconState = screenState.appIconState, size = iconSizeBig)
        Column {
            Text(text = screenState.title, style = MaterialTheme.typography.titleLarge)
            Text(text = screenState.title, style = MaterialTheme.typography.labelMedium)
            Text(text = screenState.title, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun DetailsAppIcon(appIconState: AppIconState, size: Dp) {
    AppLog.d("Details collecting appIconState $appIconState")
    when (appIconState) {
        is AppIconState.Loaded -> DetailsIconApp(bitmap = appIconState.drawable.bitmap, size = size)
        AppIconState.Default -> DetailsIconPlaceHolder(size = size)
        AppIconState.Initial -> DetailsIconPlaceHolder(size = size)
    }
}

@Composable
private fun DetailsIconPlaceHolder(size: Dp) {
    Icon(
        painter = painterResource(id = R.drawable.ic_app_icon_placeholder),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(size)
    )
}

@Composable
private fun DetailsIconApp(bitmap: Bitmap, size: Dp) {
    Icon(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(size)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsTopAppBar(titleVisibility: Float, screenState: DetailsScreenState, onEvent: (DetailsScreenEvent) -> Unit) {
    TopAppBar(
        title = {
            if (titleVisibility > 0) {
                Row(
                    modifier = Modifier.alpha(titleVisibility)
                ) {
                    DetailsAppIcon(appIconState = screenState.appIconState, size = iconSizeSmall)
                    Text(text = screenState.title)
                }
            }
        },
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
                onClick = { onEvent(DetailsScreenEvent.WatchApp) },
                enabled = screenState.changelogState is ChangelogLoadState.Complete
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = stringResource(id = R.string.menu_add)
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Label,
                    contentDescription = stringResource(id = R.string.tag)
                )
            }

            DropdownMenuAction { dismiss ->
                IconButton(onClick = {
                    onEvent(DetailsScreenEvent.Share)
                    dismiss()
                }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(id = R.string.share)
                    )
                }
                if (screenState.isInstalled) {
                    IconButton(onClick = {
                        onEvent(DetailsScreenEvent.Open)
                        dismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = stringResource(id = R.string.open)
                        )
                    }
                    IconButton(onClick = {
                        onEvent(DetailsScreenEvent.Uninstall)
                        dismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.uninstall)
                        )
                    }
                    IconButton(onClick = {
                        onEvent(DetailsScreenEvent.AppInfo)
                        dismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(id = R.string.app_info)
                        )
                    }
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
        title = "",
        rowId = 22,
        detailsUrl = "open"
    )
    AppTheme(
        customPrimaryColor = Color.Blue
    ) {
        DetailsScreenContent(
            screenState = screenState,
            onEvent = { }
        )
    }
}