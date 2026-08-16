package com.anod.appwatcher.details

import com.anod.appwatcher.database.entities.AppChange
import info.anodsplace.framework.content.InstalledApps

/**
 * Index of the changelog entry the installed version separator is drawn above, or -1 when no
 * separator is needed. An index equal to the list size draws the separator below the last entry.
 *
 * Changelog entries are ordered by version code descending, so the entries the user has not seen
 * yet are the ones newer than the installed version. Counting them keeps the separator in place
 * even when a store rollback prepends an older entry to the list. The separator is only useful
 * while unseen entries exist, so it is skipped once the installed version is the newest one.
 */
fun installedChangelogDividerIndex(changelogs: List<AppChange>, packageInfo: InstalledApps.Info): Int {
    if (!packageInfo.isInstalled) {
        return -1
    }

    val unseenCount = changelogs.count { it.versionCode > packageInfo.versionCode }
    return if (unseenCount == 0) -1 else unseenCount
}