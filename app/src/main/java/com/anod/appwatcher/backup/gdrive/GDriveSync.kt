package com.anod.appwatcher.backup.gdrive

import android.accounts.Account
import android.text.format.Formatter
import com.anod.appwatcher.backup.DbJsonReader
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.TagsTable
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Tag
import com.google.android.gms.auth.UserRecoverableAuthException
import info.anodsplace.applog.AppLog
import java.io.BufferedReader
import java.io.Reader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * @author alex
 * *
 * @date 2014-11-15
 */
class GDriveSync(private val googleAccount: Account, private val context: info.anodsplace.context.ApplicationContext, private val database: AppsDatabase) {

    class SyncError(val error: UserRecoverableAuthException?, cause: Exception) : Exception(cause.message, cause)

    suspend fun doSync() = withContext(Dispatchers.IO) {
        try {
            sLock.withLock {
                return@withContext doSyncLocked(database)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("Sync failed: ${e.message}", "GDriveSync")
            throw SyncError(DriveService.extractUserRecoverableException(e), e)
        }
    }

    private suspend fun doSyncLocked(db: AppsDatabase) = withContext(Dispatchers.IO) {
        AppLog.i("Sync to remote " + AppListFile.fileName, "GDriveSync")
        val driveClient = DriveService(createCredentials(context.actual, googleAccount), "AppWatcher")
        val file = DriveIdFile(AppListFile, driveClient, context.actual)

        AppLog.i("Check if there is an existing file", "GDriveSync")
        val driveId = file.getId()

        if (driveId != null) {
            AppLog.i("Read remote items", "GDriveSync")
            insertRemoteItems(file, db)
        }

        if (driveId == null) {
            if (db.apps().count(false) > 0) {
                file.create()
            }
        }

        val deletedTagIds = db.tags().loadDeletedIds()
        val bytes = file.write(DbJsonWriter(), db)
        AppLog.i("Uploaded ${Formatter.formatShortFileSize(context.actual, bytes)}", "GDriveSync")

        AppLog.d("Clean locally deleted apps and tags")
        val numRows = db.apps().cleanDeleted()
        val numTags = db.appTags().clean()
        AppTagsTable.Queries.clean(db)
        val numDeletedTags = if (bytes > 0 && deletedTagIds.isNotEmpty()) {
            db.tags().cleanDeleted(deletedTagIds)
        } else {
            0
        }
        AppLog.i("Cleaned $numRows locally deleted apps, $numTags tags, $numDeletedTags deleted tag records", "GDriveSync")
    }

    @Throws(Exception::class)
    private suspend fun insertRemoteItems(file: DriveIdFile, db: AppsDatabase) {
        val reader = file.read() ?: throw IllegalStateException("Cannot read file")
        reader.use {
            insertRemoteItems(it, db)
        }
    }

    companion object {
        internal suspend fun insertRemoteItems(reader: Reader, db: AppsDatabase) = withContext(Dispatchers.IO) {
            // Add missing remote entries
            val driveBufferedReader = BufferedReader(reader)
            val jsonReader = DbJsonReader()

            val currentIds = db.apps().loadPackages(true).associate { it.packageName to it.rowId }
            val currentTags = db.tags().load().associateBy { it.name }.toMutableMap()
            val deletedTagNames = db.tags().loadDeletedNames().toSet()

            val tagList = mutableListOf<Tag>()
            val tagApps = mutableMapOf<String, MutableList<String>>()

            jsonReader.read(driveBufferedReader, object : DbJsonReader.OnReadListener {
                override suspend fun onAppRead(app: App, tags: List<String>) {
                    AppLog.d("[GDrive] Read app: " + app.packageName)
                    if (!currentIds.containsKey(app.packageName)) {
                        AppListTable.Queries.insert(app, db)
                    }
                    tags.forEach {
                        if (tagApps[it] == null) {
                            tagApps[it] = mutableListOf()
                        }
                        tagApps[it]!!.add(app.appId)
                    }
                }

                override suspend fun onTagRead(tag: Tag) {
                    tagList.add(tag)
                }

                override suspend fun onFinish(appsRead: Int, tagsRead: Int) {
                    AppLog.i("Read from remote $appsRead apps, $tagsRead tags", "GDriveSync")
                    // Add missing tags
                    tagList.forEach { tag ->
                        if (tag.name !in deletedTagNames && !currentTags.containsKey(tag.name)) {
                            val rowId = TagsTable.Queries.insert(Tag(tag.name, tag.color), db).toInt()
                            if (rowId > 0) {
                                currentTags[tag.name] = Tag(rowId, tag.name, tag.color, Tag.STATUS_NORMAL)
                            }
                        }
                    }

                    tagApps.forEach { (tagName, apps) ->
                        currentTags[tagName]?.takeIf { tagName !in deletedTagNames }?.let { tag ->
                            AppTagsTable.Queries.insert(tag, apps, db)
                        }
                    }
                }
            })
        }

        /**
         * Lock used when maintaining queue of requested updates.
         */
        private val sLock = Mutex()
    }
}