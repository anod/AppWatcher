// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

import com.anod.appwatcher.sync.SyncNotificationFilter
import com.anod.appwatcher.sync.UpdatedApp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyncNotificationFilterTest {

    @Test
    fun testHasFiltered() {
        assertFalse(
                SyncNotificationFilter(
                filterInstalled = false,
                filterInstalledUpToDate = false,
                filterNoChanges = false).hasFilters)

        assertTrue(SyncNotificationFilter(
                filterInstalled = true,
                filterInstalledUpToDate = false,
                filterNoChanges = false).hasFilters)

        assertTrue(SyncNotificationFilter(
                filterInstalled = false,
                filterInstalledUpToDate = true,
                filterNoChanges = false).hasFilters)

        assertTrue(SyncNotificationFilter(
                filterInstalled = false,
                filterInstalledUpToDate = false,
                filterNoChanges = true).hasFilters)
    }

    @Test
    fun testFilterInstalled() {
        val filter = SyncNotificationFilter(
                filterInstalled = true,
                filterInstalledUpToDate = false,
                filterNoChanges = false)
        val actual1 = filter.apply(listOf(
                updatedApp(1, installedVersionCode = 0),
                updatedApp(2, installedVersionCode = 1),
                updatedApp(3, installedVersionCode = 0),
                updatedApp(4, installedVersionCode = 1)
        ))
        assertEquals(listOf(
                updatedApp(1, installedVersionCode = 0),
                updatedApp(3, installedVersionCode = 0)
        ), actual1)

        val actual2 = filter.apply(listOf(
                updatedApp(2, installedVersionCode = 1),
                updatedApp(4, installedVersionCode = 1)
        ))
        assertEquals(listOf<UpdatedApp>(), actual2)
    }

    @Test
    fun testFilterInstalledUpToDate() {
        val filter = SyncNotificationFilter(
                filterInstalled = false,
                filterInstalledUpToDate = true,
                filterNoChanges = false)
        val actual1 = filter.apply(listOf(
                updatedApp(1, installedVersionCode = 0),
                updatedApp(2, installedVersionCode = 2, versionNumber = 3),
                updatedApp(3, installedVersionCode = 0),
                updatedApp(4, installedVersionCode = 1, versionNumber = 1)
        ))
        assertEquals(listOf(
                updatedApp(1, installedVersionCode = 0),
                updatedApp(2, installedVersionCode = 2, versionNumber = 3),
                updatedApp(3, installedVersionCode = 0)
        ), actual1)
    }

    @Test
    fun testFilterNoChanges() {
        val filter = SyncNotificationFilter(
                filterInstalled = false,
                filterInstalledUpToDate = false,
                filterNoChanges = true)
        val actual = filter.apply(listOf(
                updatedApp(1, noNewDetails = false),
                updatedApp(2, noNewDetails = true),
                updatedApp(3, noNewDetails = false),
                updatedApp(4, noNewDetails = true)
        ))
        assertEquals(listOf(
                updatedApp(1),
                updatedApp(3)
        ), actual)
    }

    private fun updatedApp(id: Int, installedVersionCode: Int = 0, versionNumber: Int = 0, noNewDetails: Boolean = false) = UpdatedApp(
            packageName = "item-$id",
            versionNumber = versionNumber,
            title = "title-$id",
            uploadTime = 100,
            uploadDate = "100",
            recentChanges = "",
            installedVersionCode = installedVersionCode,
            isNewUpdate = false,
            noNewDetails = noNewDetails
    )
}