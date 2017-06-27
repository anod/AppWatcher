package com.anod.appwatcher.backup.gdrive

import android.content.Context
import com.anod.appwatcher.backup.DbJsonReader
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.Tag
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.*
import info.anodsplace.android.log.AppLog
import java.io.*

/**
 * @author alex
 * *
 * @date 2014-11-15
 */
class SyncConnectedWorker(private val context: Context, private val googleApiClient: GoogleApiClient) {

    @Throws(Exception::class)
    fun doSyncInBackground() {
        synchronized(sLock) {
            val cr = DbContentProviderClient(context)
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
        Drive.DriveApi.requestSync(googleApiClient).await()

        val file = DriveIdFile(AppListFile, googleApiClient)

        val driveId = file.driveId
        // There is as file exist, create driveFileReader
        if (driveId != null) {
            insertRemoteItems(driveId, cr)
        }

        if (driveId == null) {
            if (cr.getCount(false) > 0) {
                file.create()
            }
        }

        file.write(DbJsonWriter(), cr)

        AppLog.d("[GDrive] Clean locally deleted apps ")
        // Clean deleted
        val numRows = cr.cleanDeleted()
        AppLog.d("[GDrive] Cleaned $numRows rows")

        Drive.DriveApi.requestSync(googleApiClient).await()
    }

    @Throws(Exception::class)
    private fun getFileInputStream(contents: DriveContents): InputStreamReader {
        val inputStream = contents.inputStream ?: throw Exception("Empty input stream ")
        return InputStreamReader(inputStream, "UTF-8")
    }

    @Throws(Exception::class)
    private fun insertRemoteItems(driveId: DriveId, cr: DbContentProviderClient) {
        val file = driveId.asDriveFile()
        val contentsResult = file.open(googleApiClient, DriveFile.MODE_READ_ONLY, null).await()
        if (!contentsResult.status.isSuccess) {
            throw Exception("Error read file : " + contentsResult.status.statusMessage!!)
        }
        val contents = contentsResult.driveContents
        val driveFileReader = getFileInputStream(contents)

        AppLog.d("[GDrive] Sync remote list " + AppListFile.fileName)

        // Add missing remote entries
        val driveBufferedReader = BufferedReader(driveFileReader)
        val jsonReader = DbJsonReader()

        val currentIds = cr.queryPackagesMap(true)
        val currentTags = cr.queryTags().associate { it.name to it }.toMutableMap()

        val tags = mutableListOf<Tag>()
        val tagApps = mutableMapOf<String, MutableList<String>>()

        jsonReader.read(driveBufferedReader, object : DbJsonReader.OnReadListener {
            override fun onAppRead(app: AppInfo, tags: List<String>) {
                AppLog.d("[GDrive] Read app: " + app.packageName)
                if (!currentIds.containsKey(app.packageName)) {
                    cr.insert(app)
                }
                tags.forEach {
                    if (tagApps[it] == null) { tagApps[it] = mutableListOf() }
                    tagApps[it]!!.add(app.appId)
                }
            }

            override fun onTagRead(tag: Tag) {
                tags.add(tag)
            }

            @Throws(IOException::class)
            override fun onFinish() {
                driveFileReader.close()
                contents.discard(googleApiClient)

                // Add missing tags
                tags.forEach { tag ->
                    if (!currentTags.containsKey(tag.name)) {
                        cr.createTag(Tag(tag.name, tag.color))?.lastPathSegment?.toInt()?.let {
                            currentTags[tag.name] = Tag(it, tag.name, tag.color)
                        }
                    }
                }

                tagApps.forEach({ (tagName, apps) ->
                    currentTags[tagName]?.let {
                        cr.setAppsToTag(apps, it.id)
                    }
                })
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
