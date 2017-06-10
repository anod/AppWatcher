package com.anod.appwatcher.content

import android.content.ContentProviderClient
import android.content.Context
import android.database.Cursor
import android.os.RemoteException
import android.util.SparseIntArray
import com.anod.appwatcher.model.schema.TagsTable
import info.anodsplace.android.log.AppLog

/**
 * @author algavris
 * *
 * @date 10/03/2017.
 */

class TagsContentProviderClient {

    private var mContentProviderClient: ContentProviderClient

    constructor(context: Context) {
        mContentProviderClient = context.contentResolver.acquireContentProviderClient(DbContentProvider.AUTHORITY)
    }

    constructor(provider: ContentProviderClient) {
        mContentProviderClient = provider
    }

    fun queryAll(): TagsCursor {
        return query(DEFAULT_SORT_ORDER, null, null)
    }

    fun queryTagsAppsCounts(): SparseIntArray {
        val counts = SparseIntArray()
        try {
            val cr = mContentProviderClient.query(DbContentProvider.TAGS_APPS_COUNT_CONTENT_URI, null, null, null, null) ?: NullCursor()
            cr.moveToPosition(-1)
            while (cr.moveToNext()) {
                counts.put(cr.getInt(0), cr.getInt(1))
            }
            cr.close()
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return counts
    }

    fun query(sortOrder: String, selection: String?, selectionArgs: Array<String>?): TagsCursor {
        var cr: Cursor? = null
        try {
            cr = mContentProviderClient.query(DbContentProvider.TAGS_CONTENT_URI,
                    TagsTable.PROJECTION, selection, selectionArgs, sortOrder
            )
        } catch (e: RemoteException) {
            AppLog.e(e.message)
        }

        return TagsCursor(cr)
    }

    fun close() {
        mContentProviderClient.release()
    }

    companion object {
        val DEFAULT_SORT_ORDER = TagsTable.Columns.NAME + " COLLATE LOCALIZED ASC"
    }
}
