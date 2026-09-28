package com.anod.appwatcher.installed

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageRemovedReceiverTest {

    @Test
    fun applicationPackageChangeIsRelevant() {
        assertTrue(
            isApplicationPackageChange(
                packageName = "com.example",
                changedComponents = arrayOf("com.example")
            )
        )
    }

    @Test
    fun componentOnlyPackageChangeIsIgnored() {
        assertFalse(
            isApplicationPackageChange(
                packageName = "com.example",
                changedComponents = arrayOf("com.example.SyncReceiver")
            )
        )
    }

    @Test
    fun packageChangeWithoutComponentListIsRelevant() {
        assertTrue(
            isApplicationPackageChange(
                packageName = "com.example",
                changedComponents = null
            )
        )
    }
}