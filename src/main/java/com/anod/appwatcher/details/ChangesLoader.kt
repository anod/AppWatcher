package com.anod.appwatcher.details

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.support.v4.content.CursorLoader
import com.anod.appwatcher.content.AppChangeCursor
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.model.schema.ChangelogTable


class ChangesLoader(context: Context, appId: String): CursorLoader(context, uri(appId), ChangelogTable.projection, selection, arrayOf(appId), sortOrder) {

    override fun loadInBackground(): Cursor {
        val cr = super.loadInBackground()
        return AppChangeCursor(cr)
    }

    companion object {
        val sortOrder = "${ChangelogTable.Columns.versionCode} DESC"
        val selection = "${ChangelogTable.Columns.appId}  = ?"
        fun uri(appId: String): Uri {
            return DbContentProvider.changelogUri.buildUpon().appendPath("apps").appendPath(appId).build()
        }
     }
}