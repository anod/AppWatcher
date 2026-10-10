package com.anod.appwatcher.backup.gdrive

import android.accounts.Account
import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.anod.appwatcher.backup.DbJsonReader
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.preferences.Preferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.ConnectionFactory
import com.google.api.client.http.javanet.NetHttpTransport
import info.anodsplace.context.ApplicationContext
import info.anodsplace.framework.json.JsonWriter
import info.anodsplace.notification.NotificationManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.shadow.api.Shadow
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [36],
    application = Application::class,
    shadows = [BackupTransportShadow::class, BackupCredentialShadow::class, BackupJsonWriterShadow::class, BackupSignInShadow::class],
    instrumentedPackages = ["com.google.api.client", "info.anodsplace.framework.json", "com.google.android.gms.auth.api.signin"]
)
class GDriveFailureTest {

    private lateinit var context: Context
    private lateinit var db: AppsDatabase
    private val account = Account("backup-test@example.invalid", "com.google")

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        BackupTransportShadow.reset()
        BackupJsonWriterShadow.cacheDir = context.cacheDir
        BackupJsonWriterShadow.evictCache = false
        BackupJsonWriterShadow.otherEvictionDir = null
        BackupJsonWriterShadow.evictedFiles = 0
    }

    @After
    fun tearDown() {
        db.close()
        listOf(context.cacheDir, context.noBackupFilesDir, File(context.noBackupFilesDir, "drive-staging")).forEach { dir ->
            dir.listFiles()?.filter { it.name.startsWith(AppListFile.fileName) }?.forEach {
                assertTrue("Staging file could not be removed (possibly still open)", it.delete())
            }
        }
    }

    @Test
    fun networkDownloadPreservesOriginalException() = runBackup {
        val expected = ConnectException("test download connection failure")
        BackupTransportShadow.downloadFailure = expected
        val file = existingFile()

        assertNetworkFailure(expected, failure { file.read { } })
    }

    @Test
    fun syncDownloadPreservesNetworkCause() = runBackup {
        val expected = ConnectException("test download connection failure")
        BackupTransportShadow.downloadFailure = expected

        val error = failure { sync().doSync() }

        assertTrue(error is GDriveSync.SyncError)
        assertNetworkFailure(expected, error.cause!!)
    }

    @Test
    fun networkUploadPreservesOriginalException() = runBackup {
        val expected = ConnectException("test upload connection failure")
        BackupTransportShadow.uploadFailure = expected
        val file = existingFile()

        assertNetworkFailure(expected, failure { file.write(DbJsonWriter(), db) })
    }

    @Test
    fun networkUploadPreservesRecordsInBothCallers() = runBackup {
        insertDeletionRecords()
        val expected = ConnectException("test upload connection failure")
        BackupTransportShadow.uploadFailure = expected

        val syncError = failure { sync().doSync() }
        assertTrue(syncError is GDriveSync.SyncError)
        assertNetworkFailure(expected, syncError.cause!!)
        assertDeletionRecords()
        assertNetworkFailure(expected, failure { upload().doUploadInBackground() })
        assertDeletionRecords()
        assertNoStagingFiles()
    }

    @Test
    fun listingFailurePreservesCauseAndDeletionRecords() = runBackup {
        insertDeletionRecords()
        val expected = ConnectException("test listing connection failure")
        BackupTransportShadow.listFailure = expected

        assertNetworkFailure(expected, failure { sync().doSync() }.cause!!)
        assertNetworkFailure(expected, failure { upload().doUploadInBackground() })
        assertDeletionRecords()
        assertNoStagingFiles()
    }

    @Test
    fun quotaFailurePreservesDeletionRecordsInSync() = runBackup {
        assertQuotaFailurePreservesRecords(sync = true)
    }

    @Test
    fun quotaFailurePreservesDeletionRecordsInUpload() = runBackup {
        assertQuotaFailurePreservesRecords(sync = false)
    }

    @Test
    fun failedWorkerDoesNotReportSuccessOrAdvanceLastSyncTime() = runBackup {
        insertDeletionRecords()
        BackupTransportShadow.quotaExceeded = true
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val prefs = Preferences(context, NotificationManager.NoOp(), scope)
        prefs.lastDriveSyncTime = 123L
        startKoin {
            modules(module {
                single { prefs }
                factory { upload() }
            })
        }
        try {
            val result = TestListenableWorkerBuilder<UploadService>(context).build().doWork()

            assertEquals("Failed backup reported worker success", ListenableWorker.Result.failure(), result)
            assertEquals("Failed backup advanced last-success time", 123L, prefs.lastDriveSyncTime)
            assertDeletionRecords()
        } finally {
            stopKoin()
            scope.cancel()
        }
    }

    @Test
    fun absentRemoteWithOnlyDeletedAppsDoesNotAcknowledgeDeletion() = runBackup {
        insertDeletionRecords()
        BackupTransportShadow.remoteExists = false

        sync().doSync()

        assertDeletionRecords()
        assertEquals(null, BackupTransportShadow.uploaded)
        assertNoStagingFiles()
    }

    @Test
    fun absentRemoteCreatesAndUploadsNewBackup() = runBackup {
        insertDeletionRecords()
        val app = db.apps().loadPackages(true).single()
        db.apps().updateStatus(app.rowId, App.STATUS_NORMAL)
        BackupTransportShadow.remoteExists = false

        sync().doSync()

        assertNotNull(BackupTransportShadow.uploaded)
        assertEquals(1, db.apps().count(false))
        assertTrue(db.tags().loadDeletedIds().isEmpty())
        assertNoStagingFiles()
    }

    @Test
    fun cacheEvictionAfterSerializationDoesNotLoseUpload() = runBackup {
        BackupJsonWriterShadow.evictCache = true
        val file = existingFile()

        val bytes = file.write(DbJsonWriter(), db)

        assertTrue("Serialized cache file was lost before upload (${BackupJsonWriterShadow.evictedFiles} files evicted)", bytes > 0)
        assertNotNull(BackupTransportShadow.uploaded)
        assertEquals(0, BackupJsonWriterShadow.evictedFiles)
    }

    @Test
    fun missingStagingFileFailsUploadAndPreservesDeletionRecords() = runBackup {
        insertDeletionRecords()
        BackupJsonWriterShadow.evictCache = true
        BackupJsonWriterShadow.otherEvictionDir = File(context.noBackupFilesDir, "drive-staging")

        assertTrue(failure { upload().doUploadInBackground() } is FileNotFoundException)
        assertEquals(1, BackupJsonWriterShadow.evictedFiles)
        assertDeletionRecords()
        assertNoStagingFiles()
    }

    @Test
    fun stagingIsRemovedAfterSuccessfulUpload() = runBackup {
        existingFile().write(DbJsonWriter(), db)

        assertNoStagingFiles()
    }

    @Test
    fun abandonedSnapshotsArePurgedBeforeTransfer() = runBackup {
        val dir = File(context.noBackupFilesDir, "drive-staging")
        assertTrue(dir.mkdirs() || dir.isDirectory)
        val abandoned = File(dir, "${AppListFile.fileName}-abandoned.json")
        abandoned.writeText("""{"apps":[],"tags":[]}""")
        try {
            existingFile().write(DbJsonWriter(), db)

            assertTrue("Abandoned snapshot was retained", !abandoned.exists())
        } finally {
            abandoned.delete()
        }
    }

    @Test
    fun concurrentTransferDoesNotPurgeActiveReader() = runBackup {
        coroutineScope {
            val reading = CompletableDeferred<File>()
            val finishReading = CompletableDeferred<Unit>()
            val first = existingFile()
            val second = existingFile()
            val read = async {
                first.read { reader ->
                    val dir = File(context.noBackupFilesDir, "drive-staging")
                    val active = dir.listFiles().orEmpty().single()
                    reading.complete(active)
                    finishReading.await()
                    assertTrue("Concurrent transfer deleted active snapshot", active.exists())
                    assertEquals(emptyList<Tag>(), DbJsonReader().read(reader).tags)
                }
            }
            try {
                val active = reading.await()
                second.write(DbJsonWriter(), db)
                assertTrue("Concurrent cleanup deleted active snapshot", active.exists())
            } finally {
                finishReading.complete(Unit)
            }
            read.await()
        }
        assertNoStagingFiles()
    }

    @Test
    fun stagingIsRemovedAfterReaderCloses() = runBackup {
        existingFile().read { assertEquals(emptyList<Tag>(), DbJsonReader().read(it).tags) }

        assertNoStagingFiles()
    }

    @Test
    fun stagingIsRemovedAfterDownloadFailure() = runBackup {
        BackupTransportShadow.downloadFailure = ConnectException("test download connection failure")
        assertTrue(failure { existingFile().read { } } is ConnectException)

        assertNoStagingFiles()
    }

    @Test
    fun readerCancellationClosesStagingAndPropagates() = runBackup {
        val error = failure {
            existingFile().read { throw CancellationException("test reader cancellation") }
        }

        assertTrue(error is CancellationException)
        assertEquals("test reader cancellation", error.message)
        assertNoStagingFiles()
    }

    @Test
    fun downloadCancellationClosesStagingAndPropagates() = runBackup {
        BackupTransportShadow.downloadFailure = CancellationException("test download cancellation")

        val error = failure { sync().doSync() }
        assertTrue(error is CancellationException)
        assertEquals("test download cancellation", error.message)
        assertNoStagingFiles()
    }

    @Test
    fun cancellationPropagatesAndPreservesRecordsInSync() = runBackup {
        assertCancellationPreservesRecords(sync = true)
    }

    @Test
    fun cancellationPropagatesAndPreservesRecordsInUpload() = runBackup {
        assertCancellationPreservesRecords(sync = false)
    }

    @Test
    fun successfulRoundTripAndUploadCleanup() = runBackup {
        insertDeletionRecords()
        val tagId = db.tags().insert("Kept", Tag.DEFAULT_COLOR).toInt()
        val file = existingFile()
        val bytes = file.write(DbJsonWriter(), db)
        BackupTransportShadow.remoteJson = String(BackupTransportShadow.uploaded!!, Charsets.UTF_8)
        assertTrue("Unexpected upload payload: ${BackupTransportShadow.remoteJson.take(100)}",
            BackupTransportShadow.remoteJson.startsWith("{\"apps\":"))

        file.read { reader ->
            val restored = DbJsonReader().read(reader)
            assertEquals(listOf(tagId), restored.tags.map { it.id })
            assertEquals(listOf("deleted"), restored.apps.map { it.appId })
        }
        assertEquals(BackupTransportShadow.uploaded!!.size.toLong(), bytes)
        upload().doUploadInBackground()
        assertEquals(0, db.apps().count(true))
        assertTrue(db.appTags().load().isEmpty())
        assertTrue(db.tags().loadDeletedIds().isEmpty())
        insertDeletionRecords()
        sync().doSync()
        assertEquals(0, db.apps().count(true))
        assertTrue(db.appTags().load().isEmpty())
        assertTrue(db.tags().loadDeletedIds().isEmpty())
        assertNoStagingFiles()
    }

    private suspend fun assertQuotaFailurePreservesRecords(sync: Boolean) {
        insertDeletionRecords()
        BackupTransportShadow.quotaExceeded = true
        val error = try {
            if (sync) sync().doSync() else upload().doUploadInBackground()
            null
        } catch (e: Exception) {
            e
        }

        assertDeletionRecords()
        val cause = if (sync) error?.cause else error
        if (sync) assertEquals(null, (error as GDriveSync.SyncError).error)
        assertTrue("Drive quota failure must propagate, not return success", cause is GoogleJsonResponseException)
        assertEquals("storageQuotaExceeded", (cause as GoogleJsonResponseException).details.errors.single().reason)
        assertNoStagingFiles()
    }

    private suspend fun assertCancellationPreservesRecords(sync: Boolean) {
        insertDeletionRecords()
        val expected = CancellationException("test upload cancellation")
        BackupTransportShadow.uploadFailure = expected

        val error = failure {
            if (sync) sync().doSync() else upload().doUploadInBackground()
        }
        assertTrue(error is CancellationException)
        assertEquals(expected.message, error.message)
        assertDeletionRecords()
        assertNoStagingFiles()
    }

    private suspend fun insertDeletionRecords() {
        AppListTable.Queries.insert(
            App(
                rowId = 0, appId = "deleted", packageName = "deleted.package",
                versionNumber = 1, versionName = "1.0", title = "Deleted", creator = "test",
                iconUrl = "", status = App.STATUS_DELETED, uploadDate = "", price = Price("", "", 0),
                detailsUrl = null, uploadTime = 0, appType = "", syncTime = 0
            ),
            db
        )
        db.appTags().insert("deleted", 123)
        db.tags().insertDeleted("Deleted tag", Tag.DEFAULT_COLOR)
    }

    private suspend fun assertDeletionRecords() {
        assertEquals("Failed upload removed app tombstone", 1, db.apps().count(true))
        assertEquals("Failed upload removed app/tag link", 1, db.appTags().load().size)
        assertEquals(listOf("Deleted tag"), db.tags().loadDeletedNames())
    }

    private fun assertNoStagingFiles() {
        listOf(context.cacheDir, context.noBackupFilesDir, File(context.noBackupFilesDir, "drive-staging")).forEach { dir ->
            assertTrue("Leaked backup staging file in ${dir.name}",
                dir.listFiles().orEmpty().none { it.name.startsWith(AppListFile.fileName) })
        }
    }

    private suspend fun existingFile(): DriveIdFile {
        val service = DriveService(HttpRequestInitializer { }, "Backup test")
        return DriveIdFile(AppListFile, service, context).also { assertNotNull(it.getId()) }
    }

    private fun sync() = GDriveSync(account, ApplicationContext(context), db)

    private fun upload() = GDriveUpload(account, ApplicationContext(context), db)

    private fun assertNetworkFailure(expected: ConnectException, actual: Throwable) {
        assertTrue(actual is ConnectException)
        assertEquals(expected.message, actual.message)
        // Coroutine stack-trace recovery may copy exceptions, retaining the original as their cause.
        assertSame(expected, generateSequence(actual) { it.cause }.last())
    }

    private suspend fun failure(block: suspend () -> Unit): Throwable {
        try {
            block()
        } catch (e: Throwable) {
            return e
        }
        fail("Expected backup failure but operation returned successfully")
        error("unreachable")
    }

    private fun runBackup(block: suspend () -> Unit) {
        val task = FutureTask { runBlocking { block() } }
        val thread = Thread(task, "backup-regression").apply { isDaemon = true }
        thread.start()
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30)
        while (!task.isDone && System.nanoTime() < deadline) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            thread.join(10)
        }
        try {
            task.get(1, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            throw e.cause!!
        } finally {
            task.cancel(true)
        }
    }
}

