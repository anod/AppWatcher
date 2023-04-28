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

class DefaultSectionHeaderFactory(
    private var showRecentlyDiscovered: Boolean
) : SectionHeaderFactory {

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
                    val appListItem = after.appListItem
                    val status = appListItem.app.status
                    if (status == App.STATUS_UPDATED) {
                        return SectionItem.Header(SectionHeader.New)
                    }
                    if (showRecentlyDiscovered && appListItem.recentFlag) {
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
                    val appListItem = after.appListItem
                    val status = appListItem.app.status
                    if (status == App.STATUS_UPDATED) {
                        return SectionItem.Header(SectionHeader.New)
                    }
                    if (showRecentlyDiscovered && appListItem.recentFlag) {
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
                    val beforeItem = before.appListItem
                    val afterItem = after.appListItem
                    if (
                            beforeItem.app.status == App.STATUS_UPDATED
                            && afterItem.app.status == App.STATUS_NORMAL
                    ) {
                        if (showRecentlyDiscovered && afterItem.recentFlag) {
                            return SectionItem.Header(SectionHeader.RecentlyDiscovered)
                        }
                        return SectionItem.Header(SectionHeader.Watching)
                    } else if (
                        showRecentlyDiscovered
                            && beforeItem.app.status == App.STATUS_NORMAL
                            && afterItem.app.status == App.STATUS_NORMAL
                    ) {
                        if (beforeItem.recentFlag && !afterItem.recentFlag) {
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