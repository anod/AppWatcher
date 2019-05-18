package com.anod.appwatcher.content

import android.content.*
import android.net.Uri
import info.anodsplace.framework.app.ApplicationContext

/**
 * Wrapper above ContentResolver to simplify access to AppInfo

 * @author alex
 */
class DbContentProviderClient(private val contentProviderClient: ContentProviderClient) {

    constructor(context: ApplicationContext) : this(context.contentResolver.acquireContentProviderClient(DbContentProvider.authority)!!)

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

    fun close() {
        contentProviderClient.release()
    }
}