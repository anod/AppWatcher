package com.anod.appwatcher.backup.gdrive;

import android.content.Context;

import com.anod.appwatcher.gms.WriteDriveFileContentsAsyncTask;

public class WriteTask extends WriteDriveFileContentsAsyncTask {
    public interface Listener {
        void onWriteFinish();
        void onWriteError();
    }

    private final Listener mListener;

		public WriteTask(Context context, Listener listener) {
			super(context);
			mListener = listener;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				mListener.onWriteFinish();
			} else {
				mListener.onWriteError();
			}
		}
	}