package com.anod.appwatcher.watchlist

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.anod.appwatcher.R

@Immutable
data class DrawerItem(
    val id: Id,
    val icon: ImageVector,
    @param:StringRes val title: Int
) {
    sealed interface Id {
        object Refresh : Id
        object Add : Id
        object Installed : Id
        object Wishlist : Id
        object Purchases : Id
        object Settings : Id
    }
}

val drawerItems = listOf(
    DrawerItem(id = DrawerItem.Id.Refresh, icon = Icons.Default.Refresh, title = R.string.navdrawer_item_refresh),
    DrawerItem(id = DrawerItem.Id.Add, icon = Icons.Default.AddCircle, title = R.string.navdrawer_item_add),
    DrawerItem(id = DrawerItem.Id.Installed, icon = Icons.Default.SystemUpdate, title = R.string.installed),
    DrawerItem(id = DrawerItem.Id.Wishlist, icon = Icons.Default.Bookmark, title = R.string.navdrawer_item_wishlist),
    DrawerItem(id = DrawerItem.Id.Purchases, icon = Icons.Default.History, title = R.string.navdrawer_item_purchases),
    DrawerItem(id = DrawerItem.Id.Settings, icon = Icons.Default.Settings, title = R.string.navdrawer_item_settings),
)