package com.anod.appwatcher.backup.gdrive

import android.text.format.Formatter
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.AppsDatabase
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * @author Alex Gavrishev
 * @date 26/06/2017
 */
class GDriveUpload(private val googleAccount: GoogleSignInAccount, private val context: ApplicationContext, private val database: AppsDatabase) {

    @Throws(Exception::class)
    suspend fun doUploadInBackground() {
        sLock.withLock {
            try {
                doUploadLocked(database)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun doUploadLocked(db: AppsDatabase) = withContext(Dispatchers.IO) {
        AppLog.i("Upload to remote " + AppListFile.fileName, "GDriveUpload")

        val driveClient = DriveService(createCredentials(context.actual, googleAccount), "AppWatcher")
        val file = DriveIdFile(AppListFile, driveClient, context.actual)

        if (file.getId() == null) {
            file.create()
        }

        val bytes = file.write(DbJsonWriter(), db)
        AppLog.i("Uploaded ${Formatter.formatShortFileSize(context.actual, bytes)}", "GDriveUpload")

        AppLog.d("[GDrive] Clean locally deleted apps ")
        // Clean deleted
        val numRows = db.apps().cleanDeleted()
        val numTags = db.appTags().clean()
        AppTagsTable.Queries.clean(db)
        AppLog.i("Cleaned $numRows locally deleted apps, $numTags tags", "GDriveUpload")
    }

    companion object {
        /**
         * Lock used when maintaining queue of requested updates.
         */
        private val sLock = Mutex()
    }
}