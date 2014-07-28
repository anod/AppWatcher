package com.anod.appwatcher.backup.gdrive;

import android.content.Context;

import com.anod.appwatcher.backup.AppListReader;
import com.anod.appwatcher.gms.ReadDriveFileContentsAsyncTask;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.AppLog;
import com.google.android.gms.drive.DriveId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class ReadTask extends ReadDriveFileContentsAsyncTask {

    public interface Listener {

        void onReadFinish(DriveId driveId, InputStreamReader inputStreamReader);
        void onReadError();
    }
    private final Listener mListener;
    private InputStreamReader mInputStreamReader;

        @Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				mListener.onReadFinish(getDriveId(), mInputStreamReader);
			} else {
				mListener.onReadError();
			}
		}

		public ReadTask(Context context, Listener listener) {
			super(context);
			mListener = listener;
		}

		@Override
		protected boolean readDriveFileBackground(InputStream inputStream) {
            try {
                mInputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                AppLog.ex(e);
                return false;
            }

            return true;
		}
	}