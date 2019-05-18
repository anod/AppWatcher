package com.anod.appwatcher.backup.gdrive

import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.database.AppsDatabase
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.DriveResourceClient
import com.google.android.gms.drive.MetadataChangeSet
import com.google.android.gms.drive.query.*
import com.google.android.gms.tasks.Tasks
import info.anodsplace.framework.AppLog
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter

/**
 * @author Alex Gavrishev
 * @date 26/06/2017
 */
class DriveIdFile(private val file: FileDescription, private val driveClient: DriveResourceClient) {

    interface FileDescription {
        val fileName: String
        val mimeType: String
    }

    private var _driveId: DriveId? = null
    val driveId: DriveId?
        get() {
            if (_driveId == null) {
                _driveId = retrieve()
            }
            return _driveId
        }

    private fun retrieve(): DriveId? {

        val order = SortOrder.Builder()
                .addSortDescending(SortableField.QUOTA_USED)
                .build()

        val query = Query.Builder()
                .addFilter(Filters.and(
                        Filters.eq(SearchableField.MIME_TYPE, file.mimeType),
                        Filters.eq(SearchableField.TITLE, file.fileName)
                ))
                .setSortOrder(order)
                .build()


        val appFolder = Tasks.await(driveClient.appFolder)
        val metadataBuffer = Tasks.await(driveClient.queryChildren(appFolder, query))
        if (metadataBuffer.count == 0) {
            AppLog.d("[GDrive] File NOT found " + file.fileName)
            return null
        } else {
            val metadata = metadataBuffer.get(0)
            AppLog.d("[GDrive] File found " + file.fileName)
            return metadata.driveId
        }

    }

    fun create() {
        val appFolder = Tasks.await(driveClient.appFolder)
        val driveContents = Tasks.await(driveClient.createContents())
        AppLog.d("[GDrive] Create new file ")

        val changeSet = MetadataChangeSet.Builder()
                .setTitle(file.fileName)
                .setMimeType(file.mimeType)
                .build()

        val driveFile = Tasks.await(driveClient.createFile(appFolder, changeSet, driveContents))
        _driveId = driveFile.driveId
    }

    internal suspend fun write(writer: DbJsonWriter, db: AppsDatabase) {

        if (this._driveId == null)
        {
            AppLog.e("[GDrive] Drive Id is not initialized")
        }

        val driveId = this._driveId ?: return

        AppLog.d("[GDrive] Write full list to remote ")

        val target = driveId.asDriveFile()

        val driveContents = Tasks.await(driveClient.openFile(target, DriveFile.MODE_WRITE_ONLY))

        val outputStream = driveContents.outputStream
        val outWriter = BufferedWriter(OutputStreamWriter(outputStream))
        try {
            writer.write(outWriter, db)
        } catch (e: IOException) {
            AppLog.e(e)
        } finally {
            try {
                outputStream.close()
            } catch (e: IOException) {
                AppLog.e(e)
            }

        }

        Tasks.await(driveClient.commitContents(driveContents, null))
    }
}