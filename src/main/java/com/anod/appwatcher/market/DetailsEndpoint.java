package com.anod.appwatcher.market;

import android.content.Context;

import com.google.android.finsky.api.model.DfeDetails;
import com.google.android.finsky.api.model.DfeModel;
import com.google.android.finsky.api.model.Document;
import com.google.android.finsky.protos.DocDetails;

/**
 * @author alex
 * @date 2015-02-22
 */
public class DetailsEndpoint extends PlayStoreEndpoint {
    private String mUrl;
    private String mAppId;

    public DetailsEndpoint(String deviceId, Listener listener, Context context) {
        super(deviceId, listener, context);
    }

    public void setAppId(String appId) {
        mAppId = appId;
    }

    public DfeDetails getData() {
        return (DfeDetails)mDfeModel;
    }

    public DocDetails.AppDetails getAppDetails() {
        return getData().getDocument().getAppDetails();
    }

    @Override
    protected void execute() {
        getData().start();
    }

    @Override
    protected DfeModel createDfeModel() {
        return new DfeDetails(this.mDfeApi, "details?doc="+mAppId);
    }

    /*
    public App loadOne(String appId) {
        List<App> apps = load(appId);
        if (apps == null || apps.size() == 0) {
            return null;
        }
        return apps.get(0);
    }
    public String loadRecentChanges(String appId) {
        App app = loadOne(appId);
        String changes = "";
        if (app.getExtendedInfo() != null) {
            changes = app.getExtendedInfo().getRecentChanges();
        }
        return changes;
    }
    */

    public String loadRecentChanges(String appId) {
        return null;
    }


    public Document loadOne(String appId) {
        return null;
    }
}
