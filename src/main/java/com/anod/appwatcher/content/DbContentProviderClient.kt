package com.anod.appwatcher.content

import android.content.*
import android.net.Uri
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wrapper above ContentResolver to simplify access to AppInfo

 * @author alex
 */
class DbContentProviderClient(private val contentProviderClient: ContentProviderClient) {

    constructor(context: ApplicationContext) : this(context.contentResolver.acquireContentProviderClient(DbContentProvider.authority)!!)

    suspend fun applyBatchUpdates(values: List<ContentValues>, uriMapper: (ContentValues) -> Uri): Array<ContentProviderResult> = withContext(Dispatchers.IO) {

        val operations = values.map {
            val updateUri = uriMapper(it)
            ContentProviderOperation.newUpdate(updateUri)
                    .withValues(it)
                    .build()
        }

        return@withContext contentProviderClient.applyBatch(ArrayList(operations))
    }

    suspend fun applyBatchInsert(values: List<ContentValues>, uriMapper: (ContentValues) -> Uri): Array<ContentProviderResult> = withContext(Dispatchers.IO) {

        val operations = values.map {
            val insertUri = uriMapper(it)
            ContentProviderOperation.newInsert(insertUri)
                    .withValues(it)
                    .build()
        }

        return@withContext contentProviderClient.applyBatch(ArrayList(operations))
    }

    fun close() {
        contentProviderClient.release()
    }
}