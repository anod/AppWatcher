package com.anod.appwatcher.content

import android.content.ContentProviderClient
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.RemoteException
import android.provider.BaseColumns
import android.support.v4.util.SimpleArrayMap
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.model.AppTag
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.model.schema.TagsTable
import com.anod.appwatcher.utils.BitmapUtils
import info.anodsplace.android.log.AppLog
import java.util.*

/**
 * Wrapper above ContentResolver to simplify access to AppInfo

 * @author alex
 */
class DbContentProviderClient {

    private val mContentProviderClient: ContentProviderClient

    constructor(context: Context) {
        mContentProviderClient = context.contentResolver.acquireContentProviderClient(DbContentProvider.AUTHORITY)
    }

    constructor(provider: ContentProviderClient) {
        mContentProviderClient = provider
    }

    /**
     * Query all applications in db
     */
    fun queryAllSorted(includeDeleted: Boolean): AppListCursor {
        return queryAll(includeDeleted, DEFAULT_SORT_ORDER)
    }

    fun queryAll(includeDeleted: Boolean): AppListCursor? {
        return queryAll(includeDeleted, null)
    }

    private fun queryAll(includeDeleted: Boolean, sortOrder: String?): AppListCursor {
        if (includeDeleted) {
            return queryApps(sortOrder, null, null)
        }
        val selection = AppListTable.Columns.KEY_STATUS + " != ?"
        val selectionArgs = arrayOf(AppInfoMetadata.STATUS_DELETED.toString())

        return queryApps(sortOrder, selection, selectionArgs)
    }

    fun queryUpdated(): AppListCursor {
        val selection = AppListTable.Columns.KEY_STATUS + " = ?"
        val selectionArgs = arrayOf(AppInfoMetadata.STATUS_UPDATED.toString())
        return queryApps(null, selection, selectionArgs)
    }

    fun getCount(includeDeleted: Boolean): Int {
        val cr = queryAll(includeDeleted) ?: return 0
        return cr.count
    }

    private fun queryApps(sortOrder: String?, selection: String?, selectionArgs: Array<String>?): AppListCursor {
        var cr: Cursor? = null
        try {
            cr = mContentProviderClient.query(DbContentProvider.APPS_CONTENT_URI,
                    AppListTable.PROJECTION, selection, selectionArgs, sortOrder
            )
        } catch (e: RemoteException) {
            AppLog.e(e.message)
        }

        return AppListCursor(cr)
    }

    /**
     * @return map (AppId => RowId)
     */
    fun queryPackagesMap(includeDeleted: Boolean): SimpleArrayMap<String, Int> {
        val cursor = queryAll(includeDeleted) ?: return SimpleArrayMap()
        val result = SimpleArrayMap<String, Int>(cursor.count)
        cursor.moveToPosition(-1)
        while (cursor.moveToNext()) {
            val info = cursor.appInfo
            result.put(info.packageName, info.rowId)
        }
        cursor.close()
        return result
    }

    fun insert(app: AppInfo): Uri? {
        val values = AppListTable.createContentValues(app)
        try {
            return mContentProviderClient.insert(DbContentProvider.APPS_CONTENT_URI, values)
        } catch (e: RemoteException) {
            AppLog.e(e.message)
        }
        return null
    }

    fun update(app: AppInfo): Int {
        val rowId = app.rowId
        val values = AppListTable.createContentValues(app)
        return update(rowId, values)
    }

    fun update(rowId: Int, values: ContentValues): Int {
        val updateUri = DbContentProvider.APPS_CONTENT_URI.buildUpon().appendPath(rowId.toString()).build()
        try {
            return mContentProviderClient.update(updateUri, values, null, null)
        } catch (e: RemoteException) {
            AppLog.e(e.message)
        }
        return 0
    }

    fun updateStatus(rowId: Int, status: Int): Int {
        val updateUri = DbContentProvider.APPS_CONTENT_URI.buildUpon().appendPath(rowId.toString()).build()
        val values = ContentValues()
        values.put(AppListTable.Columns.KEY_STATUS, status)
        try {
            return mContentProviderClient.update(updateUri, values, null, null)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }
        return 0
    }

