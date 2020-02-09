package com.anod.appwatcher.backup.gdrive

import android.content.Context
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.database.AppsDatabase
import info.anodsplace.framework.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

/**
 * @author Alex Gavrishev
 * @date 26/06/2017
 */
class DriveIdFile(
        private val file: FileDescription,
        private val driveClient: DriveService,
        private val tempDir: File) {

    constructor(file: FileDescription, driveClient: DriveService, context: Context)
            : this(file, driveClient, context.cacheDir)

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
                space = AppData
        )
        if (list.isEmpty()) {
            AppLog.d("[GDrive] File NOT found " + file.fileName)
            return@withContext null
        }
        driveId = list.files[0].id
        return@withContext driveId
    }

    suspend fun create() = withContext(Dispatchers.Main) {
        AppLog.d("[GDrive] Create new file ")

        driveId = driveClient.createFile(
                name = file.fileName,
                mimeType = file.mimeType,
                space = AppData)
    }

    suspend fun write(writer: DbJsonWriter, db: AppsDatabase): Long = withContext(Dispatchers.IO) {
        var bytes = 0L
        val driveId = withContext(Dispatchers.Main) {
            if (driveId == null) {
                AppLog.e("Drive Id is not initialized", "GDrive")
            }
            driveId
        } ?: return@withContext bytes

        try {
            AppLog.d("[GDrive] Write full list to temp ")
            val tempFile = File.createTempFile(file.fileName, ".json", tempDir)
            val file = FileWriter(tempFile)
            writer.write(file, db)
            val inputStream = BufferedInputStream(FileInputStream(tempFile))
            driveClient.saveFile(driveId, "application/json", inputStream)
            bytes = tempFile.length()
        } catch (e: IOException) {
            AppLog.e(e)
        }
        return@withContext bytes
    }

    suspend fun read(): Reader? = withContext(Dispatchers.IO) {
        val driveId = withContext(Dispatchers.Main) {
            if (driveId == null) {
                AppLog.e("Drive Id is not initialized", "GDrive")
            }
            driveId
        } ?: return@withContext null

        try {
            val tempFile = File.createTempFile(file.fileName, ".json", tempDir)
            AppLog.d("[GDrive] Read into temp $tempFile")
            val fileStream = FileOutputStream(tempFile)
            driveClient.readFile(driveId, fileStream)
            return@withContext FileReader(tempFile)
        } catch (e: IOException) {
            AppLog.e(e)
        }
        return@withContext null
    }
}