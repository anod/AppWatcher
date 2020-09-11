// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import com.anod.appwatcher.database.entities.AppListItem

sealed class SectionHeader
object NewHeader : SectionHeader()
object RecentlyUpdatedHeader : SectionHeader()
object WatchingHeader : SectionHeader()
object RecentlyInstalledHeader : SectionHeader()
object OnDeviceHeader : SectionHeader()

sealed class SectionItem
object Empty : SectionItem()
class Header(val type: SectionHeader) : SectionItem()
object RecentItem : SectionItem()
class AppItem(val appListItem: AppListItem) : SectionItem()
class OnDeviceItem(val appListItem: AppListItem) : SectionItem()