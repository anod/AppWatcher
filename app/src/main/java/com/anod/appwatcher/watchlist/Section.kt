// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.utils.hashCodeOf

sealed class SectionHeader
object NewHeader : SectionHeader()
object RecentlyUpdatedHeader : SectionHeader()
object WatchingHeader : SectionHeader()
object RecentlyInstalledHeader : SectionHeader()
object OnDeviceHeader : SectionHeader()

sealed class SectionItem
class Header(val type: SectionHeader) : SectionItem()
object RecentItem : SectionItem()
class AppItem(val appListItem: AppListItem, val isLocal: Boolean) : SectionItem() {
    override fun hashCode(): Int {
        return hashCodeOf(appListItem, isLocal)
    }

    override fun equals(other: Any?): Boolean {
        val item = other as? AppItem ?: return false
        return hashCode() == item.hashCode()
    }
}

class OnDeviceItem(val appListItem: AppListItem, var showSelection: Boolean) : SectionItem() {
    override fun hashCode(): Int {
        return hashCodeOf(appListItem, showSelection)
    }

    override fun equals(other: Any?): Boolean {
        val item = other as? OnDeviceItem ?: return false
        return hashCode() == item.hashCode()
    }
}