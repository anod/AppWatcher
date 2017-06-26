package com.anod.appwatcher.utils

import android.content.Context
import android.os.Bundle

import com.anod.appwatcher.App

/**
 * @author algavris
 * *
 * @date 03/09/2016.
 */
object MetricsManagerEvent {
    fun track(context: Context, eventName: String, vararg params: String) {
        val bundle = Bundle()

        var i = 0
        while (i < params.size) {
            bundle.putString(params[i], params[i + 1])
            i += 2
        }

        App.provide(context).fireBase.logEvent(eventName, bundle)
    }
}
