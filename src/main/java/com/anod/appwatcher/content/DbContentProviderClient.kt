package com.anod.appwatcher.content

import android.app.Application
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.RemoteException
import android.provider.BaseColumns
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.contentValues
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.delete
import info.anodsplace.framework.content.query
import info.anodsplace.framework.content.update
import info.anodsplace.framework.graphics.BitmapByteArray

/**
 * Wrapper above ContentResolver to simplify access to AppInfo

 * @author alex
 */
class DbContentProviderClient(private val contentProviderClient: ContentProviderClient) {

    constructor(context: Context) : this(ApplicationContext(context))
    constructor(application: Application) : this(application.contentResolver.acquireContentProviderClient(DbContentProvider.authority))
    constructor(context: ApplicationContext) : this(context.contentResolver.acquireContentProviderClient(DbContentProvider.authority))

    fun insert(app: AppInfo): Uri? {
        val values = app.contentValues
        try {
            return contentProviderClient.insert(DbContentProvider.appsUri, values)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }
        return null
    }

    fun update(app: AppInfo): Int {
        val rowId = app.rowId
        val values = app.contentValues
        return update(rowId, values)
    }

    fun applyBatchUpdates(values: List<ContentValues>, uriMapper: (ContentValues) -> Uri): Array<ContentProviderResult> {

        val operations = values.map {
            val updateUri = uriMapper(it)
            ContentProviderOperation.newUpdate(updateUri)
                    .withValues(it)
                    .build()
        }

        return contentProviderClient.applyBatch(ArrayList(operations))
    }

    fun applyBatchInsert(values: List<ContentValues>, uriMapper: (ContentValues) -> Uri): Array<ContentProviderResult> {

        val operations = values.map {
            val insertUri = uriMapper(it)
            ContentProviderOperation.newInsert(insertUri)
                    .withValues(it)
                    .build()
        }

        return contentProviderClient.applyBatch(ArrayList(operations))
    }


    fun update(rowId: Int, values: ContentValues): Int {
        val updateUri = DbContentProvider.appsUri.buildUpon().appendPath(rowId.toString()).build()
        try {
            return contentProviderClient.update(updateUri, values)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }
        return 0
    }

    fun updateStatus(rowId: Int, status: Int): Int {
        val updateUri = DbContentProvider.appsUri.buildUpon().appendPath(rowId.toString()).build()
        val values = ContentValues()
        values.put(AppListTable.Columns.status, status)
        try {
            return contentProviderClient.update(updateUri, values)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }
        return 0
    }

    fun close() {
        contentProviderClient.release()
    }

    fun queryAppIcon(uri: Uri): Bitmap? {
        val cr: Cursor?
        try {
            cr = contentProviderClient.query(uri, arrayOf(BaseColumns._ID, AppListTable.Columns.iconCache))
        } catch (e: RemoteException) {
            AppLog.e(e)
            return null
        }
        cr.moveToPosition(-1)
        var icon: Bitmap? = null
        if (cr.moveToNext()) {
            val iconData = cr.getBlob(1)
            icon = BitmapByteArray.unflatten(iconData)
        }
        cr.close()

        return icon
    }

    fun setAppsToTag(appIds: List<String>, tagId: Int): Boolean {
        try {
            val appsTagUri = DbContentProvider.tagsUri.buildUpon().appendPath(tagId.toString()).appendPath("apps").build()
            contentProviderClient.delete(appsTagUri)
            for (appId in appIds) {
                val values = AppTag(appId, tagId).contentValues
                contentProviderClient.insert(appsTagUri, values)
            }
            return true
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return false
    }

    fun queryAppTags(rowId: Int): List<Int> {
        val appTagsUri = DbContentProvider.appsUri.buildUpon()
                .appendPath(rowId.toString())
                .appendPath("tags")
                .build()

        val tagIds = ArrayList<Int>()
        try {
            val cr = contentProviderClient.query(appTagsUri, AppTagsTable.projection)
            if (cr.count == 0) {
                return tagIds
            }
            cr.moveToPosition(-1)
            while (cr.moveToNext()) {
                val tagId = cr.getInt(AppTagsTable.Projection.tagId)
                tagIds.add(tagId)
            }
            cr.close()
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return tagIds
    }

    fun addAppToTag(appId: String, tagId: Int): Boolean {
        try {
            val appsTagUri = DbContentProvider.tagsUri
                    .buildUpon()
                    .appendPath(tagId.toString())
                    .appendPath("apps")
                    .build()
            val values = AppTag(appId, tagId).contentValues
            contentProviderClient.insert(appsTagUri, values)
            return true
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return false
    }

    fun removeAppFromTag(appId: String, tagId: Int): Boolean {
        try {
            val appsTagUri = DbContentProvider.tagsUri
                    .buildUpon()
                    .appendPath(tagId.toString())
                    .appendPath("apps")
                    .build()
            contentProviderClient.delete(appsTagUri, AppTagsTable.Columns.appId + "=?", arrayOf(appId))
            return true
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return false
    }
}