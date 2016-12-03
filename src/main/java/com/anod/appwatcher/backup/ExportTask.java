package com.anod.appwatcher.backup;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;

public class ExportTask extends AsyncTask<Uri, Void, Integer> {

    private Context mContext;
    private Listener mListener;

    public ExportTask(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
    }

    public interface Listener {
        void onExportStart();
        void onExportFinish(int code);
    }

    @Override
    protected void onPreExecute() {
        mListener.onExportStart();
    }

    protected Integer doInBackground(Uri... dest) {
        Uri destUri = dest[0];
        ListBackupManager mBackupManager = new ListBackupManager(mContext);
        if (destUri.getScheme().equals(ContentResolver.SCHEME_FILE))
        {
            int res = validateFileDestination(destUri);
            if (res != ListBackupManager.RESULT_OK) {
                return res;
            }
        }
        return mBackupManager.doExport(destUri);
    }

    private int validateFileDestination(Uri destUri) {
        if (!checkMediaWritable()) {
            return ListBackupManager.ERROR_STORAGE_NOT_AVAILABLE;
        }

        File destFile = new File(destUri.getPath());
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        return ListBackupManager.RESULT_OK;
    }

    /**
     * Checks if it possible to write to the backup directory
     *
     * @return true/false
     */
    private boolean checkMediaWritable() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            return false;
        }
        return true;
    }



    protected void onPostExecute(Integer result) {
        mListener.onExportFinish(result);
    }

}
