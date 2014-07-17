package com.anod.appwatcher.gms;

import android.content.Context;

import com.anod.appwatcher.utils.AppLog;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author alex
 * @date 2/28/14
 */
public class WriteDriveFileContentsAsyncTask extends ApiClientAsyncTask<WriteDriveFileContentsAsyncTask.FilesParam, Void, Boolean> {

	public static class FilesParam {
		private final InputStream mSourceInputStream;
		private final DriveId mDriveId;

		public FilesParam(InputStream sourceInputStream, DriveId driveId) {
			mDriveId = driveId;
            mSourceInputStream = sourceInputStream;
        }

		public DriveId getDriveId() {
			return mDriveId;
		}

		public InputStream getSource() {
			return mSourceInputStream;
		}
	}

	public WriteDriveFileContentsAsyncTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackgroundConnected(FilesParam... args) {
		FilesParam files = args[0];
		try {
			DriveFile target = Drive.DriveApi.getFile(getGoogleApiClient(), files.getDriveId());

			DriveApi.ContentsResult contentsResult = target.openContents(
					getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
			if (!contentsResult.getStatus().isSuccess()) {
				return false;
			}
			//FileInputStream fileInputStream = new FileInputStream(files.getSource());
			InputStream inputStream = new BufferedInputStream(files.getSource());
			OutputStream outputStream = contentsResult.getContents().getOutputStream();
			copyStream(inputStream, outputStream);
			com.google.android.gms.common.api.Status status = target.commitAndCloseContents(
					getGoogleApiClient(), contentsResult.getContents()).await();
			return status.getStatus().isSuccess();
		} catch (IOException e) {
			AppLog.ex(e);
		}
		return false;
	}

	private void copyStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (!result) {
			//showMessage("Error while editing contents");
			return;
		}
		//showMessage("Successfully edited contents");
	}
}