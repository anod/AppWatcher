package com.anod.appwatcher.tags

import android.database.Cursor
import android.net.Uri
import com.anod.appwatcher.content.AppTagCursor
import com.anod.appwatcher.content.CursorAsyncTask
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.model.AppTag
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.content.schema.AppTagsTable
import info.anodsplace.framework.app.ApplicationContext

/**
 * @author algavris
 * @date 26/04/2018
 */

class TagAppsAsyncTask(context: ApplicationContext, tag: Tag, private val completion: (List<AppTag>) -> Unit)
    : CursorAsyncTask<AppTag, AppTagCursor>(context, getContentUri(tag), AppTagsTable.projection) {

    override fun convert(cursor: Cursor): AppTagCursor {
        return AppTagCursor(cursor)
    }

    override fun onPostExecute(result: List<AppTag>?) {
        completion(result ?: emptyList())
    }

    companion object {
        private fun getContentUri(tag: Tag): Uri {
            val tagId = if (tag.id == -1) 0 else tag.id
            return DbContentProvider.appsTagUri
                    .buildUpon()
                    .appendPath(tagId.toString()).build()
        }
    }
}