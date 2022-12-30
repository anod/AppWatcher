package com.anod.appwatcher.watchlist

import android.accounts.Account
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchListDrawer(mainState: MainViewState, onMainEvent: (MainViewEvent) -> Unit) {
    ModalDrawerSheet(
        windowInsets = WindowInsets.navigationBars
    ) {
        DrawerContent(
            mainState = mainState,
            onMainEvent = onMainEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContent(mainState: MainViewState, onMainEvent: (MainViewEvent) -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        if (mainState.account == null) {
            OutlinedButton(
                modifier = Modifier.padding(top = 4.dp),
                onClick = {  }
            ) {
                Text(text = stringResource(id = R.string.choose_an_account))
            }
        } else {
            TextButton(
                modifier = Modifier.padding(top = 4.dp),
                onClick = {  }
            ) {
                Text(text = mainState.account.name)
            }
        }
        if (mainState.lastUpdate > 0) {
            val context = LocalContext.current
            val relativeTime = remember(mainState.lastUpdate) {
                DateUtils.getRelativeDateTimeString(context, mainState.lastUpdate, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0)
            }
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(id = R.string.last_update, relativeTime)
            )
        }
    }
    mainState.navigationItems.forEach { item ->
        val title = stringResource(id = item.title)
        NavigationDrawerItem(
            icon = { Icon(item.icon, contentDescription = title) },
            label = { Text(title) },
            selected = false,
            onClick = {
                onMainEvent(MainViewEvent.NavigateTo(item.id))
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}


@Preview(showSystemUi = true)
@Composable
fun DrawerContentPreviewNoAccount() {
    AppTheme {
        WatchListDrawer(
            mainState = MainViewState(),
            onMainEvent = { }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun DrawerContentPreviewWithAccount() {
    AppTheme {
        WatchListDrawer(
            mainState = MainViewState(
                account = Account("very_long_email_address@example.com", "test"),
                lastUpdate = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2)
            ),
            onMainEvent = {}
        )
    }
}
