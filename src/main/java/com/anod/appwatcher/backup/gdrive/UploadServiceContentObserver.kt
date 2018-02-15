package com.anod.appwatcher.backup.gdrive

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import com.anod.appwatcher.App
import com.anod.appwatcher.content.DbContentProvider
import info.anodsplace.framework.AppLog

/**
 * @author algavris
 * @date 26/06/2017
 */

class UploadServiceContentObserver(val context: Context, contentResolver: ContentResolver) : ContentObserver(Handler()) {

    init {
        contentResolver.registerContentObserver(DbContentProvider.appsUri, true, this)
        contentResolver.registerContentObserver(DbContentProvider.tagsUri, true, this)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        if (!App.provide(context).prefs.isDriveSyncEnabled) {
            return
        }

        AppLog.d("Schedule GDrive upload for ${uri.toString()}")
        UploadService.schedule(context)
    }
}