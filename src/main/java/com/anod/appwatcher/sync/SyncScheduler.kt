package com.anod.appwatcher.sync

import android.content.Context
import android.os.Bundle

import com.anod.appwatcher.App
import com.firebase.jobdispatcher.*

/**
 * @author algavris
 * *
 * @date 27/05/2016.
 */

object SyncScheduler {
    private const val windowDuration = 600

    private const val tag = "AppRefresh"

    fun schedule(context: Context, requiresCharging: Boolean, requiresWifi: Boolean, windowStartSec: Int) {

        val constraints = mutableListOf<Int>()
        if (requiresCharging) {
            constraints.add(Constraint.DEVICE_CHARGING)
        }
        if (requiresWifi) {
            constraints.add(Constraint.ON_UNMETERED_NETWORK)
        } else {
            constraints.add(Constraint.ON_ANY_NETWORK)
        }
        val dispatcher = App.provide(context).jobDispatcher
        val task = dispatcher.newJobBuilder()
                .setService(SyncTaskService::class.java)
                .setTag(tag)
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(windowStartSec, windowStartSec + windowDuration))
                .setReplaceCurrent(true)
                .setConstraints(*constraints.toIntArray())
                .setExtras(Bundle())
                .build()

        App.log(context).info("Schedule sync in ${windowStartSec/3600} hours")
        dispatcher.mustSchedule(task)
    }

    fun cancel(context: Context) {
        App.provide(context).jobDispatcher.cancel(tag)
    }
}
