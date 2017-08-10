package com.google.android.finsky.api.model;

import com.android.volley.Response;
import com.anod.appwatcher.BuildConfig;
import com.anod.appwatcher.utils.CollectionsUtils;
import com.google.android.finsky.api.DfeApi;
import com.google.android.finsky.protos.nano.Messages;
import com.google.android.finsky.protos.nano.Messages.Details;
import com.google.android.finsky.protos.nano.Messages.DocV2;

import java.util.ArrayList;
import java.util.List;

import info.anodsplace.android.log.AppLog;

public class DfeBulkDetails extends DfeBaseModel
{
    private Details.BulkDetailsResponse bulkDetailsResponse;
    private final DfeApi api;
    public List<String> docIds;

    private final CollectionsUtils.Predicate<? super Document> responseFiler;

    public DfeBulkDetails(final DfeApi dfeApi, CollectionsUtils.Predicate<Document> responseFilter) {
        super();
        api = dfeApi;
        responseFiler = responseFilter;
    }

    @Override
    protected void execute(Response.Listener<Messages.Response.ResponseWrapper> responseListener, Response.ErrorListener errorListener) {
        api.details(docIds, true, responseListener, errorListener);
    }

    public List<Document> getDocuments() {
        ArrayList<Document> list;
        if (this.bulkDetailsResponse == null) {
            list = null;
        } else {
            list = new ArrayList<>();
            for (int i = 0; i < this.bulkDetailsResponse.entry.length; ++i) {
                final DocV2 doc = this.bulkDetailsResponse.entry[i].doc;
                if (doc == null) {
                    if (BuildConfig.DEBUG) {
                        AppLog.d("Null document for requested docId: %s ", this.docIds.get(i));
                    }
                }
                else {
                    list.add(new Document(doc));
                }
            }
        }
        if (responseFiler == null || list == null) {
            return list;
        }
        return CollectionsUtils.INSTANCE.filter(list, responseFiler);
    }

    @Override
    public boolean isReady() {
        return this.bulkDetailsResponse != null;
    }

    @Override
    public void onResponse(Messages.Response.ResponseWrapper responseWrapper) {
        this.bulkDetailsResponse = responseWrapper.payload.bulkDetailsResponse;
        this.notifyDataSetChanged();
    }

}