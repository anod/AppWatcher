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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.io.File;

/**
 * @author alex
 * @date 1/19/14
 */
public class GDriveSync implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ActivityListener.ResultListener, SyncTask.Listener {
	public static final int REQUEST_CODE_RESOLUTION = 123;

    private static final int ACTION_SYNC = 1;
    private static final int ACTION_CONNECT = 2;

    private final Listener mListener;

    private GoogleApiClient mGoogleApiClient;
	private Context mContext;
	private Activity mActivity;
	private boolean mConnected;
	private int mOnConnectAction;
	private File mFile;
	private boolean mSupported;


    public interface Listener {
        void onGDriveConnect();
        void onGDriveSyncProgress();
        void onGDriveSyncStart();
        void onGDriveSyncFinish();
		void onGDriveError();
	}

    public GDriveSync(Activity activity, Listener listener) {
        mContext = activity.getApplicationContext();
        mActivity = activity;
        mListener = listener;
    }


	public void connect() {
        connectWithAction(ACTION_CONNECT);
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

    protected void connectWithAction(int action) {
        mOnConnectAction = action;
        if (mGoogleApiClient == null) {

            mGoogleApiClient = createGoogleApiClientBuilder()
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

	public void disconnect() {
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
        mListener.onGDriveConnect();
		if (mOnConnectAction == ACTION_SYNC) {
            new SyncTask(mContext, this, createGoogleApiClientBuilder().build()).execute(true);
        }
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		AppLog.e("GoogleApiClient connection failed: " + result.toString());

		if (!result.hasResolution()) {
			// show the localized error dialog.
			mListener.onGDriveError();
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), mActivity, 0).show();
			return;
		}
		try {
			result.startResolutionForResult(mActivity, REQUEST_CODE_RESOLUTION);
		} catch (IntentSender.SendIntentException e) {
			AppLog.ex(e);
			mListener.onGDriveError();
		}
	}

	public boolean isConnected() {
		return mGoogleApiClient != null && mGoogleApiClient.isConnected();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
		if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK) {
			connectWithAction(mOnConnectAction);
        }
	}

	public boolean isSupported() {
		return GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS;
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
