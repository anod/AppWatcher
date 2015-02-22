package com.anod.appwatcher.market;

import android.content.Context;

import com.google.android.finsky.api.DfeUtils;
import com.google.android.finsky.api.model.DfeModel;
import com.google.android.finsky.api.model.DfeSearch;

/**
 * @author alex
 * @date 2015-02-21
 */
public class SearchEndpoint extends PlayStoreEndpoint {

    private String mQuery;

    public SearchEndpoint(String deviceId, Listener listener, Context context) {
        super(deviceId, listener, context);
    }

    public String getQuery() {
        return mQuery;
    }

    public SearchEndpoint setQuery(String query) {
        mQuery = query;
        return this;
    }

    public DfeSearch getData() {
        return (DfeSearch)mDfeModel;
    }

    @Override
    public void reset() {
        if (getData() != null) {
            getData().resetItems();
            super.reset();
        }
    }

    public int getCount() {
        if (mDfeModel == null) {
            return 0;
        }
        return getData().getCount();
    }


    @Override
    protected void execute() {
        getData().startLoadItems();
    }

    @Override
    protected DfeModel createDfeModel() {
        String searchUrl = DfeUtils.formSearchUrl(mQuery, 0);
        return new DfeSearch(mDfeApi, mQuery, searchUrl);
    }
}
