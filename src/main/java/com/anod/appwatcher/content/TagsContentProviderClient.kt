package com.anod.appwatcher.content

import android.content.ContentProviderClient
import android.content.Context
import android.database.Cursor
import android.os.RemoteException
import android.util.SparseIntArray
import com.anod.appwatcher.model.schema.TagsTable
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.database.NullCursor

/**
 * @author algavris
 * *
 * @date 10/03/2017.
 */

class TagsContentProviderClient {

    private var contentProviderClient: ContentProviderClient

    constructor(context: ApplicationContext) {
        contentProviderClient = context.contentResolver.acquireContentProviderClient(DbContentProvider.authority)
    }

    constructor(provider: ContentProviderClient) {
        contentProviderClient = provider
    }

    fun queryAll(): TagsCursor {
        return query(DEFAULT_SORT_ORDER, null, null)
    }

    fun queryTagsAppsCounts(): SparseIntArray {
        val counts = SparseIntArray()
        try {
            val cr = contentProviderClient.query(DbContentProvider.tagsAppsCountUri, null, null, null, null) ?: NullCursor()
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
            cr = contentProviderClient.query(DbContentProvider.tagsUri,
                    TagsTable.projection, selection, selectionArgs, sortOrder
            )
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        return TagsCursor(cr)
    }

    fun close() {
        contentProviderClient.release()
    }

    companion object {
        const val DEFAULT_SORT_ORDER = TagsTable.Columns.name + " COLLATE LOCALIZED ASC"
    }
}
