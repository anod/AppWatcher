package com.anod.appwatcher

import android.content.Context
import info.anodsplace.appwatcher.framework.ApplicationContext

/**
 * @author algavris
 * *
 * @date 07/05/2016.
 */
object App {

    fun with(context: Context): AppWatcherApplication {
        return context.applicationContext as AppWatcherApplication
    }

    fun with(context: ApplicationContext): AppWatcherApplication {
        return with(context)
    }

    fun provide(context: Context): ObjectGraph {
        return (context.applicationContext as AppWatcherApplication).objectGraph
    }

    fun provide(context: ApplicationContext): ObjectGraph {
        return with(context.actual).objectGraph
    }
}
