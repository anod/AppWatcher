package com.anod.appwatcher.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anod.appwatcher.R

@Composable
fun SortMenuItem(onClick: () -> Unit) {
    DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.sort)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Sort, contentDescription = stringResource(id = R.string.sort)) },
            trailingIcon = { Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null) },
            onClick = onClick
    )
}

@Composable
fun SortDropdownMenu(selectedSortId: Int, onChangeSort: (Int) -> Unit, expanded: Boolean, onDismissRequest: () -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        val sortTitles = listOf(
                stringResource(id = R.string.sort_by_name_asc),
                stringResource(id = R.string.sort_by_name_desc),
                stringResource(id = R.string.sort_by_date_asc),
                stringResource(id = R.string.sort_by_date_desc),
        )
        sortTitles.forEachIndexed { index, sortTitle ->
            DropdownMenuItem(
                    text = { Text(text = sortTitle) },
                    leadingIcon = { Icon(imageVector = if (selectedSortId == index) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, contentDescription = null) },
                    onClick = { onChangeSort(index) }
            )
        }
    }
}