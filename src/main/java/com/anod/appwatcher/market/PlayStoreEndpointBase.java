package com.anod.appwatcher.market;

import android.accounts.Account;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.anod.appwatcher.AppWatcherApplication;
import com.google.android.finsky.api.DfeApi;
import com.google.android.finsky.api.DfeApiContext;
import com.google.android.finsky.api.DfeApiImpl;
import com.google.android.finsky.api.model.DfeModel;
import com.google.android.finsky.api.model.OnDataChangedListener;
import com.google.android.finsky.config.ContentLevel;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 2015-02-22
 */
public abstract class PlayStoreEndpointBase implements PlayStoreEndpoint, Response.ErrorListener, OnDataChangedListener {
    protected Listener mListener;
    protected final Context mContext;

    DfeModel mDfeModel;
    DfeApi mDfeApi;
    private String mAuthSubToken;
    private Account mAccount;

    PlayStoreEndpointBase(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public String getAuthSubToken() {
        return mAuthSubToken;
    }

    @Override
    public PlayStoreEndpoint setAccount(Account account, String authSubToken) {
        mAccount = account;
        mAuthSubToken = authSubToken;
        return this;
    }

    @Override
    public void startAsync() {
        init();
        executeAsync();
    }

    @Override
    public void startSync() {
        init();
        executeSync();
    }

    protected abstract void executeAsync();
    protected abstract void executeSync();

    private void init() {
        if (mDfeApi == null) {
            RequestQueue queue = AppWatcherApplication.provide(mContext).requestQueue();
            String deviceId = AppWatcherApplication.provide(mContext).deviceId();
            DfeApiContext dfeApiContext = DfeApiContext.create(mContext, mAccount, mAuthSubToken, deviceId, ContentLevel.create(mContext).getDfeValue());
            mDfeApi = new DfeApiImpl(queue, dfeApiContext);
        }
        if (mDfeModel == null) {
            mDfeModel = createDfeModel();
            mDfeModel.addDataChangedListener(this);
            mDfeModel.addErrorListener(this);
        }
    }

    @Override
    public void reset() {
        if (mDfeModel != null) {
            mDfeModel.unregisterAll();
        }
        mDfeModel = null;
    }

    protected abstract DfeModel createDfeModel();

    public DfeModel getData() {
        return mDfeModel;
    }

    @Override
    public void onDataChanged() {
        if (mListener == null) {
            return;
        }
        if (mDfeModel.isReady()) {
            mListener.onDataChanged();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        AppLog.e("ErrorResponse: " + error.getMessage(), error);
        if (mListener == null) {
            return;
        }
        mListener.onErrorResponse(error);
    }

}
