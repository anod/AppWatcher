package com.google.android.finsky.api.model;

import com.android.volley.Request;
import com.google.android.finsky.api.DfeApi;
import com.google.android.finsky.protos.nano.Messages;
import com.google.android.finsky.protos.nano.Messages.ListResponse;


public final class DfeList extends ContainerList<ListResponse>
{
    private String mFilteredDocId;
    private String mInitialListUrl;
    private DfeApi mDfeApi;


    public DfeList(final DfeApi dfeApi, final String mInitialListUrl, final boolean autoLoadNextPage) {
        super(mInitialListUrl, autoLoadNextPage);
        mDfeApi = dfeApi;
        this.mFilteredDocId = null;
        this.mInitialListUrl = mInitialListUrl;
    }

    
    @Override
    public final void clearDataAndReplaceInitialUrl(final String mInitialListUrl) {
        super.clearDataAndReplaceInitialUrl(this.mInitialListUrl = mInitialListUrl);
    }
    
    @Override
    protected final void clearDiskCache() {

    }

    @Override
    protected Document[] getItemsFromResponse(ListResponse listResponse) {
        if (listResponse.doc == null || listResponse.doc.length == 0) {
            return new Document[0];
        }
        return this.updateContainerAndGetItems(listResponse.doc[0]);

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