package com.anod.appwatcher.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.anod.appwatcher.R

@Composable
fun SortMenuItem(selectedSortId: Int, onChangeSort: (Int) -> Unit, barBounds: IntRect) {
    var topBarSortMenu by remember { mutableStateOf(false) }
    var menuItemWidth by remember { mutableStateOf(0) }
    DropdownMenuItem(
        text = { Text(text = stringResource(id = R.string.sort)) },
        leadingIcon = { Icon(imageVector = Icons.Default.Sort, contentDescription = stringResource(id = R.string.sort)) },
        trailingIcon = {
            Icon(imageVector = Icons.Default.ArrowRight, contentDescription = null)
        },
        modifier = Modifier.onGloballyPositioned {
            if (it.isAttached) {
                menuItemWidth = it.size.width
            }
        },
        onClick = {  topBarSortMenu = true },
    )

    val density = LocalDensity.current
    val dpOffset = with(density) { DpOffset(x = (barBounds.width - menuItemWidth).toDp(), y = 0.dp) }
    SortDropdownMenu(selectedSortId, onChangeSort, topBarSortMenu, { topBarSortMenu = false }, offset = dpOffset)
}

@Composable
fun SortMenuAction(selectedSortId: Int, onChangeSort: (Int) -> Unit) {
    var topBarSortMenu by remember { mutableStateOf(false) }
    IconButton(onClick = { topBarSortMenu = true }) {
        Icon(imageVector = Icons.Default.Sort, contentDescription = stringResource(id = R.string.sort))
        SortDropdownMenu(selectedSortId, onChangeSort, topBarSortMenu, { topBarSortMenu = false })
    }
}

@Composable
private fun SortDropdownMenu(selectedSortId: Int, onChangeSort: (Int) -> Unit, expanded: Boolean, onDismissRequest: () -> Unit, offset: DpOffset = DpOffset(0.dp, 0.dp)) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, offset = offset) {
        val sortTitles = listOf(
            stringResource(id = R.string.sort_by_name_asc),
            stringResource(id = R.string.sort_by_name_desc),
            stringResource(id = R.string.sort_by_date_asc),
            stringResource(id = R.string.sort_by_date_desc),
        )
        sortTitles.forEachIndexed { index, sortTitle ->
            DropdownMenuItem(
                text = { Text(text = sortTitle) },
                leadingIcon = {
                    Icon(
                        imageVector = if (selectedSortId == index) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null
                    )
                },
                onClick = {
                    onDismissRequest()
                    onChangeSort(index)
                }
            )
        }
    }
}