package com.anod.appwatcher.backup.gdrive

import android.content.Context
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.content.DbContentProviderClient
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import info.anodsplace.android.log.AppLog

/**
 * @author algavris
 * @date 26/06/2017
 */
class UploadConnectedWorker(private val context: Context, private val googleApiClient: GoogleApiClient) {

    @Throws(Exception::class)
    fun doUploadInBackground() {
        synchronized(sLock) {
            val cr = DbContentProviderClient(context)
            try {
                doUploadLocked(cr)
            } catch (e: Exception) {
                throw Exception(e)
            } finally {
                cr.close()
            }
        }
    }

    @Throws(Exception::class)
    private fun doUploadLocked(cr: DbContentProviderClient) {
        Drive.DriveApi.requestSync(googleApiClient).await()

        val file = DriveIdFile(AppListFile, googleApiClient)

        if (file.driveId == null) {
            file.create()
        }

        file.write(DbJsonWriter(), cr)

        AppLog.d("[GDrive] Clean locally deleted apps ")
        // Clean deleted
        val numRows = cr.cleanDeleted()
        AppLog.d("[GDrive] Cleaned $numRows rows")

        Drive.DriveApi.requestSync(googleApiClient).await()
    }

    companion object {
        /**
         * Lock used when maintaining queue of requested updates.
         */
        private val sLock = Any()
    }
}