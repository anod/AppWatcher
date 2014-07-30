package com.anod.appwatcher.backup.gdrive;

import android.content.Context;

import com.anod.appwatcher.backup.AppListReaderIterator;
import com.anod.appwatcher.backup.AppListWriter;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.utils.AppLog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by alex on 7/29/14.
 */
public class SyncTask extends ApiClientAsyncTask<Boolean, Boolean, SyncTask.Result> {

    private final Listener mListener;

    public static class Result {
        public boolean status;
        public Exception ex;

        public Result(boolean status, Exception ex) {
            this.status = status;
            this.ex = ex;
        }
    }

    public interface Listener {
        public void onResult(Result result);
    }

    public SyncTask(Context context, Listener listener, GoogleApiClient client) {
        super(context, client);
        mListener = listener;
    }

    @Override
    protected Result doInBackgroundConnected(Boolean... params) {
        SyncConnectedWorker worker = new SyncConnectedWorker(mContext, getGoogleApiClient());
        try {
            worker.doSyncInBackground();
        } catch (Exception e) {
            AppLog.ex(e);
            return new Result(false, e);
        }

        return new Result(true, null);
    }

    @Override
    protected void onPostExecute(Result result) {
        mListener.onResult(result);
    }



}
