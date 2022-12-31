package com.anod.appwatcher.tags

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.database.entities.Tag
import info.anodsplace.compose.ButtonsPanel
import info.anodsplace.compose.ColorDialogContent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditTagDialog(tag: Tag, onDismissRequest: () -> Unit) {
    val viewModel: EditTagViewModel = viewModel(factory = EditTagViewModel.Factory(tag))
    val screenState by viewModel.viewStates.collectAsState(initial = viewModel.viewState)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EditTagScreen(
            screenState = screenState,
            onEvent = { event -> viewModel.handleEvent(event) }
        )
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.viewActions.collect { action ->
            when (action) {
                EditTagAction.Dismiss -> onDismissRequest()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTagScreen(screenState: EditTagState, onEvent: (EditTagEvent) -> Unit) {
    var tagName by remember { mutableStateOf(screenState.tag.name) }
    var isError by remember { mutableStateOf(false) }
    var pickColor: Color? by remember { mutableStateOf(null) }
    Surface(
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColorIcon(
                    color = Color(screenState.tag.color),
                    onClick = { pickColor = it}
                )
                TextField(
                    modifier = Modifier.padding(start = 8.dp),
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text(text = stringResource(id = R.string.tag_name)) },
                    isError = isError
                )
            }
           AnimatedVisibility(visible = pickColor != null) {
                ColorDialogContent(
                    color = pickColor,
                    onColorChange = {
                        if (it != null) {
                            onEvent(EditTagEvent.UpdateColor(it.toArgb()))
                        }
                        pickColor = null
                    }
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
                    FilledIconButton(
                        onClick = { onEvent(EditTagEvent.Delete) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.delete)
                        )
                    }
                }
            )
        }
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
            onEvent = { }
        )
    }
}