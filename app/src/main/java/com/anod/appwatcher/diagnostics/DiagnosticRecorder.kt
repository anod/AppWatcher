package com.anod.appwatcher.diagnostics

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import androidx.core.content.FileProvider
import androidx.work.WorkInfo
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.sync.SyncScheduler
import info.anodsplace.applog.AppLog
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

class DiagnosticRecorder(
    private val context: Context,
    private val prefs: Preferences,
    private val scope: CoroutineScope,
    private val workInfos: WorkInfoSource
) {
    private sealed interface Command {
        data class Line(val text: String, val time: Long, val account: String?, val generation: Long) : Command
        class Operation(val action: suspend () -> Unit, val result: CompletableDeferred<Unit>) : Command
    }

    private val generation = AtomicLong()
    private var logger: DiagnosticLogger? = null
    private val store by lazy {
        DiagnosticFileStore(
            directory = File(context.noBackupFilesDir, "diagnostics"),
            maxBytes = DiagnosticFileStore.MAX_BYTES,
            segmentBytes = 256L * 1024,
            retentionMillis = DiagnosticFileStore.RETENTION_MILLIS
        )
    }
    private val exports by lazy { File(context.cacheDir, "diagnostic-exports") }
    private val commands = Channel<Command>(128)
    private val stopped = AtomicBoolean()
    private val stoppedSignal = CompletableDeferred<Unit>()
    private val pendingResults: MutableSet<CompletableDeferred<Unit>> = ConcurrentHashMap.newKeySet()
    private val dropped = AtomicLong()
    private val failures = AtomicLong()
    private val mutableState = MutableStateFlow(DiagnosticStorageState())
    val state = mutableState.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            try {
                purgeLeftovers()
                for (command in commands) {
                    val failuresBeforeBatch = failures.get()
                    process(command)
                    var next = commands.tryReceive().getOrNull()
                    while (next != null) {
                        process(next)
                        next = commands.tryReceive().getOrNull()
                    }
                    try {
                        store.flush()
                        val storage = store.state(System.currentTimeMillis())
                        mutableState.value = storage.copy(
                            dropped = dropped.get(),
                            writeFailures = failures.get(),
                            error = failures.get() > failuresBeforeBatch
                        )
                    } catch (_: Exception) {
                        fail(command)
                    }
                }
            } finally {
                runCatching { store.close() }
            }
        }.invokeOnCompletion {
            stopped.set(true)
            stoppedSignal.complete(Unit)
            commands.close()
            synchronized(this) {
                logger?.let { if (AppLog.logger === it) AppLog.logger = it.delegate }
                logger = null
            }
            drainPending()
        }
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            prefs.changes.collect { if (updateLogger()) scope.launch { safely(Unit) { purge() } } }
        }
        updateLogger()
    }

    /** Returns true when this call stopped an installed tee. */
    @Synchronized
    private fun updateLogger(): Boolean {
        if (prefs.isDiagnosticsEnabled && !stopped.get()) {
            if (logger == null) {
                val tee = DiagnosticLogger(AppLog.logger, ::offer)
                logger = tee
                AppLog.logger = tee
                scope.launch(Dispatchers.IO) { captureProcessState() }
            }
            return false
        }
        val tee = logger ?: return false
        generation.incrementAndGet()
        if (AppLog.logger === tee) AppLog.logger = tee.delegate
        logger = null
        return true
    }

    private suspend fun process(command: Command) {
        try {
            when (command) {
                is Command.Line -> {
                    if (!prefs.isDiagnosticsEnabled || command.account != prefs.account?.name || command.generation != generation.get()) return
                    store.append("${Instant.ofEpochMilli(command.time)} ${command.text}\n", command.time)
                }
                is Command.Operation -> command.action()
            }
            if (command is Command.Operation) {
                val now = System.currentTimeMillis()
                exportFiles().filter { it.lastModified() < now - 24L * 60 * 60 * 1000 }.forEach {
                    if (!it.delete()) throw IOException("diagnostic expired export delete")
                }
                mutableState.value = store.state(now).copy(dropped = dropped.get(), writeFailures = failures.get())
                command.result.complete(Unit)
            }
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

    private fun offer(priority: Int, tag: String, message: String) {
        val command = Command.Line(
            "$priority/${tag.take(128)}: ${message.take(16_384)}", System.currentTimeMillis(), prefs.account?.name, generation.get()
        )
        if (!commands.trySend(command).isSuccess) dropped.incrementAndGet()
    }

    private fun reportError() {
        mutableState.value = mutableState.value.copy(error = true)
    }

    internal suspend fun refresh() = operation { }

    suspend fun setEnabled(enabled: Boolean): Boolean = safely(false) {
        generation.incrementAndGet()
        prefs.isDiagnosticsEnabled = enabled
        updateLogger()
        if (enabled) refresh() else purge()
        true
    }

    /** Stored logs only exist while recording is enabled, so every stop deletes them. */
    private suspend fun purge() = operation { purgeFiles() }

    private fun purgeLeftovers() {
        if (prefs.isDiagnosticsEnabled) return
        try {
            purgeFiles()
        } catch (_: Exception) {
            reportError()
        }
    }

    private fun purgeFiles() {
        store.delete()
        exportFiles().forEach { if (!it.delete()) throw IOException("diagnostic export delete") }
        dropped.set(0)
        failures.set(0)
    }

    suspend fun shareIntent(): Intent? = safely(null) {
        val snapshot = snapshot()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.diagnostics", snapshot)
        diagnosticShareIntent(uri)
    }

    /** Up to [limit] of the newest stored lines, oldest first, or null when storage fails. */
    suspend fun recentLines(limit: Int): List<String>? = safely(null) {
        var lines = emptyList<String>()
        operation { lines = store.tail(limit, System.currentTimeMillis()) }
        lines
    }

    private suspend fun <T> safely(failure: T, action: suspend () -> T): T = try {
        action()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        reportError()
        failure
    }

    internal suspend fun snapshot(): File {
        var snapshot: File? = null
        operation {
            val now = System.currentTimeMillis()
            exportFiles().sortedByDescending { it.lastModified() }.drop(2).forEach {
                if (!it.delete()) throw IOException("diagnostic export cleanup")
            }
            snapshot = store.snapshot(File(exports, "appwatcher-diagnostics-$now-${UUID.randomUUID()}.log"), now)
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

    internal suspend fun captureProcessState(): Boolean = safely(false) {
        if (!prefs.isDiagnosticsEnabled) return@safely true
        metadata {
            val power = context.getSystemService(PowerManager::class.java)
            val activity = context.getSystemService(ActivityManager::class.java)
            AppLog.i("Diagnostics started versionCode=${BuildConfig.VERSION_CODE}, sdk=${android.os.Build.VERSION.SDK_INT}", "Diagnostics")
            AppLog.i("Battery optimization ignored=${power.isIgnoringBatteryOptimizations(context.packageName)}, " +
                "background restricted=${activity.isBackgroundRestricted}, power save=${power.isPowerSaveMode}, idle=${power.isDeviceIdleMode}", "Diagnostics")
        }
        metadata {
            AppLog.i("Standby bucket=${context.getSystemService(UsageStatsManager::class.java).appStandbyBucket}", "Diagnostics")
        }
        metadata {
            context.getSystemService(ActivityManager::class.java).getHistoricalProcessExitReasons(null, 0, 3).forEach { exit ->
                AppLog.i("Process exit reason=${exit.reason}, importance=${exit.importance}, timestamp=${exit.timestamp}", "Diagnostics")
            }
        }
        listOf(SyncScheduler.TAG, SyncScheduler.TAG_MANUAL).forEach { name ->
            metadata {
                val work = workInfos(name)
                AppLog.i("Work $name: ${work.joinToString { "${it.id} ${it.state} attempt=${it.runAttemptCount}" }}", "Diagnostics")
            }
        }
        true
    }

    private suspend fun metadata(action: suspend () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            if (e is CancellationException) currentCoroutineContext().ensureActive()
        }
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
            try {
                select<Unit> {
                    commands.onSend(Command.Operation(action, result)) { }
                    stoppedSignal.onAwait { throw IOException("Diagnostic recorder stopped") }
                }
            } catch (e: ClosedSendChannelException) {
                throw IOException("Diagnostic recorder stopped", e)
            }
            // The writer may have stopped after the send; nothing will consume this command.
            if (stopped.get()) drainPending()
            result.await()
        } finally {
            pendingResults.remove(result)
        }
    }
}

fun interface WorkInfoSource {
    suspend operator fun invoke(uniqueWorkName: String): List<WorkInfo>
}

internal fun diagnosticShareIntent(uri: Uri): Intent = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_STREAM, uri)
    clipData = ClipData.newRawUri("AppWatcher diagnostics", uri)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}, null)