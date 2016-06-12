package com.anod.appwatcher.sync;

import android.content.Context;
import android.os.Bundle;

import com.anod.appwatcher.App;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;

/**
 * @author algavris
 * @date 27/05/2016.
 */

public class SyncScheduler {
    private static final int ONE_HOUR_IN_SEC = 3600;
    private static final int TEN_MINUTES_IN_SEC = 600;

    private static final String TASK_TAG = "AppRefresh";

    public static void schedule(Context context, boolean requiresCharging) {

        PeriodicTask task = new PeriodicTask.Builder()
                .setExtras(new Bundle())
                .setService(SyncTaskService.class)
                .setTag(TASK_TAG)
                .setFlex(TEN_MINUTES_IN_SEC)
                .setPeriod(ONE_HOUR_IN_SEC)
                .setRequiresCharging(requiresCharging)
                .setPersisted(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setUpdateCurrent(true)
                .build();

        App.provide(context).gcmNetworkManager().schedule(task);
    }

    public static void cancel(Context context) {
        App.provide(context).gcmNetworkManager().cancelTask(TASK_TAG, SyncTaskService.class);
    }
}
