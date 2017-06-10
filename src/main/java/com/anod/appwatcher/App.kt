package com.anod.appwatcher

import android.content.Context

/**
 * @author algavris
 * *
 * @date 07/05/2016.
 */
object App {

    fun with(context: Context): AppWatcherApplication {
        return AppWatcherApplication.get(context)
    }

    fun provide(context: Context): ObjectGraph {
        return AppWatcherApplication.provide(context)
    }

}
