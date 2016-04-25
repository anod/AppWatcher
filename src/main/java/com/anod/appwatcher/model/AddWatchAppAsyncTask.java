package com.anod.appwatcher.model;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.SimpleArrayMap;

import com.anod.appwatcher.utils.DocUtils;
import com.google.android.finsky.api.model.Document;

public class AddWatchAppAsyncTask extends AsyncTask<Document, Void, SimpleArrayMap<String, Integer>> {
    private final AddWatchAppHandler mNewAppHandler;
    private final Listener mListener;
    private Context mContext;

    public interface Listener {
        void onAddAppTaskFinish(SimpleArrayMap<String, Integer> result);
    }

    public AddWatchAppAsyncTask(AddWatchAppHandler newAppHandler, Context context, Listener listener) {
        mNewAppHandler = newAppHandler;
        mListener = listener;
        mContext = context;
    }

    @Override
    protected SimpleArrayMap<String, Integer> doInBackground(Document... documents) {
        AppListContentProviderClient client = new AppListContentProviderClient(mContext);
        mNewAppHandler.setContentProvider(client);
        SimpleArrayMap<String, Integer> result = new SimpleArrayMap<>();
        for (Document doc : documents) {
            if (isCancelled()) {
                return result;
            }
            final String imageUrl = DocUtils.getIconUrl(doc);
            final AppInfo info = new AppInfo(doc, null);
            int status = mNewAppHandler.addSync(info, imageUrl);
            result.put(info.getPackageName(), status);
        }
        client.release();
        mNewAppHandler.setContentProvider(null);
        return result;
    }

    @Override
    protected void onPostExecute(SimpleArrayMap<String, Integer> result) {
        mListener.onAddAppTaskFinish(result);
    }
}