    fun cleanDeleted(): Int {
        var numRows = 0
        try {
            numRows = mContentProviderClient.delete(
                DbContentProvider.APPS_CONTENT_URI,
                AppListTable.Columns.KEY_STATUS + " = ?",
                arrayOf(AppInfoMetadata.STATUS_DELETED.toString())
            )

            val tagsCleaned = mContentProviderClient.delete(
                DbContentProvider.APPS_TAG_CLEAN_CONTENT_URI, null, null
            )
            AppLog.d("Deleted $numRows rows, tags $tagsCleaned cleaned")
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return numRows
    }

    fun close() {
        mContentProviderClient.release()
    }

    fun queryAppId(packageName: String): AppInfo? {
        val cr = queryApps(null,
                AppListTable.Columns.KEY_PACKAGE + " = ? AND " + AppListTable.Columns.KEY_STATUS + " != ?",
                arrayOf(packageName, AppInfoMetadata.STATUS_DELETED.toString()))
        if (cr.count == 0) {
            return null
        }
        cr.moveToPosition(-1)
        var info: AppInfo? = null
        if (cr.moveToNext()) {
            info = cr.appInfo
        }
        cr.close()

        return info
    }

    fun queryAppRow(rowId: Int): AppInfo? {
        val cr = queryApps(null,
                BaseColumns._ID + " = ?", arrayOf(rowId.toString()))
        if (cr.count == 0) {
            return null
        }
        val info = cr.appInfo
        cr.close()

        return info
    }

    fun queryAppIcon(uri: Uri): Bitmap? {
        val cr: Cursor?
        try {
            cr = mContentProviderClient.query(uri,
                    arrayOf(BaseColumns._ID, AppListTable.Columns.KEY_ICON_CACHE), null, null, null
            )
        } catch (e: RemoteException) {
            AppLog.e(e)
            return null
        }

        if (cr == null) {
            return null
        }
        cr.moveToPosition(-1)
        var icon: Bitmap? = null
        if (cr.moveToNext()) {
            val iconData = cr.getBlob(1)
            icon = BitmapUtils.unFlattenBitmap(iconData)
        }
        cr.close()

        return icon
    }

    fun discardAll() {
        try {
            mContentProviderClient.delete(DbContentProvider.APPS_CONTENT_URI, null, null)
            mContentProviderClient.delete(DbContentProvider.TAGS_CONTENT_URI, null, null)
            mContentProviderClient.delete(DbContentProvider.TAGS_APPS_CONTENT_URI, null, null)
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
            val cr = mContentProviderClient.query(
                    DbContentProvider.TAGS_CONTENT_URI,
                    TagsTable.PROJECTION, null, null, null
            )
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
        val values = TagsTable.createContentValues(tag)
        try {
            return mContentProviderClient.insert(DbContentProvider.TAGS_CONTENT_URI, values)
        } catch (e: RemoteException) {
            AppLog.e(e.message)
        }

        return null
    }

    fun saveTag(tag: Tag): Int {
        val updateUri = DbContentProvider.TAGS_CONTENT_URI.buildUpon().appendPath(tag.id.toString()).build()
        val values = TagsTable.createContentValues(tag)
        try {
            return mContentProviderClient.update(updateUri, values, null, null)
        } catch (e: RemoteException) {
            AppLog.e(e.message)
        }

        return 0
    }

    fun deleteTag(tag: Tag) {
        val tagDeleteUri = DbContentProvider.TAGS_CONTENT_URI.buildUpon().appendPath(tag.id.toString()).build()
        val appsTagDeleteUri = DbContentProvider.TAGS_CONTENT_URI.buildUpon().appendPath(tag.id.toString()).appendPath("apps").build()
        try {
            mContentProviderClient.delete(tagDeleteUri, null, null)
            mContentProviderClient.delete(appsTagDeleteUri, null, null)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }
    }

    fun queryAppTags(): AppTagCursor {
        try {
            val cr = mContentProviderClient.query(
                    DbContentProvider.TAGS_APPS_CONTENT_URI,
                    AppTagsTable.PROJECTION, null, null, null
            )
            return AppTagCursor(cr)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return AppTagCursor(null)
    }

    fun setAppsToTag(appIds: List<String>, tagId: Int): Boolean {
        try {
            val appsTagUri = DbContentProvider.TAGS_CONTENT_URI.buildUpon().appendPath(tagId.toString()).appendPath("apps").build()
            mContentProviderClient.delete(appsTagUri, null, null)
            for (appId in appIds) {
                val values = AppTagsTable.createContentValues(appId, tagId)
                mContentProviderClient.insert(appsTagUri, values)
            }
            return true
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return false
    }

    fun queryAppTags(rowId: Int): List<Int> {
        val appTagsUri = DbContentProvider.APPS_CONTENT_URI.buildUpon()
                .appendPath(rowId.toString())
                .appendPath("tags")
                .build()

        val tagIds = ArrayList<Int>()
        try {
            val cr = mContentProviderClient.query(appTagsUri, AppTagsTable.PROJECTION, null, null, null)
            if (cr == null || cr.count == 0) {
                return tagIds
            }
            cr.moveToPosition(-1)
            while (cr.moveToNext()) {
                val tagId = cr.getInt(AppTagsTable.Projection.TAGID)
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
            val appsTagUri = DbContentProvider.TAGS_CONTENT_URI
                    .buildUpon()
                    .appendPath(tagId.toString())
                    .appendPath("apps")
                    .build()
            val values = AppTagsTable.createContentValues(appId, tagId)
            mContentProviderClient.insert(appsTagUri, values)
            return true
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return false
    }


    fun deleteAppTags(appId: String): Int {
        try {
            return mContentProviderClient.delete(
                    DbContentProvider.TAGS_APPS_CONTENT_URI,
                    AppTagsTable.Columns.APPID + "=?",
                    arrayOf(appId))
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return 0
    }

    fun removeAppFromTag(appId: String, tagId: Int): Boolean {
        try {
            val appsTagUri = DbContentProvider.TAGS_CONTENT_URI
                    .buildUpon()
                    .appendPath(tagId.toString())
                    .appendPath("apps")
                    .build()
            mContentProviderClient.delete(appsTagUri, AppTagsTable.Columns.APPID + "=?", arrayOf(appId))
            return true
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return false
    }


    fun addAppTags(appTags: List<AppTag>) {
        val tagApps = SimpleArrayMap<Int, List<String>>()
        for (appTag in appTags) {
            var list: MutableList<String>? = tagApps.get(appTag.tagId).toMutableList()
            if (list == null) {
                list = ArrayList<String>()
                tagApps.put(appTag.tagId, list)
            }
            list.add(appTag.appId)
        }

        for (i in 0..tagApps.size() - 1) {
            val tagId = tagApps.keyAt(i)
            val list = tagApps.valueAt(i)
            setAppsToTag(list, tagId)
        }
    }

    companion object {
        private val DEFAULT_SORT_ORDER =
                AppListTable.Columns.KEY_STATUS + " DESC, " + AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED ASC"
    }

}
