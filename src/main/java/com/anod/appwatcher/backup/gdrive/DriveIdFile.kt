package com.anod.appwatcher.backup.gdrive

import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.content.DbContentProviderClient
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.MetadataChangeSet
import com.google.android.gms.drive.query.*
import info.anodsplace.android.log.AppLog
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter

/**
 * @author algavris
 * @date 26/06/2017
 */
class DriveIdFile(private val file: FileDescription, private val googleApiClient: GoogleApiClient) {

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

    @Throws(Exception::class)
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

        val metadataBufferResult = Drive.DriveApi
                .getAppFolder(googleApiClient)
                .queryChildren(googleApiClient, query)
                .await()

        if (!metadataBufferResult.status.isSuccess) {
            throw Exception("Problem retrieving " + file.fileName + " : " + metadataBufferResult.status.statusMessage)
        }
        val metadataList = metadataBufferResult.metadataBuffer
        if (metadataList.count == 0) {
            AppLog.d("[GDrive] File NOT found " + file.fileName)
            return null
        } else {
            val metadata = metadataList.get(0)
            AppLog.d("[GDrive] File found " + file.fileName)
            return metadata.driveId
        }
    }

    @Throws(Exception::class)
    fun create() {
        val contentsResult = Drive.DriveApi.newDriveContents(googleApiClient).await()
        AppLog.d("[GDrive] Create new file ")

        if (!contentsResult.status.isSuccess) {
            throw Exception("[Google Drive] File create request filed: " + contentsResult.status.statusMessage!!)
        }
        val changeSet = MetadataChangeSet.Builder()
                .setTitle(file.fileName)
                .setMimeType(file.mimeType)
                .build()

        val driveFileResult = Drive.DriveApi
                .getAppFolder(googleApiClient)
                .createFile(googleApiClient, changeSet, contentsResult.driveContents).await()

        if (!driveFileResult.status.isSuccess) {
            throw Exception("[Google Drive] File create result filed: " + driveFileResult.status.statusMessage!!)
        }
        _driveId = driveFileResult.driveFile.driveId
    }

    @Throws(Exception::class)
    internal fun write(writer: DbJsonWriter, cr: DbContentProviderClient) {

        if (this._driveId == null)
        {
            AppLog.e("[GDrive] Drive Id is not initialized")
        }

        val driveId = this._driveId ?: return

        AppLog.d("[GDrive] Write full list to remote ")

        val target = driveId.asDriveFile()

        val contentsResult = target.open(googleApiClient, DriveFile.MODE_WRITE_ONLY, null).await()
        if (!contentsResult.status.isSuccess) {
            throw Exception("Error open file for write : " + contentsResult.status.statusMessage!!)
        }

        val contents = contentsResult.driveContents
        val outputStream = contents.outputStream
        val outWriter = BufferedWriter(OutputStreamWriter(outputStream))
        try {
            writer.write(outWriter, cr)
        } catch (e: IOException) {
            AppLog.e(e)
        } finally {
            try {
                outputStream.close()
            } catch (e: IOException) {
                AppLog.e(e)
            }

        }
        val status = contents.commit(googleApiClient, null).await()
        if (!status.status.isSuccess) {
            throw Exception("Error commit changes to file : " + status.statusMessage!!)
        }
    }
}