@Implements(NetHttpTransport::class, isInAndroidSdk = false)
class BackupTransportShadow {
    @RealObject
    private lateinit var transport: NetHttpTransport

    @Implementation
    @Suppress("FunctionName")
    fun __constructor__() {
        Shadow.invokeConstructor(NetHttpTransport::class.java, transport)
        ReflectionHelpers.setField(transport, "connectionFactory", ConnectionFactory { url -> connection(url) })
    }

    companion object {
        var downloadFailure: Exception? = null
        var uploadFailure: Exception? = null
        var listFailure: Exception? = null
        var quotaExceeded = false
        var remoteExists = true
        var uploaded: ByteArray? = null
        var remoteJson = """{"apps":[],"tags":[]}"""

        fun reset() {
            downloadFailure = null
            uploadFailure = null
            listFailure = null
            quotaExceeded = false
            remoteExists = true
            uploaded = null
            remoteJson = """{"apps":[],"tags":[]}"""
        }

        private fun connection(url: URL): HttpURLConnection = object : HttpURLConnection(url) {
            private val output = ByteArrayOutputStream()
            private val isUpload = url.path.contains("/upload/") || url.path.contains("upload-session")
            private val isDownload = url.query?.contains("alt=media") == true
            private val isInitiation = url.query?.contains("uploadType=resumable") == true
            private val body: String
                get() = when {
                    isDownload -> remoteJson
                    isUpload && quotaExceeded ->
                        """{"error":{"code":403,"message":"Test quota exceeded","errors":[{"domain":"usageLimits","reason":"storageQuotaExceeded","message":"Test quota exceeded"}]}}"""
                    isUpload && isInitiation -> ""
                    isUpload -> """{"id":"test-file"}"""
                    requestMethod == "POST" -> """{"id":"test-file"}"""
                    else -> if (remoteExists) """{"files":[{"id":"test-file"}]}""" else """{"files":[]}"""
                }
            private val headers: Map<String, List<String>>
                get() = buildMap {
                    put("Content-Type", listOf("application/json; charset=UTF-8"))
                    put("Content-Length", listOf(body.toByteArray().size.toString()))
                    if (isInitiation) {
                        put("Location", listOf("https://backup-test.invalid/upload-session"))
                    }
                }

            override fun connect() {
                if (isDownload) downloadFailure?.let { throw it }
                if (isUpload) uploadFailure?.let { throw it }
                if (!isDownload && !isUpload) listFailure?.let { throw it }
                if (isUpload && !isInitiation) {
                    val bytes = output.toByteArray()
                    uploaded = if (bytes.size >= 2 && bytes[0] == 0x1f.toByte() && bytes[1] == 0x8b.toByte()) {
                        GZIPInputStream(ByteArrayInputStream(bytes)).use { it.readBytes() }
                    } else {
                        bytes
                    }
                }
            }
            override fun disconnect() = Unit
            override fun usingProxy(): Boolean = false
            override fun getOutputStream(): ByteArrayOutputStream = output
            override fun getInputStream(): InputStream = ByteArrayInputStream(body.toByteArray())
            override fun getErrorStream(): InputStream? = if (isUpload && quotaExceeded) inputStream else null
            override fun getResponseCode(): Int = if (isUpload && quotaExceeded) 403 else 200
            override fun getResponseMessage(): String = if (responseCode == 200) "OK" else "Forbidden"
            override fun getHeaderFields(): Map<String, List<String>> = headers
            override fun getHeaderField(name: String): String? = headers.entries.firstOrNull { it.key.equals(name, true) }?.value?.first()
            override fun getHeaderField(index: Int): String? = if (index == 0) "HTTP/1.1 $responseCode" else headers.values.elementAtOrNull(index - 1)?.first()
            override fun getHeaderFieldKey(index: Int): String? = headers.keys.elementAtOrNull(index - 1)
        }
    }
}

