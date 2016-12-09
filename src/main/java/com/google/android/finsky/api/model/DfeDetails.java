package com.google.android.finsky.api.model;

import com.android.volley.Response;
import com.google.android.finsky.api.DfeApi;
import com.google.android.finsky.protos.nano.Messages.Details;

public class DfeDetails extends DfeBaseModel<Details.DetailsResponse>
{
    private Details.DetailsResponse mDetailsResponse;
    private String mDetailsUrl;
    private DfeApi mDfeApi;

    public DfeDetails(final DfeApi dfeApi) {
        super();
        mDfeApi = dfeApi;
    }

    @Override
    protected void execute(Response.Listener<Details.DetailsResponse> responseListener, Response.ErrorListener errorListener) {
        mDfeApi.getDetails(mDetailsUrl, false, false, responseListener, errorListener);
    }

    public Document getDocument() {
        if (this.mDetailsResponse == null || this.mDetailsResponse.docV2 == null) {
            return null;
        }
        return new Document(this.mDetailsResponse.docV2);
    }

    @Override
    public boolean isReady() {
        return this.mDetailsResponse != null;
    }
    
    public void onResponse(final Details.DetailsResponse mDetailsResponse) {
        this.mDetailsResponse = mDetailsResponse;
        this.notifyDataSetChanged();
    }

    public void setDetailsUrl(String detailsUrl) {
        mDetailsUrl = detailsUrl;
    }
}
