package com.google.android.finsky.api.model;

import com.android.volley.Request;
import com.anod.appwatcher.utils.CollectionsUtils;
import com.google.android.finsky.api.DfeApi;
import com.google.android.finsky.api.DfeUtils;
import com.google.android.finsky.protos.nano.Messages;
import com.google.android.finsky.protos.nano.Messages.ListResponse;

import java.util.Arrays;
import java.util.List;


public final class DfeList extends ContainerList<ListResponse>
{
    private final DfeApi mDfeApi;

    public DfeList(final DfeApi dfeApi, final String mInitialListUrl, final boolean autoLoadNextPage, CollectionsUtils.Predicate<Document> responseFilter) {
        super(mInitialListUrl, autoLoadNextPage, responseFilter);
        mDfeApi = dfeApi;
    }

    @Override
    protected final Request<?> makeRequest(final String url) {
        return this.mDfeApi.list(url, this, this);
    }

}