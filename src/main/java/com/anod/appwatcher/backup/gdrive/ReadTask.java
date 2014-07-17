package com.anod.appwatcher.backup.gdrive;

import android.content.Context;

import com.anod.appwatcher.backup.AppListReader;
import com.anod.appwatcher.backup.GDriveBackup;
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
         void onReadFinish(DriveId driveId, List<AppInfo> list);
         void onReadError();
     }

    private final Listener mListener;
    private List<AppInfo> mAppList;

        @Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				mListener.onReadFinish(getDriveId(), mAppList);
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
            BufferedReader buf = null;
            try {
                buf = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                AppLog.ex(e);
                return false;
            }

            AppListReader reader = new AppListReader();
            try {
                mAppList = reader.readFromJson(buf);
            } catch (IOException e) {
                AppLog.ex(e);
                return false;
            }
            return true;
		}
	}