package com.anod.appwatcher.backup;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.anod.appwatcher.R;

import java.io.File;

public class ImportTask extends AsyncTask<Uri, Void, Integer> {

    private Context mContext;
    private Listener mListener;

    public ImportTask(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
    }

    public static void showImportFinishToast(Context context, int code) {
        switch (code) {
            case DbBackupManager.RESULT_OK:
                Toast.makeText(context, context.getString(R.string.import_done), Toast.LENGTH_SHORT).show();
                break;
            case DbBackupManager.ERROR_STORAGE_NOT_AVAILABLE:
                Toast.makeText(context, context.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show();
                break;
            case DbBackupManager.ERROR_DESERIALIZE:
                Toast.makeText(context, context.getString(R.string.restore_deserialize_failed), Toast.LENGTH_SHORT).show();
                break;
            case DbBackupManager.ERROR_FILE_READ:
                Toast.makeText(context, context.getString(R.string.failed_to_read_file), Toast.LENGTH_SHORT).show();
                break;
            case DbBackupManager.ERROR_FILE_NOT_EXIST:
                Toast.makeText(context, context.getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public interface Listener {
        void onImportFinish(int code);
    }

    protected Integer doInBackground(Uri... sources) {
        Uri srcUri = sources[0];
        DbBackupManager mBackupManager = new DbBackupManager(mContext);
        if (srcUri.getScheme().equals(ContentResolver.SCHEME_FILE))
        {
            int res = validateFileDestination(srcUri);
            if (res != DbBackupManager.RESULT_OK) {
                return res;
            }
        }
        return mBackupManager.doImport(srcUri);
    }

    private int validateFileDestination(Uri destUri) {
        if (!checkMediaReadable()) {
            return DbBackupManager.ERROR_STORAGE_NOT_AVAILABLE;
        }

        File dataFile = new File(destUri.getPath());
        if (!dataFile.exists()) {
            return DbBackupManager.ERROR_FILE_NOT_EXIST;
        }
        if (!dataFile.canRead()) {
            return DbBackupManager.ERROR_FILE_READ;
        }
        return DbBackupManager.RESULT_OK;
    }

    /**
     * Checks if it possible to read from the backup directory
     *
     * @return true/false
     */
    private boolean checkMediaReadable() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)
                && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return false;
        }
        return true;
    }

    protected void onPostExecute(Integer result) {
        mListener.onImportFinish(result);
    }

}
