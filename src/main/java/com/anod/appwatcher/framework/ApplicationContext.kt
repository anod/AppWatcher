package com.anod.appwatcher.framework

import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.annotation.StringRes
import com.anod.appwatcher.AppWatcherApplication

/**
 * @author algavris
 * @date 25/10/2017
 */
class ApplicationContext(context: Context) {
    val actual: AppWatcherApplication = context.applicationContext as AppWatcherApplication

    val contentResolver: ContentResolver
        get() = actual.contentResolver
    val notificationManager: NotificationManager
        get() = actual.objectGraph.notificationManager

    fun getString(@StringRes resId: Int): String {
        return actual.getString(resId)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return actual.getString(resId, *formatArgs)
    }

    val packageManager: PackageManager
        get() = actual.packageManager

    fun sendBroadcast(intent: Intent) {
        actual.sendBroadcast(intent)
    }
}