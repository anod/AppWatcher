package com.anod.appwatcher.sync

import android.content.Context
import android.os.Bundle
import com.anod.appwatcher.Application
import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.Lifetime
import com.firebase.jobdispatcher.Trigger
import info.anodsplace.framework.AppLog

/**
 * @author Alex Gavrishev
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
        val dispatcher = Application.provide(context).jobDispatcher
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

        AppLog.i("Schedule sync in ${windowStartSec/3600} hours")
        dispatcher.mustSchedule(task)
    }

    fun cancel(context: Context) {
        Application.provide(context).jobDispatcher.cancel(tag)
    }
}
