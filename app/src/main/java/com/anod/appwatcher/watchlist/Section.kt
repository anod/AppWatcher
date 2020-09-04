// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import com.anod.appwatcher.database.entities.AppListItem

sealed class SectionHeader
object New : SectionHeader()
object RecentlyUpdated : SectionHeader()
object Watching : SectionHeader()
object RecentlyInstalled : SectionHeader()
object OnDevice : SectionHeader()

sealed class SectionItem
object Empty : SectionItem()
class Header(val type: SectionHeader) : SectionItem()
class RecentItem(val packageNames: List<Pair<String, Int>>) : SectionItem()
class AppItem(val appListItem: AppListItem) : SectionItem()
class OnDeviceItem(val appListItem: AppListItem) : SectionItem()