package com.anod.appwatcher.diagnostics

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import androidx.core.content.FileProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.sync.SyncScheduler
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiagnosticRecorder(
    private val context: Context,
    val consent: DiagnosticConsent,
    scope: CoroutineScope,
    private val workInfos: (String) -> List<WorkInfo> = { name ->
        WorkManager.getInstance(context).getWorkInfosForUniqueWork(name).get(5, TimeUnit.SECONDS)
    }
) {
    private sealed interface Command {
        data class Record(val record: DiagnosticRecord, val time: Long, val permit: Long) : Command
        class Operation(val action: suspend () -> Unit, val result: CompletableDeferred<Unit>) : Command
    }

    private val process = UUID.randomUUID()
    private val store by lazy { DiagnosticFileStore(File(context.noBackupFilesDir, "diagnostics")) }
    private val exports by lazy { File(context.cacheDir, "diagnostic-exports") }
    private val commands = Channel<Command>(128)
    private val stopped = AtomicBoolean()
    private val pendingResults: MutableSet<CompletableDeferred<Unit>> = ConcurrentHashMap.newKeySet()
    private val dropped = AtomicLong()
    private val failures = AtomicLong()
    private val mutableState = MutableStateFlow(DiagnosticStorageState())
    val state = mutableState.asStateFlow()
    private var lastHealthDropped = 0L
    private var lastHealthFailures = 0L

    init {
        scope.launch(Dispatchers.IO) {
            for (command in commands) process(command)
        }.invokeOnCompletion {
            stopped.set(true)
            drainPending()
        }
    }

    private suspend fun process(command: Command) {
        try {
            when (command) {
                is Command.Record -> consent.withPermit(command.permit) {
                    store.append(command.record.encode(command.time, process), command.time)
                    val lost = dropped.get()
                    val failed = failures.get()
                    if (lost != lastHealthDropped || failed != lastHealthFailures) {
                        store.append(DiagnosticRecord(
                            DiagnosticEvent.RECORDER_HEALTH, count = lost, secondaryCount = failed
                        ).encode(command.time, process), command.time)
                        lastHealthDropped = lost
                        lastHealthFailures = failed
                    }
                }
                is Command.Operation -> command.action()
            }
            val now = System.currentTimeMillis()
            exportFiles().filter { it.lastModified() < now - 24L * 60 * 60 * 1000 }.forEach {
                if (!it.delete()) throw IOException("diagnostic expired export delete")
            }
            mutableState.value = store.state(now).copy(
                dropped = dropped.get(), writeFailures = failures.get()
            )
            if (command is Command.Operation) command.result.complete(Unit)
        } catch (e: CancellationException) {
            // A cancelled Future or similar is an operation failure; only the writer scope ending stops the loop.
            try {
                currentCoroutineContext().ensureActive()
            } catch (stop: CancellationException) {
                if (command is Command.Operation) command.result.completeExceptionally(IOException("Diagnostic recorder stopped"))
                throw stop
            }
            fail(command)
        } catch (_: Exception) {
            fail(command)
        }
    }

    private fun fail(command: Command) {
        failures.incrementAndGet()
        mutableState.value = mutableState.value.copy(
            dropped = dropped.get(), writeFailures = failures.get(), error = true
        )
        if (command is Command.Operation) command.result.completeExceptionally(IOException("Diagnostic storage operation failed"))
    }

    private fun drainPending() {
        pendingResults.toList().forEach { it.completeExceptionally(IOException("Diagnostic recorder stopped")) }
    }

    // Diagnostics are optional: internal failures surface through state and never reach the caller.
    fun record(record: DiagnosticRecord) {
        try {
            val permit = consent.permit() ?: return
            if (!commands.trySend(Command.Record(record, System.currentTimeMillis(), permit)).isSuccess) {
                val count = dropped.incrementAndGet()
                mutableState.value = mutableState.value.copy(dropped = count)
            }
        } catch (_: Exception) {
            val count = failures.incrementAndGet()
            mutableState.value = mutableState.value.copy(writeFailures = count, error = true)
        }
    }

    fun reportError() {
        mutableState.value = mutableState.value.copy(error = true)
    }

    suspend fun refresh() = operation { }

    suspend fun setEnabled(enabled: Boolean) {
        // Revocation is synchronous: do not wait behind queued disk writes.
        consent.setEnabled(enabled)
        refresh()
        if (consent.enabled) captureProcessState()
    }

    suspend fun delete() {
        // Invalidate queued entries so deleting cannot resurrect old buffered records.
        consent.discardQueued()
        operation {
            store.delete()
            exportFiles().forEach { if (!it.delete()) throw IOException("diagnostic export delete") }
            dropped.set(0)
            failures.set(0)
            lastHealthDropped = 0
            lastHealthFailures = 0
        }
    }

    suspend fun shareIntent(): Intent {
        val snapshot = snapshot()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.diagnostics", snapshot)
        return diagnosticShareIntent(uri)
    }

    internal suspend fun snapshot(): File {
        var snapshot: File? = null
        operation {
            val now = System.currentTimeMillis()
            exportFiles().sortedByDescending { it.lastModified() }.drop(2).forEach {
                if (!it.delete()) throw IOException("diagnostic export cleanup")
            }
            snapshot = store.snapshot(File(exports, "appwatcher-diagnostics-$now-${UUID.randomUUID()}.jsonl"), now)
            var totalBytes = exportFiles().sumOf { it.length() }
            exportFiles().filter { it != snapshot }.sortedBy { it.lastModified() }.forEach {
                if (totalBytes > DiagnosticFileStore.MAX_BYTES) {
                    val bytes = it.length()
                    if (!it.delete()) throw IOException("diagnostic export size cleanup")
                    totalBytes -= bytes
                }
            }
        }
        return checkNotNull(snapshot)
    }

    suspend fun captureProcessState() {
        if (!consent.enabled) {
            refresh()
            return
        }
        operation {
            val now = System.currentTimeMillis()
            val restrictions = metadata(1) {
                context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName) to
                    context.getSystemService(ActivityManager::class.java).isBackgroundRestricted
            }
            record(DiagnosticRecord(
                DiagnosticEvent.PROCESS_START,
                count = store.unfinishedRuns(now, process).toLong(),
                secondaryCount = BuildConfig.VERSION_CODE.toLong(),
                code = android.os.Build.VERSION.SDK_INT,
                flag = restrictions?.first,
                secondaryFlag = restrictions?.second
            ))
            metadata(2) {
                val power = context.getSystemService(PowerManager::class.java)
                record(DiagnosticRecord(
                    DiagnosticEvent.POWER_STATE, flag = power.isPowerSaveMode, secondaryFlag = power.isDeviceIdleMode
                ))
            }
            metadata(3) {
                context.getSystemService(ActivityManager::class.java).getHistoricalProcessExitReasons(null, 0, 3).forEach { exit ->
                    record(DiagnosticRecord(
                        DiagnosticEvent.PROCESS_EXIT, code = exit.reason,
                        count = exit.importance.toLong(), historicalTime = exit.timestamp
                    ))
                }
            }
            listOf(SyncScheduler.TAG, SyncScheduler.TAG_MANUAL).forEachIndexed { index, name ->
                metadata(4 + index) {
                    workInfos(name).forEach { work ->
                        record(DiagnosticRecord(
                            DiagnosticEvent.WORK_SNAPSHOT, run = work.id, code = work.state.ordinal,
                            attempt = work.runAttemptCount, flag = index == 1
                        ))
                    }
                }
            }
        }
    }

    fun notification(record: DiagnosticRecord, channel: String) {
        if (!consent.enabled) return
        try {
            val manager = context.getSystemService(NotificationManager::class.java)
            record(record.copy(
                flag = manager.areNotificationsEnabled(),
                secondaryFlag = manager.getNotificationChannel(channel)?.let { it.importance != NotificationManager.IMPORTANCE_NONE }
            ))
        } catch (_: Exception) {
            reportError()
        }
    }

    private suspend fun <T> metadata(source: Int, action: () -> T): T? = try {
        action()
    } catch (e: Exception) {
        // A cancelled Future is unavailable metadata unless the writer itself is cancelled.
        if (e is CancellationException) currentCoroutineContext().ensureActive()
        record(DiagnosticRecord(DiagnosticEvent.SNAPSHOT_UNAVAILABLE, code = source))
        null
    }

    private fun exportFiles(): List<File> {
        if (!exports.exists()) return emptyList()
        return (exports.listFiles() ?: throw IOException("diagnostic export list")).toList()
    }

    internal suspend fun operation(action: suspend () -> Unit) {
        if (stopped.get()) throw IOException("Diagnostic recorder stopped")
        val result = CompletableDeferred<Unit>()
        pendingResults.add(result)
        try {
            commands.send(Command.Operation(action, result))
            // The writer may have stopped after the send; nothing will consume this command.
            if (stopped.get()) drainPending()
            result.await()
        } finally {
            pendingResults.remove(result)
        }
    }
}

internal fun diagnosticShareIntent(uri: Uri): Intent = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
    type = "application/x-ndjson"
    putExtra(Intent.EXTRA_STREAM, uri)
    clipData = ClipData.newRawUri("AppWatcher diagnostics", uri)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}, null)