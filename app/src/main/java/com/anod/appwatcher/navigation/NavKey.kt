package com.anod.appwatcher.navigation

import androidx.navigation3.runtime.NavKey
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Tag
import kotlinx.serialization.Serializable

@Serializable
data object MainScreenNavKey : NavKey

@Serializable
data class MarketSearchNavKey(
    val keyword: String = "",
    val focus: Boolean = false,
    val initiateSearch: Boolean = false
) : NavKey

@Serializable
data object SettingsNavKey : NavKey

@Serializable
data class InstalledNavKey(val importMode: Boolean) : NavKey

@Serializable
data object WishListNavKey : NavKey

@Serializable
data object HistoryNavKey : NavKey

@Serializable
data class TagWatchListNavKey(val tag: Tag) : NavKey

@Serializable
data class SelectedAppNavKey(val selectedApp: App) : NavKey
