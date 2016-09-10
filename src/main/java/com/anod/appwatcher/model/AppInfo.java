package com.anod.appwatcher.model;

import android.content.Context;

import com.anod.appwatcher.utils.DocUtils;
import com.google.android.finsky.api.model.Document;
import com.google.android.finsky.protos.Common;
import com.google.android.finsky.protos.DocDetails;


public class AppInfo extends AppInfoMetadata {

    private int rowId;
    private String detailsUrl;

    public final String packageName;
    public final int versionNumber;
    public final String versionName;
    public final String title;
    public final String creator;
    public final String uploadDate;
    public final String priceText;
    public final String priceCur;
    public final Integer priceMicros;
    public final String iconUrl;
    public final long refreshTime;

    public AppInfo(String packageName, int versionCode, String versionName, String title, String iconUrl, int status, String uploadDate) {
        this(-1, packageName, packageName, versionCode, versionName, title, null, iconUrl,
                status, uploadDate, null, null, 0, "details?doc=" + packageName, 0);
    }

    public AppInfo(int rowId, String appId, String pname, int versionNumber, String versionName,
                   String title, String creator, String iconUrl, int status, String uploadDate, String priceText, String priceCur, Integer priceMicros, String detailsUrl, long refreshTime) {
        super(appId, status);
        this.rowId = rowId;
        this.packageName = pname;
        this.versionNumber = versionNumber;
        this.versionName = versionName;
        this.title = title;
        this.creator = creator;
        this.uploadDate = uploadDate;

        this.priceText = priceText;
        this.priceCur = priceCur;
        this.priceMicros = priceMicros;
        this.detailsUrl = detailsUrl;

        this.iconUrl = iconUrl;
        this.refreshTime = refreshTime;
    }

    public AppInfo(Document doc) {
        super(doc.getDocId(), STATUS_NORMAL);
        this.rowId = 0;
        this.appId = doc.getDocId();
        this.detailsUrl = doc.getDetailsUrl();
        DocDetails.AppDetails app = doc.getAppDetails();
        this.packageName = app.packageName;
        this.title = doc.getTitle();
        this.versionNumber = app.versionCode;
        this.versionName = app.versionString;
        this.creator = doc.getCreator();
        this.uploadDate = app.uploadDate;
        Common.Offer offer = DocUtils.getOffer(doc);
        this.priceMicros = (int) offer.micros;
        this.priceText = offer.formattedAmount;
        this.priceCur = offer.currencyCode;

        this.iconUrl = DocUtils.getIconUrl(doc);
        this.refreshTime = DocUtils.extractDate(doc);
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }

    public String getDetailsUrl() {
        return this.detailsUrl;
    }

}
