package com.anod.appwatcher.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.widget.Toast;

import com.anod.appwatcher.backup.gdrive.SyncTask;
import com.anod.appwatcher.utils.ActivityListener;
import com.anod.appwatcher.utils.AppLog;
import com.anod.appwatcher.utils.GooglePlayServices;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.io.File;

/**
 * @author alex
 * @date 1/19/14
 */
public class GDriveSync extends GooglePlayServices implements SyncTask.Listener {


    private static final int ACTION_SYNC = 1;

    private final Listener mListener;

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
