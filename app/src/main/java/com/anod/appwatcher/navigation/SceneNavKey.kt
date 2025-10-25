package com.anod.appwatcher.navigation

import androidx.navigation3.runtime.NavKey
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Tag
import info.anodsplace.framework.content.ScreenCommonNavKey
import kotlinx.serialization.Serializable


val ScreenCommonNavKey.asNavKey: NavKey
    get() = when (this) {
        is SceneNavKey -> this
        else -> throw IllegalArgumentException("Unknown ScreenCommonNavKey: $this")
    }

sealed interface SceneNavKey : NavKey, ScreenCommonNavKey {
    @Serializable
    data object Main : SceneNavKey

    @Serializable
    data class Search(
        val keyword: String = "",
        val focus: Boolean = false,
        val initiateSearch: Boolean = false,
        val isPackageSearch: Boolean = false,
        val isShareSource: Boolean = false
    ) : SceneNavKey

    @Serializable
    data object Settings : SceneNavKey

    @Serializable
    data object RefreshHistory : SceneNavKey

    @Serializable
    data object UserLog : SceneNavKey

    @Serializable
    data class Installed(val importMode: Boolean) : SceneNavKey

    @Serializable
    data object WishList : SceneNavKey

    @Serializable
    data object PurchaseHistory : SceneNavKey

    @Serializable
    data class TagWatchList(val tag: Tag) : SceneNavKey

    @Serializable
    data class AppDetails(val selectedApp: App) : SceneNavKey
}

