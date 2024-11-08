package com.anod.appwatcher.watchlist

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntRect
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.DropdownMenuAction
import com.anod.appwatcher.compose.EditIcon
import com.anod.appwatcher.compose.SearchTopBar
import com.anod.appwatcher.compose.SearchTopBarEvent
import com.anod.appwatcher.compose.SearchTopBarState
import com.anod.appwatcher.compose.TagAppIconButton
import kotlin.math.roundToInt

@Composable
fun WatchListTopBar(
    title: String,
    subtitle: String?,
    showSearch: Boolean,
    filterQuery: String,
    containerColor: Color,
    contentColor: Color,
    visibleActions: @Composable () -> Unit,
    dropdownActions: @Composable (ColumnScope.(dismiss: () -> Unit, barBounds: IntRect) -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    onEvent: (WatchListEvent) -> Unit
) {
    var barBounds : IntRect by remember { mutableStateOf(IntRect(0, 0, 0, 0)) }
    SearchTopBar(
        state = SearchTopBarState(
            title = title,
            subtitle = subtitle,
            searchQuery = filterQuery,
            showSearch = showSearch,
            initialSearchFocus = true,
        ),
        containerColor = containerColor,
        contentColor = contentColor,
        onEvent = { event ->
            when (event) {
                SearchTopBarEvent.NavigationAction -> {
                    onEvent(WatchListEvent.NavigationButton)
                }
                SearchTopBarEvent.SearchAction -> {
                    onEvent(WatchListEvent.ShowSearch(show = true))
                }
                is SearchTopBarEvent.SearchChange -> {
                    onEvent(WatchListEvent.FilterByTitle(query = event.value))
                }
                SearchTopBarEvent.SearchSubmit -> {
                    onEvent(WatchListEvent.SearchSubmit)
                }
            }
        },
        navigationIcon = navigationIcon,
        modifier = Modifier.onGloballyPositioned {
            if (it.isAttached) {
                val size = it.size
                val position = it.positionInWindow()
                barBounds = IntRect(position.x.roundToInt(), position.y.roundToInt(), size.width, size.height)
            }
        },
        actions = {
            visibleActions()

            if (dropdownActions != null) {
                DropdownMenuAction { dismiss ->
                    dropdownActions(dismiss, barBounds)
                }
            }
        }
    )
}

@Preview(showBackground = false, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun DefaultPreview() {
    AppTheme(
        customPrimaryColor = Color.Cyan
    ) {
        Scaffold(
            topBar = {
                WatchListTopBar(
                    title = "What will happen when title is too long",
                    subtitle = "What will happen when subtitle is too long",
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    filterQuery = "",
                    showSearch = false,
                    visibleActions = {
                        TagAppIconButton(onClick = {})
                    },
                    dropdownActions = { dismiss, _ ->
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.menu_edit)) },
                            leadingIcon = { EditIcon() },
                            onClick = {
                                dismiss()
                            }
                        )
                    },
                    onEvent = { }
                )
            }
        ) { paddingValues ->
            Box(Modifier.padding(paddingValues)) {
            }
        }
    }
}