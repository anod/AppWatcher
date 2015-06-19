package com.anod.appwatcher.backup;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.anod.appwatcher.R;
import com.anod.appwatcher.backup.gdrive.SyncConnectedWorker;
import com.anod.appwatcher.backup.gdrive.SyncTask;
import com.anod.appwatcher.utils.GooglePlayServices;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

/**
 * @author alex
 * @date 1/19/14
 */
public class GDriveSync extends GooglePlayServices implements SyncTask.Listener {


    private static final int ACTION_SYNC = 2;
    private static final int NOTIFICATION_ID = 2;

    private Listener mListener;

    public void showResolutionNotification(PendingIntent resolution) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_stat_update)
                .setContentTitle("Google Drive sync failed.")
                .setContentText("Required user action")
                .setContentIntent(resolution)
        ;

        Notification notification = builder.build();
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }


    public interface Listener {
        void onGDriveConnect();
        void onGDriveSyncProgress();
        void onGDriveSyncStart();
        void onGDriveSyncFinish();
		void onGDriveError();
	}

    public GDriveSync(Activity activity, Listener listener) {
        super(activity);
        mListener = listener;
    }

    public GDriveSync(Context context) {
        super(context);
    }

    @Override
    protected void onConnectAction(int action) {
        mListener.onGDriveConnect();
        if (action == ACTION_SYNC) {
            new SyncTask(mContext, this, createGoogleApiClientBuilder().build()).execute(true);
        }
    }

    @Override
    protected void onConnectionError() {
        mListener.onGDriveError();
    }

    public void sync() {
        mListener.onGDriveSyncStart();
        if (!isConnected()) {
            connectWithAction(ACTION_SYNC);
        } else {
            new SyncTask(mContext, this, createGoogleApiClientBuilder().build()).execute(true);
        }
    }

    public void syncLocked() throws Exception {
        if (!isConnected()) {
            connectLocked();
        }
        SyncConnectedWorker worker = new SyncConnectedWorker(mContext, mGoogleApiClient);
        worker.doSyncInBackground();
    }

    protected GoogleApiClient.Builder createGoogleApiClientBuilder() {
        return new GoogleApiClient.Builder(mContext)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER);
    }

    @Override
    public void onResult(SyncTask.Result result) {
        if (result == null) {
            //Connection error
            mListener.onGDriveError();
            return;
        }
        if (result.status) {
            mListener.onGDriveSyncFinish();
        } else {
            Toast.makeText(mContext, result.ex.getMessage(), Toast.LENGTH_SHORT).show();
            mListener.onGDriveError();
        }
    }

}
