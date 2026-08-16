package com.anod.appwatcher.details

import com.anod.appwatcher.database.entities.AppChange
import info.anodsplace.framework.content.InstalledApps

/**
 * Index of the changelog entry the installed version separator is drawn above, or -1 when no
 * separator is needed. An index equal to the list size draws the separator below the last entry.
 *
 * Entries newer than the installed version are the ones the user has not seen yet. The separator
 * is placed after the last of them rather than after their count, because the details view model
 * prepends the store entry without comparing version codes and a rollback can leave the list out
 * of order. Counting would strand an unseen entry below the separator and make it read as already
 * seen. The separator is only useful while unseen entries exist, so it is skipped once the
 * installed version is the newest one.
 */
internal fun installedChangelogDividerIndex(changelogs: List<AppChange>, packageInfo: InstalledApps.Info): Int {
    if (!packageInfo.isInstalled) {
        return -1
    }

    val lastUnseenIndex = changelogs.indexOfLast { it.versionCode > packageInfo.versionCode }
    return if (lastUnseenIndex == -1) -1 else lastUnseenIndex + 1
}