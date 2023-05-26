package com.anod.appwatcher.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
fun DropdownMenuAction(content: @Composable (dismiss: () -> Unit) -> Unit) {
    var topBarMoreMenu by remember { mutableStateOf(false) }

    MoreIconButton(onClick = { topBarMoreMenu = true })

    DropdownMenu(
        expanded = topBarMoreMenu,
        onDismissRequest = { topBarMoreMenu = false },
    ) {
        content(dismiss = { topBarMoreMenu = false })
    }
}

@Composable
fun SortMenuItem(selectedSortId: Int, onChangeSort: (Int) -> Unit, barBounds: IntRect) {
    var topBarSortMenu by remember { mutableStateOf(false) }
    var menuItemWidth by remember { mutableIntStateOf(0) }
    DropdownMenuItem(
        text = { Text(text = stringResource(id = R.string.sort)) },
        leadingIcon = { SortIcon() },
        trailingIcon = { ExpandMenuIcon() },
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
    SortIconButton(onClick = { topBarSortMenu = true }) {
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
                text = { Text(text = sortTitle, modifier = Modifier.padding(horizontal = 8.dp)) },
                leadingIcon = {
                    RadioIcon(isChecked = (selectedSortId == index) )
                },
                onClick = {
                    onDismissRequest()
                    onChangeSort(index)
                }
            )
        }
    }
}

@Composable
fun FilterMenuItem(filterId: Int, onFilterChange: (Int) -> Unit, barBounds: IntRect) {
    var topBarFilterMenu by remember { mutableStateOf(false) }
    var menuItemWidth by remember { mutableIntStateOf(0) }
    DropdownMenuItem(
        text = { Text(text = stringResource(id = R.string.filter)) },
        leadingIcon = { FilterIcon() },
        trailingIcon = { ExpandMenuIcon() },
        modifier = Modifier.onGloballyPositioned {
            if (it.isAttached) {
                menuItemWidth = it.size.width
            }
        },
        onClick = { topBarFilterMenu = true },
    )

    val density = LocalDensity.current
    val dpOffset = with(density) { DpOffset(x = (barBounds.width - menuItemWidth).toDp(), y = 0.dp) }
    FilterDropdownMenu(filterId, onFilterChange, topBarFilterMenu, { topBarFilterMenu = false }, offset = dpOffset)
}

@Composable
fun FilterMenuAction(filterId: Int, onFilterChange: (Int) -> Unit) {
    var topBarFilterMenu by remember { mutableStateOf(false) }
    FilterIconButton(onClick = { topBarFilterMenu = true }) {
        FilterDropdownMenu(filterId, onFilterChange, topBarFilterMenu, { topBarFilterMenu = false })
    }
}

@Composable
private fun FilterDropdownMenu(filterId: Int, onFilterChange: (Int) -> Unit, expanded: Boolean, onDismissRequest: () -> Unit, offset: DpOffset = DpOffset(0.dp, 0.dp)) {
    val filterPagesTitles = listOf(
        stringResource(id = R.string.tab_all),
        stringResource(id = R.string.tab_installed),
        stringResource(id = R.string.tab_not_installed),
        stringResource(id = R.string.tab_updatable),
    )

    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, offset = offset) {
        filterPagesTitles.forEachIndexed { index, title ->
            DropdownMenuItem(
                text = { Text(text = title, modifier = Modifier.padding(horizontal = 8.dp)) },
                leadingIcon = { RadioIcon(isChecked = (filterId == index)) },
                onClick = {
                    onDismissRequest()
                    onFilterChange(index)
                }
            )
        }
    }
}