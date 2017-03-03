package com.google.android.finsky.api.model;

import com.android.volley.Request;
import com.anod.appwatcher.utils.CollectionsUtils;
import com.google.android.finsky.api.DfeApi;
import com.google.android.finsky.protos.nano.Messages;
import com.google.android.finsky.protos.nano.Messages.ListResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public final class DfeList extends ContainerList<ListResponse>
{
    private final DfeApi mDfeApi;
    private final CollectionsUtils.Predicate<Document> mResponseFiler;

    public DfeList(final DfeApi dfeApi, final String mInitialListUrl, final boolean autoLoadNextPage, CollectionsUtils.Predicate<Document> responseFilter) {
        super(mInitialListUrl, autoLoadNextPage);
        mDfeApi = dfeApi;
        mResponseFiler = responseFilter;
    }

    
    @Override
    public final void clearDataAndReplaceInitialUrl(final String initialListUrl) {
        super.clearDataAndReplaceInitialUrl(initialListUrl);
    }
    
    @Override
    protected final void clearDiskCache() {

    }

    @Override
    protected Document[] getItemsFromResponse(ListResponse listResponse) {
        if (listResponse.doc == null || listResponse.doc.length == 0 || listResponse.doc[0] == null) {
            return new Document[0];
        }
        Document[] docs = this.updateContainerAndGetItems(listResponse.doc[0]);
        if (mResponseFiler == null)
        {
            return docs;
        }
        List<Document> list = CollectionsUtils.filter(Arrays.asList(docs), mResponseFiler);
        return list.toArray(new Document[0]);
    }

    @Override
    protected String getNextPageUrl(ListResponse listResponse) {
        final int length = listResponse.doc.length;
        String nextPageUrl = null;
        if (length == 1) {
            final Messages.DocV2 docV2 = listResponse.doc[0];
            final Messages.Containers.ContainerMetadata containerMetadata = docV2.containerMetadata;
            nextPageUrl = null;
            if (containerMetadata != null) {
                nextPageUrl = docV2.containerMetadata.nextPageUrl;
            }
        }
        return nextPageUrl;
    }

    @Override
    protected final Request<?> makeRequest(final String url) {
        return this.mDfeApi.getList(url, this, this);
    }

}