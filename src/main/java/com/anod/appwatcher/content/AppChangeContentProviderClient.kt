package com.anod.appwatcher.content

import android.content.ContentProviderClient
import android.content.Context
import com.anod.appwatcher.model.AppChange
import com.anod.appwatcher.model.schema.ChangelogTable
import info.anodsplace.framework.content.query

/**
 * @author algavris
 * *
 * @date 10/03/2017.
 */

class AppChangeContentProviderClient {

    private var contentProviderClient: ContentProviderClient

    constructor(context: Context) {
        contentProviderClient = context.contentResolver.acquireContentProviderClient(DbContentProvider.authority)
    }

    constructor(provider: ContentProviderClient) {
        contentProviderClient = provider
    }

    fun query(appId: String, versionCode: Int): AppChange? {
        val uri = DbContentProvider.changelogUri
                .buildUpon()
                .appendPath("apps")
                .appendPath(appId)
                .appendPath("v")
                .appendPath(versionCode.toString())
                .build()

        val cr = contentProviderClient.query(uri)
        if (cr.count == 0) {
            return null
        }
        val change = AppChangeCursor(cr).moveToNextObject()
        cr.close()
        return change
    }

    fun query(appId: String): AppChangeCursor {
        val uri = DbContentProvider.changelogUri
                .buildUpon()
                .appendPath("apps")
                .appendPath(appId)
                .build()
        val cr = contentProviderClient.query(uri, sortOrder)
        return AppChangeCursor(cr)
    }

    fun close() {
        contentProviderClient.release()
    }

    companion object {
        const val sortOrder = ChangelogTable.Columns.versionCode + " DESC"
    }
}