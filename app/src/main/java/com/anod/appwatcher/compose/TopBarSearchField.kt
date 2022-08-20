package com.anod.appwatcher.compose

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
        }
    }
}