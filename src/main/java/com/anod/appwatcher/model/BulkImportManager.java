package com.anod.appwatcher.model;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.SimpleArrayMap;

import com.android.volley.VolleyError;
import com.anod.appwatcher.market.BulkDetailsEndpoint;
import com.anod.appwatcher.market.PlayStoreEndpoint;

import com.anod.appwatcher.utils.DocUtils;
import com.google.android.finsky.api.model.Document;

import java.util.ArrayList;
import java.util.List;

public class BulkImportManager implements PlayStoreEndpoint.Listener, AddAppAsyncTask.Listener {
    private static final int BULK_SIZE = 20;

    private final BulkDetailsEndpoint mEndpoint;
    private final NewWatchAppHandler mNewAppHandler;
    private List<List<String>> listsDocIds;
    private int currentBulk;
    private BulkImportManager.Listener mListener;

    interface Listener
    {
        void onImportProgress(List<String> docIds, SimpleArrayMap<String, Integer> result);
        void onImportFinish();
        void onImportStart(List<String> docIds);
    }

    public BulkImportManager(Context context, Listener listener) {
        mEndpoint = new BulkDetailsEndpoint(context);
        mEndpoint.setListener(this);
        mNewAppHandler = new NewWatchAppHandler(context, null);
        mListener = listener;
    }

    public void init() {
        listsDocIds = new ArrayList<>();
        currentBulk = 0;
    }

    public void addPackage(String packageName) {
        List<String> currentList = listsDocIds.get(currentBulk);
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
        new AddAppAsyncTask(mNewAppHandler, this).execute(docs.toArray(new Document[docs.size()]));
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    public void setAccount(Account account, String authSubToken) {
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