package com.anod.appwatcher.tags

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.rememberViwModeStoreOwner
import com.anod.appwatcher.database.entities.Tag
import info.anodsplace.compose.BottomSheet
import info.anodsplace.compose.ButtonsPanel
import info.anodsplace.compose.ColorDialogContent

@Composable
fun EditTagDialog(tag: Tag, onDismissRequest: (tagId: Int) -> Unit) {
    val storeOwner = rememberViwModeStoreOwner()
    val viewModel: EditTagViewModel = viewModel(
        key = "tag-${tag.id}",
        viewModelStoreOwner = storeOwner,
        factory = EditTagViewModel.Factory(tag)
    )
    val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)

    AppTheme(
        customPrimaryColor = Color(screenState.tag.color),
        updateSystemBars = false
    ) {
        Dialog(onDismissRequest = { onDismissRequest(screenState.tag.id) }) {
            EditTagScreen(
                screenState = screenState,
                onEvent = viewModel::handleEvent
            )
        }

        if (screenState.showPickColor) {
            BottomSheet(
                onDismissRequest = { viewModel.handleEvent(EditTagEvent.PickColor(show = false)) }
            ) {
                ColorChooser(
                    color = Color(screenState.tag.color),
                    onColorChange = {
                        if (it != null) {
                            viewModel.handleEvent(EditTagEvent.UpdateColor(it.toArgb()))
                        }
                    },
                )
            }
        }
    }

    LaunchedEffect(key1 = viewModel, key2 = onDismissRequest) {
        viewModel.viewActions.collect { action ->
            when (action) {
                is EditTagAction.Dismiss -> onDismissRequest(action.tagId)
            }
        }
    }
}

@Composable
private fun EditTagScreen(screenState: EditTagState, onEvent: (EditTagEvent) -> Unit) {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            var tagName by remember { mutableStateOf(screenState.tag.name) }
            var isError by remember { mutableStateOf(false) }

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        ColorIcon(
                            color = Color(screenState.tag.color),
                            onClick = { onEvent(EditTagEvent.PickColor(show = true)) }
                        )
                    }
                    TextField(
                        modifier = Modifier.padding(start = 8.dp),
                        value = tagName,
                        onValueChange = { tagName = it },
                        label = { Text(text = stringResource(id = R.string.tag_name)) },
                        isError = isError
                    )
                }

                ButtonsPanel(
                    actionText = stringResource(id = R.string.save),
                    onDismissRequest = { onEvent(EditTagEvent.Dismiss) },
                    onAction = {
                        if (tagName.isEmpty()) {
                            isError = true
                        } else {
                            isError = false
                            onEvent(EditTagEvent.SaveAndDismiss(name = tagName))
                        }
                    },
                    leadingContent = {
                        if (screenState.tag.id > 0) {
                            FilledIconButton(
                                onClick = { onEvent(EditTagEvent.Delete) },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(id = R.string.delete)
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorChooser(color: Color, onColorChange: (Color?) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth()) {
        ColorDialogContent(
            color = color,
            onColorChange = onColorChange,
            showNone = false,
            showAlpha = false
        )
    }
}

@Composable
private fun ColorIcon(color: Color, onClick: (Color) -> Unit, modifier: Modifier = Modifier) {
    Icon(
        modifier = modifier
            .size(48.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clip(shape = CircleShape)
            .clickable { onClick(color) },
        painter = ColorPainter(color),
        tint = Color.Unspecified,
        contentDescription = color.toString()
    )
}

@Preview
@Composable
private fun EditTagScreenPreview() {
    val tag = Tag("Banana")
    AppTheme {
        EditTagScreen(
            screenState = EditTagState(tag),
            onEvent = { },
        )
    }
}

@Preview
@Composable
private fun EditTagScreenColorPreview() {
    AppTheme {
        ColorChooser(
            color = Color.Cyan,
            onColorChange = { }
        )
    }
}