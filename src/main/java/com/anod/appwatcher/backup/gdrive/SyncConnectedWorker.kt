package com.anod.appwatcher.backup.gdrive

import com.anod.appwatcher.Application
import com.anod.appwatcher.backup.DbJsonReader
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.TagsTable
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.database.entities.Tag
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveContents
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.DriveId
import com.google.android.gms.tasks.Tasks
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * @author alex
 * *
 * @date 2014-11-15
 */
class SyncConnectedWorker(private val context: ApplicationContext, private val googleAccount: GoogleSignInAccount) {

    fun doSyncInBackground() {
        synchronized(sLock) {
            val db = Application.provide(context).database
            try {
                doSyncLocked(db)
            } catch (e: Exception) {
                throw Exception(e)
            }
        }
    }

    private fun doSyncLocked(db: AppsDatabase) {
        Tasks.await(Drive.getDriveClient(context.actual, googleAccount).requestSync())

        val driveClient = Drive.getDriveResourceClient(context.actual, googleAccount)

        val file = DriveIdFile(AppListFile, driveClient)

        val driveId = file.driveId
        // There is as file exist, create driveFileReader
        if (driveId != null) {
            insertRemoteItems(driveId, db)
        }

        if (driveId == null) {
            if (db.apps().count(false) > 0) {
                file.create()
            }
        }

        file.write(DbJsonWriter(), db)

        AppLog.d("[GDrive] Clean locally deleted apps ")
        // Clean deleted
        val numRows = db.apps().cleanDeleted()
        db.appTags().clean()
        AppLog.d("[GDrive] Cleaned $numRows rows")

        Tasks.await(Drive.getDriveClient(context.actual, googleAccount).requestSync())
    }

    private fun getFileInputStream(contents: DriveContents): InputStreamReader {
        val inputStream = contents.inputStream ?: throw Exception("Empty input stream ")
        return InputStreamReader(inputStream, "UTF-8")
    }

    @Throws(Exception::class)
    private fun insertRemoteItems(driveId: DriveId, db: AppsDatabase) {
        val file = driveId.asDriveFile()

        val driveClient = Drive.getDriveResourceClient(context.actual, googleAccount)

        val driveContents = Tasks.await(driveClient.openFile(file, DriveFile.MODE_READ_ONLY))

        val driveFileReader = getFileInputStream(driveContents)

        AppLog.d("[GDrive] Sync remote list " + AppListFile.fileName)

        // Add missing remote entries
        val driveBufferedReader = BufferedReader(driveFileReader)
        val jsonReader = DbJsonReader()

        val currentIds = db.apps().loadPackages(true).associate { it.packageName to it.rowId }
        val currentTags = db.tags().load().associate { it.name to it }.toMutableMap()

        val tagList = mutableListOf<Tag>()
        val tagApps = mutableMapOf<String, MutableList<String>>()

        jsonReader.read(driveBufferedReader, object : DbJsonReader.OnReadListener {
            override fun onAppRead(app: AppInfo, tags: List<String>) {
                AppLog.d("[GDrive] Read app: " + app.packageName)
                if (!currentIds.containsKey(app.packageName)) {
                    AppListTable.Queries.insert(app, db)
                }
                tags.forEach {
                    if (tagApps[it] == null) { tagApps[it] = mutableListOf() }
                    tagApps[it]!!.add(app.appId)
                }
            }

            override fun onTagRead(tag: Tag) {
                tagList.add(tag)
            }

            override fun onFinish() {
                driveFileReader.close()
                driveClient.discardContents(driveContents)

                // Add missing tags
                tagList.forEach { tag ->
                    if (!currentTags.containsKey(tag.name)) {
                        val rowId = TagsTable.Queries.insert(Tag(tag.name, tag.color), db).toInt()
                        if (rowId > 0) {
                            currentTags[tag.name] = Tag(rowId, tag.name, tag.color)
                        }
                    }
                }

                tagApps.forEach { (tagName, apps) ->
                    currentTags[tagName]?.let { tag ->
                        AppTagsTable.Queries.insert(tag, apps, db)
                    }
                }
            }
        })
    }

    companion object {
        /**
         * Lock used when maintaining queue of requested updates.
         */
        private val sLock = Any()
    }

}
