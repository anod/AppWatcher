package com.anod.appwatcher.details

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.support.v4.content.CursorLoader
import com.android.volley.VolleyError
import com.anod.appwatcher.App
import com.anod.appwatcher.content.AppChangeCursor
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.model.AppChange
import com.anod.appwatcher.model.schema.ChangelogTable
import finsky.api.model.Document
import finsky.protos.nano.Messages
import info.anodsplace.playstore.DetailsEndpoint


class ChangesLoader(context: Context, private var appId: String,private val detailsEndpoint: DetailsEndpoint?):
        CursorLoader(context, uri(appId), ChangelogTable.projection, selection, arrayOf(appId), sortOrder) {

    override fun loadInBackground(): Cursor {
        val cr = super.loadInBackground()

        try {
            detailsEndpoint?.startSync()
        } catch (e: VolleyError) {
            App.log(context).error("Fetching of details failed ${e.message ?: ""}")
        }

        return AppChangeCursor(cr)
    }

    companion object {
        val sortOrder = "${ChangelogTable.Columns.versionCode} DESC"
        val selection = "${ChangelogTable.Columns.appId}  = ?"
        fun uri(appId: String): Uri {
            return DbContentProvider.changelogUri.buildUpon().appendPath("apps").appendPath(appId).build()
        }
     }

    val document: Document?
        get() = detailsEndpoint?.document

    val appDetails: Messages.AppDetails?
        get() = detailsEndpoint?.appDetails

    val recentChange: AppChange
        get() {
            val details = appDetails ?: return AppChange(appId, 0, "", "")
            return AppChange(appId, details.versionCode, details.versionString, details.recentChangesHtml ?: "")
        }

}