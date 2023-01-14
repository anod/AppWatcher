package com.anod.appwatcher.tags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.rememberViwModeStoreOwner
import com.anod.appwatcher.database.entities.Tag
import info.anodsplace.compose.CheckBoxItem
import info.anodsplace.compose.CheckBoxList

@Composable
fun TagSelectionDialog(appId: String, appTitle: String, onDismissRequest: () -> Unit) {
    val storeOwner = rememberViwModeStoreOwner()
    val viewModel: TagsSelectionViewModel = viewModel(
        key = appId,
        viewModelStoreOwner = storeOwner,
        factory = TagsSelectionViewModel.Factory(appId, appTitle)
    )
    val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        TagSelectionScreen(
            screenState = screenState,
            onEvent = { event -> viewModel.handleEvent(event) },
            onDismissRequest = onDismissRequest
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelectionScreen(screenState: TagsSelectionState, onEvent: (TagSelectionEvent) -> Unit, onDismissRequest: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(0.9f)
    ) {
        Column {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.tag_app, screenState.appTitle),
                        fontSize = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismissRequest) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(TagSelectionEvent.AddTag(
                        show = true
                    )) }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.menu_add))
                    }
                }
            )
            if (screenState.items.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.tags_list_is_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                    Button(
                        onClick = { onEvent(TagSelectionEvent.AddTag(show = true))  },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 188.dp)
                            .padding(top = 8.dp)
                    ) {
                        Text(text = stringResource(id = R.string.menu_add))
                    }
                }
            } else {
                CheckBoxList(
                    items = screenState.items,
                    modifier = Modifier.padding(end = 16.dp),
                    onCheckedChange = { item ->
                        onEvent(
                            TagSelectionEvent.UpdateTag(
                                item.key,
                                item.checked
                            )
                        )
                    }
                )
            }
        }
    }

    if (screenState.showAddTagDialog) {
        EditTagDialog(tag = Tag.empty) { tagId ->
            onEvent(TagSelectionEvent.AddTag(show = false, tagId = tagId))
        }
    }
}

@Preview
@Composable
private fun TagSelectionScreen() {
    AppTheme {
        TagSelectionScreen(
            screenState = TagsSelectionState(
                appId = "package1",
                appTitle = "App Title",
                items = listOf(
                    CheckBoxItem(
                        key = "tag-1",
                        checked = false,
                        title = "Tag #1",
                        icon = Icons.Default.Label,
                        iconTint = Color.Red
                    ),
                    CheckBoxItem(
                        key = "tag-2",
                        checked = true,
                        title = "Tag #2",
                        icon = Icons.Default.Label,
                        iconTint = Color.Yellow
                    )
                )
            ),
            onEvent = { },
            onDismissRequest = { }
        )
    }
}
