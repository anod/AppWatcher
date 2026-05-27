package com.anod.appwatcher.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListDetailPaneContextTest {

    @Test
    fun detailsDoNotUpdateSystemBarsInsideListDetailPaneScaffold() {
        assertFalse(shouldUpdateDetailSystemBars(isInListDetailPaneScaffold = true))
    }

    @Test
    fun standaloneDetailsUpdateSystemBars() {
        assertTrue(shouldUpdateDetailSystemBars(isInListDetailPaneScaffold = false))
    }
}