// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import com.anod.appwatcher.database.entities.AppListItem
import info.anodsplace.framework.content.InstalledPackage

sealed class SectionHeader
class New(val count: Int, val updatable: Int) : SectionHeader()
class RecentlyUpdated(val count: Int) : SectionHeader()
class Watching(val count: Int) : SectionHeader()
object RecentlyInstalled : SectionHeader()
object OnDevice : SectionHeader()

sealed class SectionItem
object Empty : SectionItem()
class Header(val type: SectionHeader) : SectionItem()
class RecentItem(val packageNames: List<Pair<String, Int>>) : SectionItem()
class AppItem(val appListItem: AppListItem) : SectionItem()
class OnDeviceItem(val installedPackage: InstalledPackage) : SectionItem()