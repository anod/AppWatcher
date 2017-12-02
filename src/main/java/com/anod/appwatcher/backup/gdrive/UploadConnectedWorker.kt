package com.anod.appwatcher.backup.gdrive

import android.content.Context
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.content.DbContentProviderClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.drive.Drive
import com.google.android.gms.tasks.Tasks
import info.anodsplace.android.log.AppLog
import info.anodsplace.appwatcher.framework.ApplicationContext

/**
 * @author algavris
 * @date 26/06/2017
 */
class UploadConnectedWorker(private val context: ApplicationContext, private val googleAccount: GoogleSignInAccount) {

    constructor(context: Context, googleAccount: GoogleSignInAccount)
        : this(ApplicationContext(context), googleAccount)

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
        Tasks.await(Drive.getDriveClient(context.actual, googleAccount).requestSync())

        val driveClient = Drive.getDriveResourceClient(context.actual, googleAccount)
        val file = DriveIdFile(AppListFile, driveClient)

        if (file.driveId == null) {
            file.create()
        }

        file.write(DbJsonWriter(), cr)

        AppLog.d("[GDrive] Clean locally deleted apps ")
        // Clean deleted
        val numRows = cr.cleanDeleted()
        AppLog.d("[GDrive] Cleaned $numRows rows")

        Tasks.await(Drive.getDriveClient(context.actual, googleAccount).requestSync())
    }

    companion object {
        /**
         * Lock used when maintaining queue of requested updates.
         */
        private val sLock = Any()
    }
}