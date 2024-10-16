package com.anod.appwatcher.database

import android.content.*
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.room.withTransaction

suspend fun AppsDatabase.applyBatchUpdates(
    contentResolver: ContentResolver,
    values: List<ContentValues>,
    uriMapper: (ContentValues) -> Uri
): Array<ContentProviderResult> = withContext(Dispatchers.IO) {
    val operations = values.map {
        ContentProviderOperation.newUpdate(uriMapper(it)).withValues(it).build()
    }

    return@withContext withTransaction {
        contentResolver.applyBatch(DbContentProvider.authority, ArrayList(operations))
    }
}

suspend fun AppsDatabase.applyBatchInsert(
    contentResolver: ContentResolver,
    values: List<ContentValues>,
    uriMapper: (ContentValues) -> Uri
): Array<ContentProviderResult> = withContext(Dispatchers.IO) {
    val operations = values.map {
        ContentProviderOperation.newInsert(uriMapper(it)).withValues(it).build()
    }

    return@withContext withTransaction {
        contentResolver.applyBatch(DbContentProvider.authority, ArrayList(operations))
    }
}