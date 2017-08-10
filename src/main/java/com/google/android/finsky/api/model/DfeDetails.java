package com.google.android.finsky.api.model;

import com.android.volley.Response;
import com.google.android.finsky.api.DfeApi;
import com.google.android.finsky.protos.nano.Messages;
import com.google.android.finsky.protos.nano.Messages.Details;

public class DfeDetails extends DfeBaseModel
{
    private Details.DetailsResponse detailsResponse;
    public String detailsUrl;
    private DfeApi api;

    public DfeDetails(final DfeApi dfeApi) {
        super();
        this.api = dfeApi;
    }

    @Override
    protected void execute(Response.Listener<Messages.Response.ResponseWrapper> responseListener, Response.ErrorListener errorListener) {
        api.details(detailsUrl, false, false, responseListener, errorListener);
    }

    public Document getDocument() {
        if (this.detailsResponse == null || this.detailsResponse.docV2 == null) {
            return null;
        }
        return new Document(this.detailsResponse.docV2);
    }

    @Override
    public boolean isReady() {
        return this.detailsResponse != null;
    }

    @Override
    public void onResponse(Messages.Response.ResponseWrapper responseWrapper) {
        this.detailsResponse = responseWrapper.payload.detailsResponse;
        this.notifyDataSetChanged();
    }

}
