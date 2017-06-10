package com.anod.appwatcher.backup.gdrive

import android.content.Context
import com.anod.appwatcher.backup.DbJsonReader
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppTag
import com.anod.appwatcher.model.Tag
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.*
import com.google.android.gms.drive.query.*
import info.anodsplace.android.log.AppLog
import java.io.*

/**
 * @author alex
 * *
 * @date 2014-11-15
 */
class SyncConnectedWorker(private val mContext: Context, private val mGoogleApiClient: GoogleApiClient) {

    @Throws(Exception::class)
    fun doSyncInBackground() {
        synchronized(sLock) {
            val cr = DbContentProviderClient(mContext)
            try {
                doSyncLocked(cr)
            } catch (e: Exception) {
                throw Exception(e)
            } finally {
                cr.close()
            }
        }
    }

    @Throws(Exception::class)
    private fun doSyncLocked(cr: DbContentProviderClient) {
        Drive.DriveApi.requestSync(mGoogleApiClient).await()

        var driveId = retrieveFileDriveId()
        // There is as file exist, create driveFileReader
        if (driveId != null) {
            insertRemoteItems(driveId, cr)
        }

        if (driveId == null) {
            if (cr.getCount(false) > 0) {
                driveId = createNewFile()
            }
        }

        if (driveId != null) {
            writeToDrive(driveId, cr)
        }

        AppLog.d("[GDrive] Clean locally deleted apps ")
        // Clean deleted
        val numRows = cr.cleanDeleted()
        AppLog.d("[GDrive] Cleaned $numRows rows")

        Drive.DriveApi.requestSync(mGoogleApiClient).await()
    }

    @Throws(Exception::class)
    private fun writeToDrive(driveId: DriveId, cr: DbContentProviderClient) {

        AppLog.d("[GDrive] Write full list to remote ")

        val target = driveId.asDriveFile()

        val contentsResult = target.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).await()
        if (!contentsResult.status.isSuccess) {
            throw Exception("Error open file for write : " + contentsResult.status.statusMessage!!)
        }

        val contents = contentsResult.driveContents
        val outputStream = contents.outputStream
        val writer = DbJsonWriter()
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
        val status = contents.commit(mGoogleApiClient, null).await()
        if (!status.status.isSuccess) {
            throw Exception("Error commit changes to file : " + status.statusMessage!!)
        }
    }

    @Throws(Exception::class)
    private fun getFileInputStream(contents: DriveContents): InputStreamReader {
        val inputStream = contents.inputStream ?: throw Exception("Empty input stream ")
        return InputStreamReader(inputStream, "UTF-8")
    }

    @Throws(Exception::class)
    private fun insertRemoteItems(driveId: DriveId, cr: DbContentProviderClient) {
        val file = driveId.asDriveFile()
        val contentsResult = file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).await()
        if (!contentsResult.status.isSuccess) {
            throw Exception("Error read file : " + contentsResult.status.statusMessage!!)
        }
        val contents = contentsResult.driveContents
        val driveFileReader = getFileInputStream(contents)

        AppLog.d("[GDrive] Sync remote list " + APPLIST_JSON)

        // Add missing remote entries
        val driveBufferedReader = BufferedReader(driveFileReader)
        val jsonReader = DbJsonReader()

        val currentIds = cr.queryPackagesMap(true)
        jsonReader.read(driveBufferedReader, object : DbJsonReader.OnReadListener {
            override fun onAppRead(app: AppInfo) {
                AppLog.d("[GDrive] Read app: " + app.packageName)
                if (!currentIds.containsKey(app.packageName)) {
                    cr.insert(app)
                }
            }

            override fun onTagRead(tag: Tag) {

            }

            override fun onAppTagRead(appTag: AppTag) {

            }

            @Throws(IOException::class)
            override fun onFinish() {
                driveFileReader.close()
                contents.discard(mGoogleApiClient)
            }
        })
    }

    @Throws(Exception::class)
    private fun retrieveFileDriveId(): DriveId? {

        val order = SortOrder.Builder()
                .addSortDescending(SortableField.QUOTA_USED)
                .build()

        val query = Query.Builder()
                .addFilter(Filters.and(
                        Filters.eq(SearchableField.MIME_TYPE, MIME_TYPE),
                        Filters.eq(SearchableField.TITLE, APPLIST_JSON)
                ))
                .setSortOrder(order)
                .build()

        val metadataBufferResult = Drive.DriveApi
                .getAppFolder(mGoogleApiClient)
                .queryChildren(mGoogleApiClient, query)
                .await()

        if (!metadataBufferResult.status.isSuccess) {
            throw Exception("Problem retrieving " + APPLIST_JSON + " : " + metadataBufferResult.status.statusMessage)
        }
        val metadataList = metadataBufferResult.metadataBuffer
        if (metadataList.count == 0) {
            AppLog.d("[GDrive] File NOT found " + APPLIST_JSON)
            return null
        } else {
            val metadata = metadataList.get(0)
            AppLog.d("[GDrive] File found " + APPLIST_JSON)
            return metadata.driveId
        }
    }

    @Throws(Exception::class)
    private fun createNewFile(): DriveId {
        val contentsResult = Drive.DriveApi.newDriveContents(mGoogleApiClient).await()
        AppLog.d("[GDrive] Create new file ")

        if (!contentsResult.status.isSuccess) {
            throw Exception("[Google Drive] File create request filed: " + contentsResult.status.statusMessage!!)
        }
        val changeSet = MetadataChangeSet.Builder()
                .setTitle(APPLIST_JSON)
                .setMimeType(MIME_TYPE)
                .build()

        val driveFileResult = Drive.DriveApi
                .getAppFolder(mGoogleApiClient)
                .createFile(mGoogleApiClient, changeSet, contentsResult.driveContents).await()

        if (!driveFileResult.status.isSuccess) {
            throw Exception("[Google Drive] File create result filed: " + driveFileResult.status.statusMessage!!)
        }
        return driveFileResult.driveFile.driveId
    }

    companion object {
        /**
         * Lock used when maintaining queue of requested updates.
         */
        private val sLock = Any()

        private val APPLIST_JSON = "applist.json"
        private val MIME_TYPE = "application/json"
    }

}
