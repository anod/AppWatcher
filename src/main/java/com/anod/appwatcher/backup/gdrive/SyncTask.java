package com.anod.appwatcher.backup.gdrive;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

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
