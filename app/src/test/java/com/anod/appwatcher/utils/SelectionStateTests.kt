package com.anod.appwatcher.utils

import org.junit.Assert
import org.junit.Test

class SelectionStateTests {

    @Test
    fun testSelectKey() {
        val selection = SelectionState()
            .selectKey("A", true, mapOf("A" to "1"))
            .selectKey("B", false, mapOf("B" to "2"))

        Assert.assertEquals(SelectionState(
            extras = mapOf(
                "A" to  mapOf("A" to "1"),
                "B" to  mapOf("B" to "2")
            ),
            selectedKeys = mapOf("A" to true, "B" to false),
            selectedCount = 1
        ), selection)
    }

    @Test
    fun testSelectKeysTrue() {
        val selection = SelectionState()
            .selectKey("A", true, mapOf("A" to "1"))
            .selectKey("B", false, mapOf("B" to "2"))
            .selectKeys(listOf("B","C", "D"), true)

        Assert.assertEquals(SelectionState(
            extras = mapOf(
                "A" to  mapOf("A" to "1"),
                "B" to  mapOf("B" to "2")
            ),
            selectedKeys = mapOf("A" to true, "B" to true, "C"  to true, "D" to true),
            selectedCount = 4
        ), selection)
    }

    @Test
    fun testSelectKeysFalse() {
        val selection = SelectionState()
            .selectKey("A", true, mapOf("A" to "1"))
            .selectKey("B", false, mapOf("B" to "2"))
            .selectKeys(listOf("B","C", "D"), true)
            .selectKeys(listOf("A", "D"), false)

        Assert.assertEquals(SelectionState(
            extras = mapOf(
                "A" to  mapOf("A" to "1"),
                "B" to  mapOf("B" to "2")
            ),
            selectedKeys = mapOf("A" to false, "B" to true, "C"  to true, "D" to false),
            selectedCount = 2
        ), selection)
    }

    @Test
    fun testMergeExtras() {
        val selection = SelectionState()
            .selectKey("A", true, mapOf("A" to "1"))
            .selectKey("B", false, mapOf("B" to "2"))
            .selectKey("C", false)
            .mergeExtras(mapOf(
                "B" to mapOf("s" to "e"),
                "C" to mapOf("s" to "d")
            ))

        Assert.assertEquals(SelectionState(
            extras = mapOf(
                "A" to  mapOf("A" to "1"),
                "B" to  mapOf("B" to "2", "s" to "e"),
                "C" to  mapOf("s" to "d"),
            ),
            selectedKeys = mapOf("A" to true, "B" to false, "C" to false),
            selectedCount = 1
        ), selection)
    }

    @Test
    fun testFilterExtras() {
        val keys = SelectionState()
            .selectKey("A", true, mapOf("A" to "1"))
            .selectKey("B", false, mapOf("B" to "2"))
            .selectKey("C", false)
            .mergeExtras(mapOf(
                "B" to mapOf("s" to "e"),
                "C" to mapOf("s" to "d")
            ))
            .filterWithExtra { it["s"] == "d" }

        Assert.assertEquals(listOf("C"), keys)
    }
}