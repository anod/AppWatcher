package com.anod.appwatcher.details

import android.database.Cursor
import android.net.Uri
import com.anod.appwatcher.content.AppChangeCursor
import com.anod.appwatcher.content.CursorAsyncTask
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.model.AppChange
import com.anod.appwatcher.model.schema.ChangelogTable
import info.anodsplace.framework.app.ApplicationContext

class ChangesAsyncTask(context: ApplicationContext, val appId: String, private val completion: (List<AppChange>) -> Unit)
    : CursorAsyncTask<AppChange, AppChangeCursor>(
        context, uri(appId), ChangelogTable.projection, selection, arrayOf(appId), sortOrder) {

    override fun convert(cursor: Cursor): AppChangeCursor {
        return AppChangeCursor(cursor)
    }

    companion object {
        const val sortOrder = "${ChangelogTable.Columns.versionCode} DESC"
        const val selection = "${ChangelogTable.Columns.appId} = ?"
        fun uri(appId: String): Uri {
            return DbContentProvider.changelogUri.buildUpon().appendPath("apps").appendPath(appId).build()
        }
    }

    override fun onPostExecute(result: List<AppChange>) {
        this.completion(result)
    }
}