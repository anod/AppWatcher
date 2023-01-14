package com.anod.appwatcher.backup.gdrive

import android.content.Context
import androidx.room.InvalidationTracker
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.TagsTable
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.applog.AppLog

/**
 * @author Alex Gavrishev
 * @date 26/06/2017
 */

class UploadServiceContentObserver(private val context: Context, private val prefs: Preferences)
    : InvalidationTracker.Observer(AppListTable.table, TagsTable.table, AppTagsTable.table) {

    override fun onInvalidated(tables: Set<String>) {
        if (!prefs.isDriveSyncEnabled) {
            return
        }

        AppLog.d("Schedule GDrive upload for $tables")
        UploadService.schedule(prefs.isWifiOnly, prefs.isRequiresCharging, context)
    }
}