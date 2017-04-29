package com.anod.appwatcher.model;

/**
 * @author algavris
 * @date 27/04/2017.
 */

public class AppTag {
    public final String appId;
    public final int tagId;

    public AppTag(String appId, int tagId) {
        this.appId = appId;
        this.tagId = tagId;
    }
}
