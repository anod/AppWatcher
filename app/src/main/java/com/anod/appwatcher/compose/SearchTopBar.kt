package com.anod.appwatcher.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import com.anod.appwatcher.R
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.delay

sealed interface SearchTopBarEvent {
    class SearchChange(val value: String) : SearchTopBarEvent
    object SearchSubmit : SearchTopBarEvent
    object SearchAction : SearchTopBarEvent
    object NavigationAction : SearchTopBarEvent
}

data class SearchTopBarState(
    val title: String,
    val subtitle: String? = null,
    val searchQuery: String = "",
    val showSearch: Boolean = false,
    val initialSearchFocus: Boolean = false,
)

@Composable
fun SearchTopBar(
    title: String,
    showSearch: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    searchQuery: String = "",
    initialSearchFocus: Boolean = false,
    hideSearchOnNavigation: Boolean = true,
    onValueChange: (String) -> Unit = { },
    onSearchSubmit: (String) -> Unit = { },
    onNavigation: () -> Unit = { },
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable () -> Unit = { },
) {
    var showSearchView by remember { mutableStateOf(showSearch) }
    var searchQueryValue by remember { mutableStateOf(searchQuery) }
    SearchTopBar(
        state = SearchTopBarState(
            title = title,
            subtitle = subtitle,
            searchQuery = searchQueryValue,
            showSearch = showSearchView,
            initialSearchFocus = initialSearchFocus
        ),
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        navigationIcon = navigationIcon,
        actions = actions,
        onEvent = { event ->
            when (event) {
                SearchTopBarEvent.NavigationAction -> {
                    if (showSearchView) {
                        if (hideSearchOnNavigation) {
                            onValueChange("")
                            showSearchView = false
                        } else {
                            onNavigation()
                        }
                    } else {
                        onNavigation()
                    }
                }
                SearchTopBarEvent.SearchAction -> {
                    showSearchView = true
                }
                is SearchTopBarEvent.SearchChange -> {
                    searchQueryValue = event.value
                    onValueChange(event.value)
                }
                is SearchTopBarEvent.SearchSubmit -> {
                    onSearchSubmit(searchQueryValue)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    state: SearchTopBarState,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable () -> Unit = { },
    onEvent: (SearchTopBarEvent) -> Unit = { }
) {
    var requestFocus by remember { mutableStateOf(state.initialSearchFocus) }
    TopAppBar(
        modifier = modifier,
        title = {
            if (state.showSearch) {
                TopBarSearchField(
                    query = state.searchQuery,
                    onValueChange = { onEvent(SearchTopBarEvent.SearchChange(it)) },
                    onSubmit = { onEvent(SearchTopBarEvent.SearchSubmit) },
                    requestFocus = requestFocus,
                    contentColor = contentColor,
                    containerColor = containerColor
                )
            } else {
                if (state.subtitle != null) {
                    Column {
                        Text(
                            state.title,
                            color = contentColor,
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            state.subtitle,
                            color = contentColor,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        state.title,
                        color = contentColor,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { onEvent(SearchTopBarEvent.NavigationAction) }) {
                when {
                    state.showSearch -> BackArrowIcon()
                    navigationIcon != null -> navigationIcon()
                    else -> BackArrowIcon()
                }
            }
        },
        actions = {
            if (!state.showSearch) {
                SearchIconButton(onClick = {
                    onEvent(SearchTopBarEvent.SearchAction)
                    requestFocus = true
                })
            }

            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor,
        )
    )
}

@Composable
fun TopBarSearchField(
    query: String = "",
    onValueChange: (String) -> Unit = { },
    onSubmit: () -> Unit = { },
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    requestFocus: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    TextField(
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged {
                AppLog.d("onFocusChanged $it")
            },
        value = query,
        onValueChange = { onValueChange(it) },
        placeholder = {
            Text(text = stringResource(id = R.string.search))
        },
        leadingIcon = { SearchIcon() },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    ClearIcon()
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSubmit()
                focusManager.clearFocus()
            }
        ),
        textStyle = MaterialTheme.typography.labelLarge,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            focusedTextColor = contentColor,
            unfocusedTextColor = contentColor,
            focusedLeadingIconColor = contentColor,
            unfocusedLeadingIconColor = contentColor.copy(alpha = 0.4f),
            focusedPlaceholderColor = contentColor.copy(alpha = 0.4f),
            unfocusedPlaceholderColor = contentColor.copy(alpha = 0.4f),
            focusedTrailingIconColor = contentColor,
            unfocusedTrailingIconColor = contentColor.copy(alpha = 0.4f),
        )
    )

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
            delay(100)
            keyboard?.show()
        } else {
            keyboard?.hide()
        }
    }
}