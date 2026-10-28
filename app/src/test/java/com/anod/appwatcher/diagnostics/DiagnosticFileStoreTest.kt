package com.anod.appwatcher.diagnostics

import java.io.File
import java.io.IOException
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticFileStoreTest {
    private val root = File("build/diagnostic-tests/${UUID.randomUUID()}")
    private val logs = File(root, "logs")
    private val now = 1_800_000_000_000L

    @After
    fun tearDown() {
        root.deleteRecursively()
    }

    @Test
    fun rotatesAndCapsOldestSegmentsAcrossRestart() {
        val store = DiagnosticFileStore(logs, maxBytes = 700, segmentBytes = 256)
        repeat(15) { index -> store.append(line(now + index), now + index) }
        val state = store.state(now + 15)
        assertTrue(state.bytes in 1..700)
        assertTrue(logs.listFiles()!!.size > 1)
        assertTrue(state.oldest!! > now)
        val reopened = DiagnosticFileStore(logs, maxBytes = 700, segmentBytes = 256)
        assertEquals(state, reopened.state(now + 15))
    }

    @Test
    fun capAlsoBoundsSegmentsWhenSmallerThanDefaultSegmentSize() {
        val store = DiagnosticFileStore(logs, maxBytes = 100)
        repeat(20) { store.append(line(now + it), now + it) }
        store.flush()
        assertTrue(logs.listFiles()!!.sumOf { it.length() } <= 100)
        store.close()
    }

    @Test
    fun ageRetentionAppliesWhenDisabledAndExporting() {
        val store = DiagnosticFileStore(logs)
        store.append(line(now), now)
        assertTrue(store.state(now).bytes > 0)
        val later = now + DiagnosticFileStore.RETENTION_MILLIS + 1
        val exported = store.snapshot(File(root, "exports/snapshot.jsonl"), later)
        assertEquals("", exported.readText())
        assertEquals(0L, store.state(later).bytes)
    }

    @Test
    fun snapshotIsStableAndDeleteDoesNotResurrectData() {
        val store = DiagnosticFileStore(logs)
        store.append(line(now), now)
        val snapshot = store.snapshot(File(root, "exports/snapshot.jsonl"), now)
        val content = snapshot.readText()
        store.append(line(now + 1), now + 1)
        assertEquals(content, snapshot.readText())
        store.delete()
        assertEquals(0L, store.state(now + 2).bytes)
        store.append(line(now + 3), now + 3)
        assertEquals(line(now + 3), store.snapshot(File(root, "exports/next.jsonl"), now + 3).readText())
    }

    @Test(expected = IOException::class)
    fun invalidStorageLocationIsAnExplicitFailure() {
        root.mkdirs()
        logs.writeText("not a directory")
        DiagnosticFileStore(logs).append(line(now), now)
    }

    private fun line(time: Long): String = "$time 4/AppWatcher: line\n"
}