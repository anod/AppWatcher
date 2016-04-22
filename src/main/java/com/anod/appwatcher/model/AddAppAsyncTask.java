package com.anod.appwatcher.model;

import android.os.AsyncTask;
import android.support.v4.util.SimpleArrayMap;

import com.anod.appwatcher.utils.DocUtils;
import com.google.android.finsky.api.model.Document;

public class AddAppAsyncTask extends AsyncTask<Document, Void, SimpleArrayMap<String, Integer>>
    {
        private final NewWatchAppHandler mNewAppHandler;
        private final Listener mListener;

        interface Listener
        {
            void onAddAppTaskFinish(SimpleArrayMap<String, Integer> result);
        }

        AddAppAsyncTask(NewWatchAppHandler newAppHandler, Listener listener) {
            mNewAppHandler = newAppHandler;
            mListener = listener;
        }

        @Override
        protected SimpleArrayMap<String, Integer> doInBackground(Document... documents) {
            SimpleArrayMap<String, Integer> result = new SimpleArrayMap<>();
            for(Document doc: documents)
            {
                final String imageUrl = DocUtils.getIconUrl(doc);
                final AppInfo info = new AppInfo(doc, null);
                int status = mNewAppHandler.addSync(info, imageUrl);
                result.put(info.getPackageName(),status);
            }
            return result;
        }

        @Override
        protected void onPostExecute(SimpleArrayMap<String, Integer> result) {
            mListener.onAddAppTaskFinish(result);
        }
    }