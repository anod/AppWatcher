package com.google.android.finsky.api.model;

import com.android.volley.Response;
import com.google.android.finsky.api.DfeApi;
import com.google.android.finsky.protos.Details;
import com.google.android.finsky.protos.DocumentV2;

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

    public Details.DiscoveryBadge[] getDiscoveryBadges() {
        if (this.mDetailsResponse == null) {
            return null;
        }
        return this.mDetailsResponse.discoveryBadge;
    }
    
    public Document getDocument() {
        if (this.mDetailsResponse == null || this.mDetailsResponse.docV2 == null) {
            return null;
        }
        return new Document(this.mDetailsResponse.docV2);
    }
    
    public String getFooterHtml() {
        if (this.mDetailsResponse == null || this.mDetailsResponse.footerHtml.length() == 0) {
            return null;
        }
        return this.mDetailsResponse.footerHtml;
    }
    
    public byte[] getServerLogsCookie() {
        if (this.mDetailsResponse == null || this.mDetailsResponse.serverLogsCookie.length == 0) {
            return null;
        }
        return this.mDetailsResponse.serverLogsCookie;
    }
    
    public DocumentV2.Review getUserReview() {
        if (this.mDetailsResponse == null || this.mDetailsResponse.userReview == null) {
            return null;
        }
        return this.mDetailsResponse.userReview;
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
