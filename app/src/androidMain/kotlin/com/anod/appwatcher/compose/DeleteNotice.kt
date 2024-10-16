package com.anod.appwatcher.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anod.appwatcher.R

@Composable
fun DeleteNotice(onDelete: () -> Unit, onDismissRequest: () -> Unit) {
    AlertDialog(
        title = { Text(text = stringResource(id = R.string.already_exist)) },
        text = { Text(text = stringResource(id = R.string.delete_existing_item)) },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = {
                onDelete()
            }) {
                Text(text = stringResource(id = R.string.delete))
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        }
    )
}