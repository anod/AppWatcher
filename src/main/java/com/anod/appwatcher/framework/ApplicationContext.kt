package com.anod.appwatcher.framework

import android.content.ContentResolver
import android.content.Context

/**
 * @author algavris
 * @date 25/10/2017
 */
class ApplicationContext(context: Context) {
    val actual: Context = context.applicationContext

    val contentResolver: ContentResolver
        get() = actual.contentResolver
}