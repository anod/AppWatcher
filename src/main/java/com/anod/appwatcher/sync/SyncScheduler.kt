package com.anod.appwatcher.sync

import android.content.Context
import android.os.Bundle

import com.anod.appwatcher.App
import com.google.android.gms.gcm.PeriodicTask
import com.google.android.gms.gcm.Task

/**
 * @author algavris
 * *
 * @date 27/05/2016.
 */

object SyncScheduler {
    private val ONE_HOUR_IN_SEC = 3600
    private val TEN_MINUTES_IN_SEC = 600

    private val TASK_TAG = "AppRefresh"

    fun schedule(context: Context, requiresCharging: Boolean) {

        val task = PeriodicTask.Builder()
                .setExtras(Bundle())
                .setService(SyncTaskService::class.java)
                .setTag(TASK_TAG)
                .setFlex(TEN_MINUTES_IN_SEC.toLong())
                .setPeriod((2 * ONE_HOUR_IN_SEC).toLong())
                .setRequiresCharging(requiresCharging)
                .setPersisted(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setUpdateCurrent(true)
                .build()

        App.provide(context).gcmNetworkManager.schedule(task)
    }

    fun cancel(context: Context) {
        App.provide(context).gcmNetworkManager.cancelTask(TASK_TAG, SyncTaskService::class.java)
    }
}
