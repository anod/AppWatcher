package com.anod.appwatcher.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.anod.appwatcher.R
import info.anodsplace.applog.AppLog
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
        title: String,
        subtitle: String? = null,
        containerColor: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        searchQuery: String = "",
        showSearch: Boolean = false,
        initialSearchFocus: Boolean = false,
        onValueChange: (String) -> Unit = { },
        onSearchAction: (String) -> Unit = { },
        onNavigation: () -> Unit = { },
        actions: @Composable () -> Unit = { },
) {
    var showSearchView by remember { mutableStateOf(showSearch) }
    var requestFocus by remember { mutableStateOf(initialSearchFocus) }
    SmallTopAppBar(
            title = {
                if (showSearchView) {
                    TopBarSearchField(
                            query = searchQuery,
                            onValueChange = onValueChange,
                            onSearchAction = onSearchAction,
                            requestFocus = requestFocus,
                            contentColor = contentColor,
                            containerColor = containerColor
                    )
                } else {
                    if (subtitle != null) {
                        Column {
                            Text(title, color = contentColor, style = MaterialTheme.typography.headlineSmall)
                            Text(subtitle, color = contentColor, style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        Text(title, color = contentColor, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            },
            actions = {
                if (!showSearchView) {
                    IconButton(onClick = {
                        showSearchView = true
                        requestFocus = true
                    }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(id = R.string.menu_filter))
                    }
                }

                actions()
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (showSearchView) {
                        showSearchView = false
                    } else {
                        onNavigation()
                    }
                }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                }
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor,
            )
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TopBarSearchField(
        query: String = "",
        onValueChange: (String) -> Unit = { },
        onSearchAction: (String) -> Unit = { },
        containerColor: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        requestFocus: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var searchValue by remember { mutableStateOf(query) }
    val keyboard = LocalSoftwareKeyboardController.current

    TextField(
            modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        AppLog.d("onFocusChanged $it")
                    },
            value = searchValue,
            onValueChange = {
                searchValue = it
                onValueChange(it)
            },
            placeholder = {
                Text(text = stringResource(id = R.string.search))
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(id = R.string.menu_filter))
            },
            keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchAction(searchValue)
                        focusManager.clearFocus()
                    }
            ),
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                    containerColor = containerColor,
                    textColor = contentColor,
                    focusedLeadingIconColor = contentColor,
                    unfocusedLeadingIconColor = contentColor.copy(alpha = 0.4f),
                    placeholderColor = contentColor.copy(alpha = 0.4f),
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