package com.anod.appwatcher.backup.gdrive

import android.content.Context
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.database.AppsDatabase
import info.anodsplace.applog.AppLog
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.Reader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Transfers fail with their original exception; staging is owned until transfer/reading finishes.
 * Context-based staging uses non-cache, non-backed-up storage to avoid cache eviction.
 *
 * @author Alex Gavrishev
 * @date 26/06/2017
 */
class DriveIdFile(private val file: FileDescription, private val driveClient: DriveService, private val tempDir: File) {

    constructor(file: FileDescription, driveClient: DriveService, context: Context)
        : this(file, driveClient, File(context.noBackupFilesDir, "drive-staging"))

    interface FileDescription {
        val fileName: String
        val mimeType: String
    }

    private var driveId: String? = null

    suspend fun getId(): String? = withContext(Dispatchers.Main) {
        if (driveId != null) {
            return@withContext driveId
        }

        val list = driveClient.queryAppDataFiles(
            orderBy = "quotaBytesUsed desc",
            mimeType = file.mimeType,
            name = file.fileName,
            space = GDriveSpace.AppData
        )
        if (list.isEmpty() || list.files.isEmpty()) {
            AppLog.i("File not found " + file.fileName, "DriveIdFile")
            return@withContext null
        }
        driveId = list.files[0].id
        AppLog.i("Found $driveId", "DriveIdFile")
        return@withContext driveId
    }

    suspend fun create() = withContext(Dispatchers.Main) {
        AppLog.i("Create a new file", "DriveIdFile")

        driveId = driveClient.createFile(
            name = file.fileName,
            mimeType = file.mimeType,
            space = GDriveSpace.AppData)
    }

    suspend fun write(writer: DbJsonWriter, db: AppsDatabase): Long = withContext(Dispatchers.IO) {
        val driveId = requireId()

        AppLog.i("Write full list to a temp file", "DriveIdFile")
        withTempFile { tempFile ->
            FileWriter(tempFile).use { writer.write(it, db) }
            val bytes = tempFile.length()
            BufferedInputStream(FileInputStream(tempFile)).use { inputStream ->
                AppLog.i("Save temp file to drive", "DriveIdFile")
                driveClient.saveFile(driveId, file.mimeType, inputStream)
            }
            bytes
        }
    }

    /**
     * Keeps the reader and staging file owned by this operation, including on cancellation.
     */
    suspend fun <T> read(onRead: suspend (Reader) -> T): T = withContext(Dispatchers.IO) {
        val driveId = requireId()

        withTempFile { tempFile ->
            AppLog.d("[GDrive] Read into temp $tempFile")
            FileOutputStream(tempFile).use { driveClient.readFile(driveId, it) }
            FileReader(tempFile).use { onRead(it) }
        }
    }

    private suspend fun requireId(): String = withContext(Dispatchers.Main) {
        checkNotNull(driveId) { "Drive Id is not initialized" }
    }

    private suspend fun <T> withTempFile(block: suspend (File) -> T): T {
        val tempFile = synchronized(stagingLock) {
            if (!tempDir.mkdirs() && !tempDir.isDirectory) {
                throw IOException("Cannot create backup staging directory")
            }
            val files = tempDir.listFiles() ?: throw IOException("Cannot list backup staging directory")
            files.filter {
                it.isFile && it.name.startsWith(file.fileName) && it.name.endsWith(".json") && it !in activeFiles
            }.forEach { deleteTempFile(it) }
            File.createTempFile(file.fileName, ".json", tempDir).also { activeFiles.add(it) }
        }
        try {
            return block(tempFile)
        } finally {
            synchronized(stagingLock) {
                deleteTempFile(tempFile)
                activeFiles.remove(tempFile)
            }
        }
    }

    private fun deleteTempFile(tempFile: File) {
        if (!tempFile.delete() && tempFile.exists()) {
            AppLog.e("Cannot delete backup staging file", "DriveIdFile")
        }
    }

    companion object {
        // Sync and upload have separate locks, so staging ownership must be shared across instances.
        private val stagingLock = Any()
        private val activeFiles = mutableSetOf<File>()
    }
}