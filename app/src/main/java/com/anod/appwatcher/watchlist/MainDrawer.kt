package com.anod.appwatcher.watchlist

import android.accounts.Account
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AddIcon
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.TagIcon
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.utils.isLightColor
import java.util.concurrent.TimeUnit

@Composable
fun MainDrawer(mainState: MainViewState, onMainEvent: (MainViewEvent) -> Unit) {
    ModalDrawerSheet(
        windowInsets = WindowInsets.navigationBars
    ) {
        Column(Modifier. verticalScroll(rememberScrollState())) {
            DrawerContent(
                mainState = mainState,
                onMainEvent = onMainEvent
            )
        }
    }
}

@Composable
private fun DrawerContent(mainState: MainViewState, onMainEvent: (MainViewEvent) -> Unit) {

    DrawerHeader(
        mainState = mainState,
        onMainEvent = onMainEvent
    )

    mainState.drawerItems.forEach { item ->
        val title = stringResource(id = item.title)
        NavigationDrawerItem(
            icon = { Icon(item.icon, contentDescription = title) },
            label = { Text(title) },
            selected = false,
            onClick = {
                onMainEvent(MainViewEvent.DrawerItemClick(item.id))
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }

    HorizontalDivider(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
    )

    Text(
        text = stringResource(id = R.string.tags),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
    )

    mainState.tags.forEach { (tag, count) ->
        NavigationDrawerItem(
            icon = {  TagIcon(outlined = true, contentDescription = tag.name) },
            label = { Text(if (tag.isEmpty) stringResource(R.string.untagged) else tag.name) },
            badge = { TagBadge(if (tag.isEmpty) MaterialTheme.colorScheme.primaryContainer else Color(tag.color), count) },
            selected = false,
            onClick = {
                onMainEvent(MainViewEvent.NavigateToTag(tag))
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).height(42.dp)
        )
    }

    NavigationDrawerItem(
        icon = { AddIcon() },
        label = { Text(text = stringResource(id = R.string.menu_add)) },
        selected = false,
        onClick = {
              onMainEvent(MainViewEvent.AddNewTagDialog(show = true))
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).height(48.dp)
    )
}

@Composable
private fun DrawerHeader(mainState: MainViewState, onMainEvent: (MainViewEvent) -> Unit) {
    val inset = WindowInsets.statusBars.asPaddingValues()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 24.dp)
            .padding(top = inset.calculateTopPadding() + 12.dp, bottom = 12.dp)
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedButton(
            modifier = Modifier.padding(top = 12.dp),
            onClick = { onMainEvent(MainViewEvent.ChooseAccount) }
        ) {
            if (mainState.account == null) {
                Text(text = stringResource(id = R.string.choose_an_account))
            } else {
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
}

@Composable
private fun TagBadge(color: Color, count: Int, modifier: Modifier = Modifier) {
    Badge(
        modifier
            .size(24.dp),
        containerColor = color,
        contentColor = if (color.isLightColor) Color.Black else Color.White
    ) {
        Text(
            text = if (count > 99) "99+" else "" + count,
            fontSize = if (count > 99) 8.sp else 12.sp,
            maxLines = 1
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun DrawerContentPreviewNoAccount() {
    AppTheme {
        MainDrawer(
            mainState = MainViewState(),
            onMainEvent = { }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun DrawerContentPreviewWithAccount() {
    AppTheme {
        MainDrawer(
            mainState = MainViewState(
                account = Account("very_long_email_address@example.com", "test"),
                lastUpdate = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2),
                tags = listOf(
                    Pair(Tag("Banana", Color.Yellow.toArgb()), 90),
                    Pair(Tag("Kiwi", Color.DarkGray.toArgb()), 125),
                    Pair(Tag("Apple", Color.Red.toArgb()), 0),
                )
            ),
            onMainEvent = {}
        )
    }
}
