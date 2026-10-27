package com.anod.appwatcher.diagnostics

import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticFileStoreTest {
    private val root = File("build/diagnostic-tests/${UUID.randomUUID()}")
    private val logs = File(root, "logs")
    private val process = UUID.randomUUID()
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

    @Test
    fun unfinishedStartsSurviveProcessRestartWithoutClaimingACause() {
        val store = DiagnosticFileStore(logs)
        val first = UUID.randomUUID()
        val second = UUID.randomUUID()
        store.append(DiagnosticRecord(DiagnosticEvent.WORK_START, run = first).encode(now, process), now)
        store.append(DiagnosticRecord(DiagnosticEvent.WORK_START, run = second).encode(now + 1, process), now + 1)
        store.append(DiagnosticRecord(DiagnosticEvent.WORK_CANCELLED, run = first).encode(now + 2, process), now + 2)
        assertEquals(1, DiagnosticFileStore(logs).unfinishedRuns(now + 3, UUID.randomUUID()))
    }

    @Test
    fun unfinishedRunsExcludeTheCurrentLiveProcess() {
        val store = DiagnosticFileStore(logs)
        val previousProcess = UUID.randomUUID()
        val previousUnfinished = UUID.randomUUID()
        val previousCompleted = UUID.randomUUID()
        val live = UUID.randomUUID()
        store.append(DiagnosticRecord(DiagnosticEvent.WORK_START, run = previousUnfinished).encode(now, previousProcess), now)
        store.append(DiagnosticRecord(DiagnosticEvent.WORK_START, run = previousCompleted).encode(now + 1, previousProcess), now + 1)
        store.append(DiagnosticRecord(DiagnosticEvent.WORK_END, run = previousCompleted).encode(now + 2, previousProcess), now + 2)
        store.append(DiagnosticRecord(DiagnosticEvent.WORK_START, run = live).encode(now + 3, process), now + 3)
        assertEquals(1, store.unfinishedRuns(now + 4, process))
        assertEquals(2, store.unfinishedRuns(now + 4, UUID.randomUUID()))
    }

    @Test
    fun equalTimestampSegmentsKeepEventOrderAcrossRestart() {
        val run = UUID.randomUUID()
        val store = DiagnosticFileStore(logs, segmentBytes = 256)
        store.append(DiagnosticRecord(DiagnosticEvent.WORK_START, run = run).encode(now, process), now)
        repeat(5) { store.append(line(now), now) }
        store.append(DiagnosticRecord(DiagnosticEvent.WORK_END, run = run).encode(now, process), now)
        val reopened = DiagnosticFileStore(logs, segmentBytes = 256)
        assertEquals(0, reopened.unfinishedRuns(now, UUID.randomUUID()))
    }

    @Test(expected = IOException::class)
    fun invalidStorageLocationIsAnExplicitFailure() {
        root.mkdirs()
        logs.writeText("not a directory")
        DiagnosticFileStore(logs).append(line(now), now)
    }

    @Test
    fun recordsHaveOnlyAllowlistedValuesAndUtcCorrelation() {
        val run = UUID.randomUUID()
        val encoded = DiagnosticRecord(
            DiagnosticEvent.STAGE, run = run,
            stage = DiagnosticStage.fromInternalStage("bulk-details private-data"),
            code = 3, flag = false
        ).encode(now, process)
        assertTrue(encoded.contains(Instant.ofEpochMilli(now).toString()))
        assertTrue(encoded.contains(process.toString()))
        assertTrue(encoded.contains(run.toString()))
        assertTrue(encoded.contains("\"stage\":\"BULK_DETAILS\""))
        assertFalse(encoded.contains("private-data"))
        assertEquals(DiagnosticStage.OTHER, DiagnosticStage.fromInternalStage("credential-token"))
        assertFalse(DiagnosticRecord::class.java.declaredFields.any { it.type == String::class.java })
    }

    private fun line(time: Long): String = DiagnosticRecord(DiagnosticEvent.STAGE).encode(time, process)
}