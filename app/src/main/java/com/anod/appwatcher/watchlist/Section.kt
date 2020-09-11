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
object Empty : SectionItem()
class Header(val type: SectionHeader) : SectionItem()
class RecentItem(val sortId: Int, val titleFilter: String) : SectionItem() {
    override fun hashCode() = hashCodeOf(sortId, titleFilter)
    override fun equals(other: Any?): Boolean {
        val item = other as? RecentItem ?: return false
        return hashCode() == item.hashCode()
    }
}

class AppItem(val appListItem: AppListItem) : SectionItem()
class OnDeviceItem(val appListItem: AppListItem) : SectionItem()