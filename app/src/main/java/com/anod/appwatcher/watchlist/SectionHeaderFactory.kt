package com.anod.appwatcher.watchlist

import com.anod.appwatcher.database.entities.App

/**
 * @author Alex Gavrishev
 * @date 02/06/2018
 */

interface SectionHeaderFactory {
    fun insertSeparator(before: SectionItem?, after: SectionItem?): SectionItem.Header?

    class Empty : SectionHeaderFactory {
        override fun insertSeparator(before: SectionItem?, after: SectionItem?): SectionItem.Header? = null
    }
}

class DefaultSectionHeaderFactory(private var showRecentlyDiscovered: Boolean) : SectionHeaderFactory {

    private val SectionItem.App.hasUpdate: Boolean
        get() = isPackageEnabled && appListItem.app.status == App.STATUS_UPDATED

    private val SectionItem.App.isRecentlyDiscovered: Boolean
        get() = isPackageEnabled && appListItem.recentFlag

    override fun insertSeparator(before: SectionItem?, after: SectionItem?): SectionItem.Header? {
        if (after == null) {
            // we're at the end of the list
            return null
        }

        if (before == null) {
            when (after) {
                is SectionItem.Recent -> return SectionItem.Header(SectionHeader.RecentlyInstalled)
                is SectionItem.OnDevice -> return SectionItem.Header(SectionHeader.OnDevice)
                is SectionItem.App -> {
                    if (after.hasUpdate) {
                        return SectionItem.Header(SectionHeader.New)
                    }
                    if (showRecentlyDiscovered && after.isRecentlyDiscovered) {
                        return SectionItem.Header(SectionHeader.RecentlyDiscovered)
                    }
                    return SectionItem.Header(SectionHeader.Watching)
                }
                is SectionItem.Empty -> {
                }
                is SectionItem.Header -> {
                }
            }
        }

        if (before is SectionItem.Recent) {
            when (after) {
                is SectionItem.OnDevice -> return SectionItem.Header(SectionHeader.OnDevice)
                is SectionItem.App -> {
                    if (after.hasUpdate) {
                        return SectionItem.Header(SectionHeader.New)
                    }
                    if (showRecentlyDiscovered && after.isRecentlyDiscovered) {
                        return SectionItem.Header(SectionHeader.RecentlyDiscovered)
                    }
                    return SectionItem.Header(SectionHeader.Watching)
                }
                SectionItem.Empty -> {}
                is SectionItem.Header -> {}
                SectionItem.Recent -> {}
            }
        }

        if (before is SectionItem.App) {
            when (after) {
                is SectionItem.OnDevice -> return SectionItem.Header(SectionHeader.OnDevice)
                is SectionItem.App -> {
                    if (before.hasUpdate && !after.hasUpdate) {
                        if (showRecentlyDiscovered && after.isRecentlyDiscovered) {
                            return SectionItem.Header(SectionHeader.RecentlyDiscovered)
                        }
                        return SectionItem.Header(SectionHeader.Watching)
                    } else if (
                        showRecentlyDiscovered &&
                        !before.hasUpdate &&
                        !after.hasUpdate
                    ) {
                        if (before.isRecentlyDiscovered && !after.isRecentlyDiscovered) {
                            return SectionItem.Header(SectionHeader.Watching)
                        }
                    }
                }
                SectionItem.Empty -> {}
                is SectionItem.Header -> {}
                SectionItem.Recent -> {}
            }
        }

        return null
    }
}