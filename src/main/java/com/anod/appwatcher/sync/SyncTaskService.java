package com.anod.appwatcher.sync;

import android.content.ContentProviderClient;
import android.content.Intent;

import com.anod.appwatcher.AppListContentProvider;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import info.anodsplace.android.log.AppLog;

public class SyncTaskService extends GcmTaskService {

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        SyncScheduler.schedule(getApplicationContext());
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        AppLog.d("Scheduled call executed. Task: " + taskParams.getTag());

        SyncAdapter syncAdapter = new SyncAdapter(getApplicationContext());
        ContentProviderClient contentProviderClient = getContentResolver().acquireContentProviderClient(AppListContentProvider.AUTHORITY);

        int updatesCount = syncAdapter.onPerformSync(taskParams.getExtras(), contentProviderClient);

        Intent finishIntent = new Intent(SyncAdapter.SYNC_STOP);
        finishIntent.putExtra(SyncAdapter.EXTRA_UPDATES_COUNT, updatesCount);
        sendBroadcast(finishIntent);

        return GcmNetworkManager.RESULT_SUCCESS;
    }
}
