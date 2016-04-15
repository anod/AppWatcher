package com.anod.appwatcher.model;

import android.graphics.Bitmap;

import com.anod.appwatcher.utils.DocUtils;
import com.google.android.finsky.api.model.Document;
import com.google.android.finsky.protos.Common;
import com.google.android.finsky.protos.DocDetails;


public class AppInfo extends AppInfoMetadata {

    private int rowId;
    private String packageName;
    private int versionNumber;
    private String versionName;
    private String title;
    private String creator;
    private Bitmap icon;
    private String uploadDate;
    private String priceText;
    private String priceCur;
    private Integer priceMicros;
    private String detailsUrl;

    public AppInfo(int rowId, String appId, String pname, int versionNumber, String versionName,
                   String title, String creator, Bitmap icon, int status, String uploadDate, String priceText, String priceCur, Integer priceMicros, String detailsUrl) {
        super(appId, status);
        this.rowId = rowId;
        this.packageName = pname;
        this.versionNumber = versionNumber;
        this.versionName = versionName;
        this.title = title;
        this.creator = creator;
        this.icon = icon;
        this.uploadDate = uploadDate;

        this.priceText = priceText;
        this.priceCur = priceCur;
        this.priceMicros = priceMicros;
        this.detailsUrl = detailsUrl;
    }

    public AppInfo(Document doc, Bitmap icon) {
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
        this.icon = icon;
        Common.Offer offer = DocUtils.getOffer(doc);
        this.priceMicros = (int) offer.micros;
        this.priceText = offer.formattedAmount;
        this.priceCur = offer.currencyCode;
    }

    public int getRowId() {
        return rowId;
    }

    /**
     * @return the packageName
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @return the versionNumber
     */
    public int getVersionCode() {
        return versionNumber;
    }

    /**
     * @return the versionName
     */
    public String getVersionName() {
        return versionName;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * @return the icon
     */
    public Bitmap getIcon() {
        return icon;
    }

    /**
     * @return the last update time
     */
    public String getUploadDate() {
        return uploadDate;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }


    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getPriceText() {
        return priceText;
    }

    public String getPriceCur() {
        return priceCur;
    }

    public Integer getPriceMicros() {
        return priceMicros;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }
}
