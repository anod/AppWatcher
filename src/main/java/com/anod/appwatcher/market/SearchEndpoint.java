package com.anod.appwatcher.market;

import android.content.Context;

import com.google.android.finsky.api.DfeUtils;
import com.google.android.finsky.api.model.DfeModel;
import com.google.android.finsky.api.model.DfeSearch;

/**
 * @author alex
 * @date 2015-02-21
 */
public class SearchEndpoint extends PlayStoreEndpointBase {

    private static final int BACKEND_ID = 3;
    private String mQuery;
    private boolean mAutoLoadNextPage;

    public SearchEndpoint(Context context, boolean autoLoadNextPage) {
        super(context);
        mAutoLoadNextPage = autoLoadNextPage;
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
    protected void executeAsync() {
        getData().startLoadItems();
    }

    @Override
    protected void executeSync() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected DfeModel createDfeModel() {
        String searchUrl = DfeUtils.formSearchUrl(mQuery, BACKEND_ID);
        return new DfeSearch(mDfeApi, mQuery, searchUrl, mAutoLoadNextPage, AppDetailsFilter.createPredicate());
    }
}
