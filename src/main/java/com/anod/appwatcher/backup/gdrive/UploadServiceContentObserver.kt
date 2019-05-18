package com.anod.appwatcher.backup.gdrive

import android.content.Context
import androidx.room.InvalidationTracker
import com.anod.appwatcher.Application
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.TagsTable
import info.anodsplace.framework.AppLog

/**
 * @author Alex Gavrishev
 * @date 26/06/2017
 */

class UploadServiceContentObserver(val context: Context)
    : InvalidationTracker.Observer(AppListTable.table, TagsTable.table, AppTagsTable.table) {

    override fun onInvalidated(tables: MutableSet<String>) {
        val prefs = Application.provide(context).prefs
        if (!prefs.isDriveSyncEnabled) {
            return
        }

        AppLog.d("Schedule GDrive upload for $tables")
        UploadService.schedule(prefs.isWifiOnly, prefs.isRequiresCharging, context)
    }

}