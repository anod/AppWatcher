package com.anod.appwatcher.model;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.SimpleArrayMap;

import com.google.android.finsky.api.model.Document;

public class AddWatchAppAsyncTask extends AsyncTask<Document, Void, SimpleArrayMap<String, Integer>> {
    private final WatchAppList mNewAppHandler;
    private final Listener mListener;
    private Context mContext;

    public interface Listener {
        void onAddAppTaskFinish(SimpleArrayMap<String, Integer> result);
    }

    public AddWatchAppAsyncTask(WatchAppList newAppHandler, Context context, Listener listener) {
        mNewAppHandler = newAppHandler;
        mListener = listener;
        mContext = context;
    }

    @Override
    protected SimpleArrayMap<String, Integer> doInBackground(Document... documents) {
        AppListContentProviderClient client = new AppListContentProviderClient(mContext);
        mNewAppHandler.initContentProvider(client);
        SimpleArrayMap<String, Integer> result = new SimpleArrayMap<>();
        for (Document doc : documents) {
            if (isCancelled()) {
                return result;
            }
            final AppInfo info = new AppInfo(doc);
            int status = mNewAppHandler.addSync(info);
            result.put(info.packageName, status);
        }
        client.release();
        mNewAppHandler.initContentProvider(null);
        return result;
    }

    @Override
    protected void onPostExecute(SimpleArrayMap<String, Integer> result) {
        mListener.onAddAppTaskFinish(result);
    }
}