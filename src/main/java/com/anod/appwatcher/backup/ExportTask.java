package com.anod.appwatcher.backup;

import android.content.Context;
import android.os.AsyncTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExportTask extends AsyncTask<String, Void, Integer> {

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

    protected Integer doInBackground(String... filenames) {
        ListExportManager mBackupManager = new ListExportManager(mContext);
        SimpleDateFormat sdf = new SimpleDateFormat(ListExportManager.DATE_FORMAT_FILENAME, Locale.US);
        String filename = sdf.format(new Date(System.currentTimeMillis()));
        return mBackupManager.doExport(filename);
    }

    protected void onPostExecute(Integer result) {
        mListener.onExportFinish(result);
    }

}
