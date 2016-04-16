package com.anod.appwatcher.market;

import android.content.Context;

import com.google.android.finsky.api.model.DfeBulkDetails;
import com.google.android.finsky.api.model.DfeDetails;
import com.google.android.finsky.api.model.DfeModel;
import com.google.android.finsky.api.model.Document;
import com.google.android.finsky.protos.DocDetails;

import java.util.List;

/**
 * @author alex
 * @date 2015-02-22
 */
public class BulkDetailsEndpoint extends PlayStoreEndpoint {
    private List<String> mDocIds;

    public BulkDetailsEndpoint(Context context) {
        super(context);
    }

    @Override
    protected void executeAsync() {
        getData().setDocIds(mDocIds);
        getData().startAsync();
    }

    @Override
    protected void executeSync() {
        getData().setDocIds(mDocIds);
        getData().startSync();
    }

    public void setDocIds(List<String> docIds) {
        mDocIds = docIds;
    }

    public DfeBulkDetails getData() {
        return (DfeBulkDetails)mDfeModel;
    }

    public List<Document> getDocuments() {
        return getData().getDocuments();
    }

    @Override
    protected DfeModel createDfeModel() {
        return new DfeBulkDetails(this.mDfeApi);
    }


}
