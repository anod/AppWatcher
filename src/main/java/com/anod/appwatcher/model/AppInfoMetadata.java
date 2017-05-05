package com.anod.appwatcher.model;

/**
 * @author alex
 * @date 2015-02-27
 */
public class AppInfoMetadata {

    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_UPDATED = 1;
    public static final int STATUS_DELETED = 2;

    String appId;
    protected int status;

    AppInfoMetadata(String appId, int status) {
        this.appId = appId;
        this.status = status;
    }

    public String getAppId() {
        return appId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

}
