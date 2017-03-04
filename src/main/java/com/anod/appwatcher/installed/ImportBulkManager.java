package com.anod.appwatcher.installed;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.SimpleArrayMap;

import com.android.volley.VolleyError;
import com.anod.appwatcher.market.BulkDetailsEndpoint;
import com.anod.appwatcher.market.PlayStoreEndpoint;
import com.anod.appwatcher.model.AddWatchAppAsyncTask;
import com.anod.appwatcher.model.WatchAppList;
import com.google.android.finsky.api.model.Document;

import java.util.ArrayList;
import java.util.List;

class ImportBulkManager implements PlayStoreEndpoint.Listener, AddWatchAppAsyncTask.Listener {
    private static final int BULK_SIZE = 20;

    private final BulkDetailsEndpoint mEndpoint;
    private final WatchAppList mWatchAppList;
    private final Context mContext;
    private List<List<String>> listsDocIds;
    private int currentBulk;
    private ImportBulkManager.Listener mListener;
    private AsyncTask<Document, Void, SimpleArrayMap<String, Integer>> mTask;

    public interface Listener
    {
        void onImportProgress(List<String> docIds, SimpleArrayMap<String, Integer> result);
        void onImportFinish();
        void onImportStart(List<String> docIds);
    }

    ImportBulkManager(Context context, Listener listener) {
        mEndpoint = new BulkDetailsEndpoint(context);
        mEndpoint.setListener(this);
        mWatchAppList = new WatchAppList(null);
        mContext = context;
        mListener = listener;
    }

    public void init() {
        listsDocIds = new ArrayList<>();
        currentBulk = 0;
    }


    void stop() {
        mEndpoint.reset();
        if (mTask != null && !mTask.isCancelled()){
            mTask.cancel(true);
            mTask = null;
        }
    }

    void addPackage(String packageName) {
        List<String> currentList = null;
        if (listsDocIds.size() > currentBulk) {
            currentList = listsDocIds.get(currentBulk);
        }
        if (currentList == null) {
            currentList = new ArrayList<>();
            listsDocIds.add(currentList);
        } else if (currentList.size() > BULK_SIZE) {
            currentBulk++;
            currentList = new ArrayList<>();
            listsDocIds.add(currentList);
        }
        currentList.add(packageName);
    }

    public boolean isEmpty() {
        return listsDocIds == null || listsDocIds.size() == 0;
    }

    public void start() {
        currentBulk = 0;
        nextBulk();
    }

    private void nextBulk() {
        List<String> docIds = listsDocIds.get(currentBulk);
        mListener.onImportStart(docIds);
        mEndpoint.setDocIds(docIds);
        mEndpoint.startAsync();
    }

    @Override
    public void onDataChanged() {
        List<Document> docs = mEndpoint.getDocuments();
        mTask = new AddWatchAppAsyncTask(mWatchAppList, mContext, this).execute(docs.toArray(new Document[docs.size()]));
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        List<String> docIds = listsDocIds.get(currentBulk);
        mListener.onImportProgress(docIds, new SimpleArrayMap<String, Integer>());
        currentBulk++;
        if (currentBulk == listsDocIds.size()) {
            mListener.onImportFinish();
        }
        else
        {
            nextBulk();
        }
    }

    void setAccount(Account account, String authSubToken) {
        mEndpoint.setAccount(account, authSubToken);
    }

    @Override
    public void onAddAppTaskFinish(SimpleArrayMap<String, Integer> result) {
        List<String> docIds = listsDocIds.get(currentBulk);
        mListener.onImportProgress(docIds, result);
        currentBulk++;
        if (currentBulk == listsDocIds.size()) {
            mListener.onImportFinish();
        }
        else
        {
            nextBulk();
        }
    }
}