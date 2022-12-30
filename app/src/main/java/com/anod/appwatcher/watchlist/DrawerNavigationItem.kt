package com.anod.appwatcher.watchlist

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.ui.graphics.vector.ImageVector
import com.anod.appwatcher.R

data class DrawerNavigationItem(
    val id: Id,
    val icon: ImageVector,
    @StringRes val title: Int
) {
    sealed interface Id {
        object Refresh : Id
        object Add : Id
        object Installed : Id
        object Wishlist : Id
        object Settings : Id
    }
}

val drawerNavigationItems = listOf(
    DrawerNavigationItem(id = DrawerNavigationItem.Id.Refresh, icon = Icons.Default.Refresh, title = R.string.navdrawer_item_refresh),
    DrawerNavigationItem(id = DrawerNavigationItem.Id.Add, icon = Icons.Default.AddCircle, title = R.string.navdrawer_item_add),
    DrawerNavigationItem(id = DrawerNavigationItem.Id.Installed, icon = Icons.Default.SystemUpdate, title = R.string.installed),
    DrawerNavigationItem(id = DrawerNavigationItem.Id.Wishlist, icon = Icons.Default.Bookmark, title = R.string.navdrawer_item_wishlist),
    DrawerNavigationItem(id = DrawerNavigationItem.Id.Settings, icon = Icons.Default.Settings, title = R.string.navdrawer_item_settings),
)
