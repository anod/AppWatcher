package com.anod.appwatcher.content

import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.support.v4.content.ContentResolverCompat
import android.support.v4.os.CancellationSignal
import android.support.v4.os.OperationCanceledException
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.database.CursorIterator
import info.anodsplace.framework.database.NullCursor

/**
 * @author algavris
 * @date 19/04/2018
 */
abstract class CursorAsyncTask<R, out CR : CursorIterator<R>>(
        private val context: ApplicationContext,
        private val uri: Uri,
        private val projection: Array<String>,
        private val selection: String,
        private val selectionArgs: Array<String>, private val sortOrder: String)
    : AsyncTask<Void, Void, List<R>>() {

    constructor(context: ApplicationContext, uri: Uri, projection: Array<String>) : this(context, uri, projection, "", emptyArray(), "")

    abstract fun convert(cursor: Cursor): CR

    override fun doInBackground(vararg params: Void?): List<R> {
        synchronized(this) {
            if (isCancelled) {
                throw OperationCanceledException()
            }
        }
        val querySelection = if (selection.isEmpty()) null else selection
        val querySelectionArgs = if (selectionArgs.isEmpty()) null else selectionArgs
        val querySortOrder = if (sortOrder.isEmpty()) null else sortOrder

        val cursor = ContentResolverCompat.query(context.contentResolver,
                uri, projection, querySelection, querySelectionArgs, querySortOrder,
                CancellationSignal()) ?: NullCursor()
        try {
            // Ensure the cursor window is filled.
            cursor.count
        } catch (ex: RuntimeException) {
            cursor.close()
            throw ex
        }
        val typedCursor = convert(cursor)
        val result = typedCursor.toList()
        typedCursor.close()
        return result
    }
}