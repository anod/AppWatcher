// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import com.anod.appwatcher.database.entities.AppListItem
import info.anodsplace.ktx.hashCodeOf

sealed class SectionHeader
object NewHeader : SectionHeader()
object RecentlyUpdatedHeader : SectionHeader()
object WatchingHeader : SectionHeader()
object RecentlyInstalledHeader : SectionHeader()
object OnDeviceHeader : SectionHeader()

sealed interface SectionItem {
    class Header(val type: SectionHeader) : SectionItem
    object Recent : SectionItem
    object Empty : SectionItem

    class App(val appListItem: AppListItem, val isLocal: Boolean) : SectionItem {
        override fun hashCode(): Int {
            return hashCodeOf(appListItem, isLocal)
        }

        override fun equals(other: Any?): Boolean {
            val item = other as? App ?: return false
            return hashCode() == item.hashCode()
        }
    }

    class OnDevice(val appListItem: AppListItem, var showSelection: Boolean) : SectionItem {
        override fun hashCode(): Int {
            return hashCodeOf(appListItem, showSelection)
        }

        override fun equals(other: Any?): Boolean {
            val item = other as? OnDevice ?: return false
            return hashCode() == item.hashCode()
        }
    }
}