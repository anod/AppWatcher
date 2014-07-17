package com.anod.appwatcher.gms;

import android.content.Context;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.InputStream;

/**
 * @author alex
 * @date 2/28/14
 */
public abstract class ReadDriveFileContentsAsyncTask extends ApiClientAsyncTask<DriveId, Boolean, Boolean> {

    private DriveId mDriveId;

    public ReadDriveFileContentsAsyncTask(Context context) {
		super(context);
	}

    public DriveId getDriveId() {
       return mDriveId;
    }

	@Override
	protected Boolean doInBackgroundConnected(DriveId... params) {
		String contents = null;
        mDriveId = params[0];
		DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(), mDriveId);
		DriveApi.ContentsResult contentsResult =
				file.openContents(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
		if (!contentsResult.getStatus().isSuccess()) {
			return null;
		}

		InputStream inputStream = contentsResult.getContents().getInputStream();
		boolean result = readDriveFileBackground(inputStream);

		file.discardContents(getGoogleApiClient(), contentsResult.getContents()).await();
		return result;
	}

	protected abstract boolean readDriveFileBackground(InputStream inputStream);

}