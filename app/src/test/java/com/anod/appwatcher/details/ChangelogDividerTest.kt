package com.anod.appwatcher.details

import com.anod.appwatcher.database.entities.AppChange
import info.anodsplace.framework.content.InstalledApps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogDividerTest {

    private fun change(versionCode: Int) = AppChange(
        appId = "com.anod.appwatcher",
        versionCode = versionCode,
        versionName = "v$versionCode",
        details = "",
        uploadDate = "",
        noNewDetails = false
    )

    private val changelogs = listOf(change(17300), change(17200), change(17100), change(17000))

    private fun installed(versionCode: Int) = InstalledApps.Info(versionCode = versionCode, versionName = "installed")

    @Test
    fun knownInstalledVersionIsSeparatedFromNewerEntries() {
        assertEquals(2, installedChangelogDividerIndex(changelogs, installed(17100)))
    }

    @Test
    fun unknownInstalledVersionIsSeparatedFromNewerEntries() {
        assertEquals(2, installedChangelogDividerIndex(changelogs, installed(17150)))
    }

    @Test
    fun newestEntryInstalledDrawsNothing() {
        assertEquals(-1, installedChangelogDividerIndex(changelogs, installed(17300)))
    }

    @Test
    fun versionNewerThanEveryEntryDrawsNothing() {
        assertEquals(-1, installedChangelogDividerIndex(changelogs, installed(17400)))
    }

    @Test
    fun versionOlderThanEveryEntryDrawsSeparatorBelowTheList() {
        assertEquals(changelogs.size, installedChangelogDividerIndex(changelogs, installed(16000)))
    }

    @Test
    fun rolledBackEntryPrependedByMergeStillSeparatesUnseenEntries() {
        // A store rollback prepends an older remote entry, so the list is no longer sorted
        val rolledBack = listOf(change(17200), change(17300), change(17200))

        // Below the unseen 17300 entry, not above it
        assertEquals(2, installedChangelogDividerIndex(rolledBack, installed(17200)))
    }

    @Test
    fun unseenEntriesNeverEndUpBelowTheSeparatorInAnUnsortedList() {
        val rolledBack = listOf(change(17200), change(17300), change(17200))

        val index = installedChangelogDividerIndex(rolledBack, installed(17200))

        assertTrue(rolledBack.drop(index).none { it.versionCode > 17200 })
    }

    @Test
    fun notInstalledDrawsNothing() {
        assertEquals(-1, installedChangelogDividerIndex(changelogs, installed(0)))
    }

    @Test
    fun emptyChangelogDrawsNothing() {
        assertEquals(-1, installedChangelogDividerIndex(emptyList(), installed(17100)))
    }
}