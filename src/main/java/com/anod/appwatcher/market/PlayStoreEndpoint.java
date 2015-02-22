package com.anod.appwatcher.market;

import android.accounts.Account;
import android.content.Context;

import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.NoCache;
import com.anod.appwatcher.utils.AppLog;
import com.google.android.finsky.api.DfeApi;
import com.google.android.finsky.api.DfeApiContext;
import com.google.android.finsky.api.DfeApiImpl;
import com.google.android.finsky.api.model.DfeModel;
import com.google.android.finsky.api.model.OnDataChangedListener;
import com.google.android.finsky.config.ContentLevel;
import com.google.android.volley.GoogleHttpClientStack;

/**
 * @author alex
 * @date 2015-02-22
 */
public abstract class PlayStoreEndpoint implements Response.ErrorListener, OnDataChangedListener {
    protected final Listener mListener;
    protected final String mDeviceId;
    protected final Context mContext;

    protected DfeModel mDfeModel;
    protected RequestQueue mRequestQueue;
    protected DfeApi mDfeApi;
    protected String mAuthSubToken;
    protected Account mAccount;

    public interface Listener {
        void onDataChanged();
        void onErrorResponse(VolleyError error);
    }

    public PlayStoreEndpoint(String deviceId, Listener listener, Context context) {
        mDeviceId = deviceId;
        mContext = context;
        mListener = listener;
    }

    public String getAuthSubToken() {
        return mAuthSubToken;
    }

    public PlayStoreEndpoint setAccount(Account account, String authSubToken) {
        mAccount = account;
        mAuthSubToken = authSubToken;
        return this;
    }

    public void start() {
        if (mDfeApi == null) {
            mRequestQueue = new RequestQueue(new NoCache(), createNetwork(), 2);
            mRequestQueue.start();
            DfeApiContext dfeApiContext = DfeApiContext.create(mContext, mAccount, mAuthSubToken, mDeviceId, ContentLevel.create(mContext).getDfeValue());
            mDfeApi = new DfeApiImpl(mRequestQueue, dfeApiContext);
        }
        if (mDfeModel == null) {
            mDfeModel = createDfeModel();
            mDfeModel.addDataChangedListener(this);
            mDfeModel.addErrorListener(this);
        }
        execute();
    }

    public void reset() {
        if (mDfeModel != null) {
            mDfeModel.unregisterAll();
        }
        mDfeModel = null;
    }

    protected abstract void execute();

    protected abstract DfeModel createDfeModel();

    public DfeModel getData() {
        return mDfeModel;
    }

    @Override
    public void onDataChanged() {
        if (mDfeModel.isReady()) {
            mListener.onDataChanged();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        AppLog.e("ErrorResponse: "+error.getMessage(), error);
        mListener.onErrorResponse(error);
    }


    private Network createNetwork()
    {
        return new BasicNetwork(new GoogleHttpClientStack(mContext.getApplicationContext(), false), new ByteArrayPool(1024 * 256));
    }
}
