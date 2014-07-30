package com.anod.appwatcher.model;

/**
 * Created by alex on 7/13/14.
 */
public class AppInfoMetadata {

    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_UPDATED = 1;
    public static final int STATUS_DELETED = 2;

    protected int status;
    protected long modifyTime; // when the item was inserted or deleted
    protected String appId;

    public AppInfoMetadata(String appId, int status) {
        this.appId = appId;
        this.status = status;
    }

    public String getAppId() {
        return appId;
    }

    public int getStatus() {
        return status;
    }

    public long getModifyTime() {
        return modifyTime;
    }
    /**
     * @param status
     */
    public void setStatus(int status) {
        this.status = status;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }
}