@Implements(GoogleAccountCredential::class, isInAndroidSdk = false)
class BackupCredentialShadow {
    @Implementation
    fun initialize(request: HttpRequest) = Unit
}

@Implements(GoogleSignIn::class, isInAndroidSdk = false)
class BackupSignInShadow {
    companion object {
        @JvmStatic
        @Implementation
        fun getLastSignedInAccount(context: Context): GoogleSignInAccount = GoogleSignInAccount.fromAccount(
            Account("backup-test@example.invalid", "com.google")
        )
    }
}

@Implements(JsonWriter::class, isInAndroidSdk = false)
class BackupJsonWriterShadow {
    @RealObject
    private lateinit var writer: JsonWriter

    @Implementation
    fun close() {
        Shadow.directlyOn<Unit, JsonWriter>(writer, JsonWriter::class.java, "close")
        if (evictCache) {
            listOfNotNull(cacheDir, otherEvictionDir).forEach { dir ->
                dir.listFiles()?.filter { it.name.startsWith(AppListFile.fileName) }?.forEach {
                    assertTrue("Could not delete closed staging file", it.delete())
                    evictedFiles++
                }
            }
        }
    }

    companion object {
        lateinit var cacheDir: File
        var evictCache = false
        var otherEvictionDir: File? = null
        var evictedFiles = 0
    }
}