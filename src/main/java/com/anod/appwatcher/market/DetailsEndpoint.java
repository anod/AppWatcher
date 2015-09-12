package com.anod.appwatcher.market;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.finsky.api.model.DfeDetails;
import com.google.android.finsky.api.model.DfeModel;
import com.google.android.finsky.protos.DocDetails;

/**
 * @author alex
 * @date 2015-02-22
 */
public class DetailsEndpoint extends PlayStoreEndpoint {
    private String mUrl;

    public DetailsEndpoint(Context context) {
        super(context);
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public DfeDetails getData() {
        return (DfeDetails)mDfeModel;
    }

    public DocDetails.AppDetails getAppDetails() {
        return getData().getDocument().getAppDetails();
    }

    public String getRecentChanges() {
        DocDetails.AppDetails details = getAppDetails();
        if (details != null) {
            return details.recentChangesHtml;
        }
        return null;
    }
    @Override
    protected void executeAsync() {
        getData().setDetailsUrl(mUrl);
        getData().startAsync();
    }

    @Override
    protected void executeSync() {

        getData().setDetailsUrl(mUrl);
        getData().startSync();
    }

    @Override
    protected DfeModel createDfeModel() {
        return new DfeDetails(this.mDfeApi);
    }

}
