package com.anod.appwatcher.watchlist

import com.anod.appwatcher.model.AppInfoMetadata

/**
 * @author Alex Gavrishev
 * @date 02/06/2018
 */

interface SectionHeaderFactory {
    fun insertSeparator(before: SectionItem?, after: SectionItem?): Header?

    class Empty : SectionHeaderFactory {
        override fun insertSeparator(before: SectionItem?, after: SectionItem?): Header? = null
    }
}

class DefaultSectionHeaderFactory(
        private var showRecentlyUpdated: Boolean
) : SectionHeaderFactory {

    override fun insertSeparator(before: SectionItem?, after: SectionItem?): Header? {
        if (after == null) {
            // we're at the end of the list
            return null
        }

        if (before == null) {
            when (after) {
                is RecentItem -> return Header(RecentlyInstalledHeader)
                is OnDeviceItem -> return Header(OnDeviceHeader)
                is AppItem -> {
                    val appListItem = after.appListItem
                    val status = appListItem.app.status
                    if (status == AppInfoMetadata.STATUS_UPDATED) {
                        return Header(NewHeader)
                    }
                    if (showRecentlyUpdated && appListItem.recentFlag) {
                        return Header(RecentlyUpdatedHeader)
                    }
                    return Header(WatchingHeader)
                }
                is EmptyItem -> {
                }
                is Header -> {
                }
            }
        }

        if (before is RecentItem) {
            when (after) {
                is OnDeviceItem -> return Header(OnDeviceHeader)
                is AppItem -> {
                    val appListItem = after.appListItem
                    val status = appListItem.app.status
                    if (status == AppInfoMetadata.STATUS_UPDATED) {
                        return Header(NewHeader)
                    }
                    if (showRecentlyUpdated && appListItem.recentFlag) {
                        return Header(RecentlyUpdatedHeader)
                    }
                    return Header(WatchingHeader)
                }
            }
        }

        if (before is AppItem) {
            when (after) {
                is OnDeviceItem -> return Header(OnDeviceHeader)
                is AppItem -> {
                    val beforeItem = before.appListItem
                    val afterItem = after.appListItem
                    if (
                            beforeItem.app.status == AppInfoMetadata.STATUS_UPDATED
                            && afterItem.app.status == AppInfoMetadata.STATUS_NORMAL
                    ) {
                        if (showRecentlyUpdated && afterItem.recentFlag) {
                            return Header(RecentlyUpdatedHeader)
                        }
                        return Header(WatchingHeader)
                    } else if (
                            showRecentlyUpdated
                            && beforeItem.app.status == AppInfoMetadata.STATUS_NORMAL
                            && afterItem.app.status == AppInfoMetadata.STATUS_NORMAL
                    ) {
                        if (beforeItem.recentFlag && !afterItem.recentFlag) {
                            return Header(WatchingHeader)
                        }
                    }
                }
            }
        }

        return null
    }
}