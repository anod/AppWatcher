package com.anod.appwatcher.backup.gdrive

import android.content.Context
import com.anod.appwatcher.Application
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.database.AppsDatabase
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.drive.Drive
import com.google.android.gms.tasks.Tasks
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * @author Alex Gavrishev
 * @date 26/06/2017
 */
class GDriveUpload(private val context: ApplicationContext, private val googleAccount: GoogleSignInAccount) {

    constructor(context: Context, googleAccount: GoogleSignInAccount)
        : this(ApplicationContext(context), googleAccount)

    @Throws(Exception::class)
    suspend fun doUploadInBackground() {
        val db = Application.provide(context).database
        sLock.withLock {
            try {
                doUploadLocked(db)
            } catch (e: Exception) {
                throw Exception(e)
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun doUploadLocked(db: AppsDatabase) = withContext(Dispatchers.IO) {
        Tasks.await(Drive.getDriveClient(context.actual, googleAccount).requestSync())

        val driveClient = Drive.getDriveResourceClient(context.actual, googleAccount)
        val file = DriveIdFile(AppListFile, driveClient)

        if (file.driveId == null) {
            file.create()
        }

        file.write(DbJsonWriter(), db)

        AppLog.d("[GDrive] Clean locally deleted apps ")
        // Clean deleted
        val numRows = db.apps().cleanDeleted()
        db.appTags().clean()
        AppLog.d("[GDrive] Cleaned $numRows rows")

        Tasks.await(Drive.getDriveClient(context.actual, googleAccount).requestSync())
    }

    companion object {
        /**
         * Lock used when maintaining queue of requested updates.
         */
        private val sLock = Mutex()
    }
}