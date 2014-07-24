package com.anod.appwatcher.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.widget.Toast;

import com.anod.appwatcher.backup.gdrive.ReadTask;
import com.anod.appwatcher.backup.gdrive.WriteTask;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.ActivityListener;
import com.anod.appwatcher.utils.AppLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.File;
import java.util.List;

/**
 * @author alex
 * @date 1/19/14
 */
public class GDriveBackup implements GoogleApiClient.ConnectionCallbacks,
                                      GoogleApiClient.OnConnectionFailedListener,
                                    ActivityListener.ResultListener,
        ReadTask.Listener, WriteTask.Listener
{
	public static final int REQUEST_CODE_RESOLUTION = 123;

	public static final String MIME_TYPE = "application/json";


    private static final int ACTION_SYNC = 1;
    private static final int ACTION_CONNECT = 2;

    public static final String APPLIST_JSON = "applist.json";
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
		void onGDriveActionStart();
		void onGDriveDownloadFinish();
		void onGDriveUploadFinish();
		void onGDriveError();
	}


    public GDriveBackup(Activity activity, Listener listener) {
        mContext = activity.getApplicationContext();
        mActivity = activity;
        mListener = listener;
    }

    private ResultCallback<DriveApi.ContentsResult> mFileCreateRequest = new ResultCallback<DriveApi.ContentsResult>() {
        @Override
        public void onResult(DriveApi.ContentsResult contentsResult) {
            if (!contentsResult.getStatus().isSuccess()) {
                Toast.makeText(mContext,"Error while trying to create new file contents",Toast.LENGTH_SHORT).show();
                return;
            }
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(APPLIST_JSON)
                    .setMimeType(MIME_TYPE)
                    .build();

            Drive.DriveApi
                    .getAppFolder(mGoogleApiClient)
                    .createFile(mGoogleApiClient, changeSet, contentsResult.getContents())
                    .setResultCallback(mFileCreateResult);
        }
    };

    private ResultCallback<DriveFolder.DriveFileResult> mFileCreateResult = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(DriveFolder.DriveFileResult driveFileResult) {
            if (!driveFileResult.getStatus().isSuccess()) {
              //  showMessage("Error while trying to create the file");
                return;
            }
            doSync(driveFileResult.getDriveFile().getDriveId(), null);
        }
    };

    private ResultCallback<DriveApi.MetadataBufferResult> mQueryResult = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
            if (!metadataBufferResult.getStatus().isSuccess()) {
                //showMessage("Problem while retrieving files");
                AppLog.d("Problem retrieving "+APPLIST_JSON);
                return;
            }
            MetadataBuffer metadataList = metadataBufferResult.getMetadataBuffer();
            if (metadataList.getCount() == 0) {
                AppLog.d("File not found " + APPLIST_JSON);
                requestNewContents();
                return;
            }
            Metadata metadata = metadataList.get(0);
            DriveId driveId = metadata.getDriveId();
            new ReadTask(mContext, GDriveBackup.this).execute(driveId);
        }
    };


    private void doSync(DriveId driveId, List<AppInfo> list) {

    }


	public void connect() {
        connectWithAction(ACTION_CONNECT);
	}

    public void sync() {
        mListener.onGDriveActionStart();
        if (!isConnected()) {
            connectWithAction(ACTION_SYNC);
        } else {
            retrieveFileConnected();
        }
    }

    protected void connectWithAction(int action) {
        mOnConnectAction = action;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_APPFOLDER)
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
			retrieveFileConnected();
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


	private void retrieveFileConnected() {

        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, APPLIST_JSON))
                .build();
        Drive.DriveApi
            .getAppFolder(mGoogleApiClient)
            .queryChildren(mGoogleApiClient, query)
            .setResultCallback(mQueryResult);
	}

	private void requestNewContents() {
		Drive.DriveApi.newContents(mGoogleApiClient).setResultCallback(mFileCreateRequest);
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
    public void onWriteFinish() {

    }

    @Override
    public void onWriteError() {

    }

    @Override
    public void onReadFinish(DriveId driveId, List<AppInfo> list) {
        doSync(driveId, list);
    }


    @Override
    public void onReadError() {

    }
}
