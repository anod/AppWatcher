package com.anod.appwatcher.watchlist

import android.util.SparseArray
import androidx.core.util.set

/**
 * @author Alex Gavrishev
 * @date 02/06/2018
 */
class SectionHeaderFactory(
        private var showRecentlyUpdated: Boolean,
        private var hasSectionRecent: Boolean,
        private var hasSectionOnDevice: Boolean
) {


    private fun create(totalAppsCount: Int, newAppsCount: Int, recentlyUpdatedCount: Int, updatableNewCount: Int): SparseArray<SectionHeader> {
        val sections = SparseArray<SectionHeader>()
        if (newAppsCount > 0) {
            sections[0] = New(newAppsCount, updatableNewCount)
        }
        val effectiveRecentlyUpdatedCount  = if (showRecentlyUpdated) recentlyUpdatedCount else 0
        if (effectiveRecentlyUpdatedCount > 0) {
            sections[newAppsCount] = RecentlyUpdated(effectiveRecentlyUpdatedCount)
        }
        sections[effectiveRecentlyUpdatedCount + newAppsCount] = Watching(totalAppsCount - effectiveRecentlyUpdatedCount - newAppsCount)
        return sections
    }

    fun create(totalAppsCount: Int, newAppsCount: Int, recentlyUpdatedCount: Int, updatableNewCount: Int, hasRecentlyInstalled: Boolean, hasInstalledPackages: Boolean): SparseArray<SectionHeader> {
        val sections = create(totalAppsCount, newAppsCount, recentlyUpdatedCount, updatableNewCount)
        val isRecentVisible = hasSectionRecent && hasRecentlyInstalled
        val isOnDeviceVisible = hasSectionOnDevice && hasInstalledPackages

        if (isRecentVisible) {
            val newSections = SparseArray<SectionHeader>()
            newSections[0] = RecentlyInstalled
            for (i in 0 until sections.size()) {
                newSections[sections.keyAt(i) + 1] = sections.valueAt(i)
            }

            if (isOnDeviceVisible) {
                newSections[totalAppsCount + 1] = OnDevice
            }
            return newSections
        }

        if (isOnDeviceVisible) {
            sections[totalAppsCount] = OnDevice
        }

        return sections
    }
}