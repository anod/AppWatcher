package com.anod.appwatcher.watchlist

import com.anod.appwatcher.model.AppInfoMetadata

/**
 * @author Alex Gavrishev
 * @date 02/06/2018
 */
class SectionHeaderFactory(
        private var showRecentlyUpdated: Boolean
) {

    fun insertSeparator(before: SectionItem?, after: SectionItem?): Header? {
        if (after == null) {
            // we're at the end of the list
            return null
        }

        if (before == null) {
            when (after) {
                is RecentItem -> return Header(RecentlyInstalled)
                is OnDeviceItem -> return Header(OnDevice)
                is AppItem -> {
                    val appListItem = after.appListItem
                    val status = appListItem.app.status
                    if (status == AppInfoMetadata.STATUS_UPDATED) {
                        return Header(New)
                    }
                    if (showRecentlyUpdated && appListItem.recentFlag) {
                        return Header(RecentlyUpdated)
                    }
                    return Header(Watching)
                }
            }
        }

        if (before is RecentItem) {
            when (after) {
                is OnDeviceItem -> return Header(OnDevice)
                is AppItem -> {
                    val appListItem = after.appListItem
                    val status = appListItem.app.status
                    if (status == AppInfoMetadata.STATUS_UPDATED) {
                        return Header(New)
                    }
                    if (showRecentlyUpdated && appListItem.recentFlag) {
                        return Header(RecentlyUpdated)
                    }
                    return Header(Watching)
                }
            }
        }

        if (before is AppItem) {
            when (after) {
                is OnDeviceItem -> return Header(OnDevice)
                is AppItem -> {
                    val beforeItem = before.appListItem
                    val afterItem = after.appListItem
                    if (
                            beforeItem.app.status == AppInfoMetadata.STATUS_UPDATED
                            && afterItem.app.status == AppInfoMetadata.STATUS_NORMAL
                    ) {
                        if (showRecentlyUpdated && afterItem.recentFlag) {
                            return Header(RecentlyUpdated)
                        }
                        return Header(Watching)
                    } else if (
                            showRecentlyUpdated
                            && beforeItem.app.status == AppInfoMetadata.STATUS_NORMAL
                            && afterItem.app.status == AppInfoMetadata.STATUS_NORMAL
                    ) {
                        if (beforeItem.recentFlag && !afterItem.recentFlag) {
                            return Header(Watching)
                        }
                    }
                }
            }
        }

        return null
    }
}