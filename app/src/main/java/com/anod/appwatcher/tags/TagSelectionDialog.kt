package com.anod.appwatcher.tags

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import info.anodsplace.compose.CheckBoxList

@Composable
fun TagSelectionDialog(appId: String, appTitle: String, onDismissRequest: () -> Unit) {
    val viewModel: TagsSelectionViewModel = viewModel(key = appId, factory = TagsSelectionViewModel.Factory(appId, appTitle))
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
    Surface {
        Column {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.tag_app, screenState.appTitle)) },
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
            CheckBoxList(items = screenState.items, onCheckedChange = { key, checked -> onEvent(TagSelectionEvent.UpdateTag(key, checked)) })
        }
    }


    if (screenState.showAddTagDialog) {
        EditTagDialog(tag = Tag.empty) { tagId ->
            onEvent(TagSelectionEvent.AddTag(show = false, tagId = tagId))
        }
    }
}
