package com.anod.appwatcher.content

import android.app.Application
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.RemoteException
import android.provider.BaseColumns
import android.support.v4.util.SimpleArrayMap
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.TagsTable
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

    /**
     * Query all applications in db
     */
    fun queryAllSorted(includeDeleted: Boolean): AppListCursor {
        return queryAll(includeDeleted, defaultAppsSortOrder)
    }

    fun queryAll(includeDeleted: Boolean): AppListCursor {
        return queryAll(includeDeleted, null)
    }

    private fun queryAll(includeDeleted: Boolean, sortOrder: String?): AppListCursor {
        if (includeDeleted) {
            return queryApps(sortOrder, null, null)
        }
        val selection = AppListTable.Columns.status + " != ?"
        val selectionArgs = arrayOf(AppInfoMetadata.STATUS_DELETED.toString())

        return queryApps(sortOrder, selection, selectionArgs)
    }

    fun getCount(includeDeleted: Boolean): Int {
        val cr = queryAll(includeDeleted)
        return cr.count
    }

    private fun queryApps(sortOrder: String?, selection: String?, selectionArgs: Array<String>?): AppListCursor {
        var cr: Cursor? = null
        try {
            cr = contentProviderClient.query(DbContentProvider.appsUri,
                    AppListTable.projection, selection, selectionArgs, sortOrder
            )
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return AppListCursor(cr)
    }

    /**
     * @return map (AppId => RowId)
     */
    fun queryPackagesMap(includeDeleted: Boolean): SimpleArrayMap<String, Int> {
        val cursor = queryAll(includeDeleted)
        val result = SimpleArrayMap<String, Int>(cursor.count)
        cursor.forEach { result.put(it.packageName, it.rowId) }
        cursor.close()
        return result
    }

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

    fun cleanDeleted(): Int {
        var numRows = 0
        try {
            numRows = contentProviderClient.delete(
                DbContentProvider.appsUri,
                AppListTable.Columns.status + " = ?",
                arrayOf(AppInfoMetadata.STATUS_DELETED.toString())
            )

            val tagsCleaned = contentProviderClient.delete(
                DbContentProvider.appsTagCleanUri, null, null
            )
            AppLog.d("Deleted $numRows rows, tags $tagsCleaned cleaned")
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return numRows
    }

    fun close() {
        contentProviderClient.release()
    }

    fun queryAppId(packageName: String): AppInfo? {
        val cr = queryApps(null,
                AppListTable.Columns.packageName + " = ?",
                arrayOf(packageName))
        val info = cr.firstOrNull()
        cr.close()
        return info
    }

    fun queryAppRow(rowId: Int): AppInfo? {
        val cr = queryApps(null,
                BaseColumns._ID + " = ?", arrayOf(rowId.toString()))

        val info = cr.firstOrNull()
        cr.close()
        return info
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

    fun discardAll() {
        try {
            contentProviderClient.delete(DbContentProvider.appsUri)
            contentProviderClient.delete(DbContentProvider.tagsUri)
            contentProviderClient.delete(DbContentProvider.tagsAppsUri)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

    }

    fun addApps(appList: List<AppInfo>) {
        val currentIds = queryPackagesMap(true)
        for (app in appList) {
            val rowId = currentIds.get(app.packageName)
            if (rowId == null) {
                insert(app)
            } else {
                app.rowId = rowId
                update(app)
            }
        }
    }

    fun queryTags(): TagsCursor {
        try {
            val cr = contentProviderClient.query(DbContentProvider.tagsUri, TagsTable.projection)
            return TagsCursor(cr)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return TagsCursor(null)
    }

    fun addTags(tags: List<Tag>) {
        for (tag in tags) {
            createTag(tag)
        }
    }

    fun createTag(tag: Tag): Uri? {
        val values = tag.contentValues
        try {
            return contentProviderClient.insert(DbContentProvider.tagsUri, values)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return null
    }

    fun saveTag(tag: Tag): Int {
        val updateUri = DbContentProvider.tagsUri.buildUpon().appendPath(tag.id.toString()).build()
        val values = tag.contentValues
        try {
            return contentProviderClient.update(updateUri, values)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return 0
    }

    fun deleteTag(tag: Tag) {
        val tagDeleteUri = DbContentProvider.tagsUri.buildUpon().appendPath(tag.id.toString()).build()
        val appsTagDeleteUri = DbContentProvider.tagsUri.buildUpon().appendPath(tag.id.toString()).appendPath("apps").build()
        try {
            contentProviderClient.delete(tagDeleteUri)
            contentProviderClient.delete(appsTagDeleteUri)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }
    }

    fun queryAppTags(): AppTagCursor {
        try {
            val cr = contentProviderClient.query(
                    DbContentProvider.tagsAppsUri,
                    AppTagsTable.projection
            )
            return AppTagCursor(cr)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return AppTagCursor(null)
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


    fun deleteAppTags(appId: String): Int {
        try {
            return contentProviderClient.delete(
                    DbContentProvider.tagsAppsUri,
                    AppTagsTable.Columns.appId + "=?",
                    arrayOf(appId))
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return 0
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


    fun addAppTags(appTags: List<AppTag>) {
        val tagApps = SimpleArrayMap<Int, MutableList<String>>()
        for (appTag in appTags) {
            if (tagApps.get(appTag.tagId) == null) {
                tagApps.put(appTag.tagId, mutableListOf())
            }
            tagApps.get(appTag.tagId)!!.add(appTag.appId)
        }

        for (i in 0 until tagApps.size()) {
            val tagId = tagApps.keyAt(i)
            val list = tagApps.valueAt(i)
            setAppsToTag(list, tagId)
        }
    }

    companion object {
        private const val defaultAppsSortOrder =
                AppListTable.Columns.status + " DESC, " + AppListTable.Columns.title + " COLLATE LOCALIZED ASC"
    }
}