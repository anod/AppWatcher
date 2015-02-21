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
import com.google.android.finsky.api.DfeUtils;
import com.google.android.finsky.api.model.DfeSearch;
import com.google.android.finsky.api.model.OnDataChangedListener;
import com.google.android.finsky.config.ContentLevel;
import com.google.android.volley.GoogleHttpClientStack;

/**
 * @author alex
 * @date 2015-02-21
 */
public class SearchApps implements Response.ErrorListener, OnDataChangedListener {
    private final Listener mListener;
    private DfeSearch mSearchData;
    private RequestQueue mRequestQueue;
    private String mDeviceId;
    private DfeApi mDfeApi;
    private String mQuery;
    private Context mContext;
    private String mAuthSubToken;
    private Account mAccount;

    public String getQuery() {
        return mQuery;
    }


    public interface Listener {
        void onDataChanged();
        void onErrorResponse(VolleyError error);
    }

    public SearchApps(String deviceId, Listener listener, Context context) {
        mDeviceId = deviceId;
        mContext = context;
        mListener = listener;
    }

    public String getAuthSubToken() {
        return mAuthSubToken;
    }

    public SearchApps setAccount(Account account, String authSubToken) {
        mAccount = account;
        mAuthSubToken = authSubToken;
        return this;
    }

    public SearchApps setQuery(String query) {
        mQuery = query;
        return this;
    }

    public void reset() {
        if (mSearchData != null) {
            mSearchData.resetItems();
            mSearchData = null;
        }
    }

    public int getCount() {
        if (mSearchData == null) {
            return 0;
        }
        return mSearchData.getCount();
    }

    public void start() {
        if (mDfeApi == null) {
            mRequestQueue = new RequestQueue(new NoCache(), createNetwork(), 2);
            mRequestQueue.start();
            DfeApiContext dfeApiContext = DfeApiContext.create(mContext, mAccount, mAuthSubToken, mDeviceId, ContentLevel.create(mContext).getDfeValue());
            mDfeApi = new DfeApiImpl(mRequestQueue, dfeApiContext);
        }
        if (mSearchData == null) {
            String searchUrl = DfeUtils.formSearchUrl(mQuery, 0);
            (mSearchData = new DfeSearch(mDfeApi, mQuery, searchUrl)).addDataChangedListener(this);
            mSearchData.addErrorListener(this);
        }
        mSearchData.startLoadItems();
    }

    public DfeSearch getData() {
        return mSearchData;
    }

    @Override
    public void onDataChanged() {
        if (mSearchData.isReady()) {
            AppLog.d("Count: " + mSearchData.getCount());
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
