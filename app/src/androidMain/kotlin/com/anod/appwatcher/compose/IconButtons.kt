package com.anod.appwatcher.compose

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anod.appwatcher.R

@Composable
fun SortIconButton(onClick: () -> Unit, content: @Composable () -> Unit = {}) {
    IconButton(onClick = onClick) {
        SortIcon()
        content()
    }
}

@Composable
fun TagAppIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        AddIcon(contentDescription = stringResource(id = R.string.tag_app))
    }
}

@Composable
fun FilterIconButton(onClick: () -> Unit, content: @Composable () -> Unit = {}) {
    IconButton(onClick = onClick) {
        FilterIcon()
        content()
    }
}

@Composable
fun MoreIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        MoreMenuIcon()
    }
}

@Composable
fun SearchIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        SearchIcon()
    }
}

@Composable
fun BackArrowIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        BackArrowIcon()
    }
}

@Composable
fun ShareIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        ShareIcon()
    }
}