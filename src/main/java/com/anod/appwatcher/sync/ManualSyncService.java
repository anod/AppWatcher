package com.anod.appwatcher.sync;

import android.app.IntentService;
import android.content.ContentProviderClient;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;

import com.anod.appwatcher.AppListContentProvider;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class ManualSyncService extends IntentService {
    private static final String ACTION_MANUAL_SYNC = "com.anod.appwatcher.sync.action.SYNC";

    public ManualSyncService() {
        super("ManualSyncService");
    }

    /**
     * Starts this service to perform action Sync. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSync(Context context) {
        Intent intent = new Intent(context, ManualSyncService.class);
        intent.setAction(ACTION_MANUAL_SYNC);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            SyncAdapter syncAdapter = new SyncAdapter(getApplicationContext());
            ContentProviderClient contentProviderClient = getContentResolver().acquireContentProviderClient(AppListContentProvider.AUTHORITY);

            Bundle bundle = new Bundle();
            bundle.putBoolean(SyncAdapter.SYNC_EXTRAS_MANUAL, true);

            int updatesCount = syncAdapter.onPerformSync(bundle, contentProviderClient);

            Intent finishIntent = new Intent(SyncAdapter.SYNC_STOP);
            finishIntent.putExtra(SyncAdapter.EXTRA_UPDATES_COUNT, updatesCount);
            sendBroadcast(finishIntent);
        }
    }

}
