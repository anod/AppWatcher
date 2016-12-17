package com.anod.appwatcher.model;

import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.anod.appwatcher.utils.AppIconLoader;
import com.anod.appwatcher.utils.DocUtils;
import com.google.android.finsky.api.model.Document;
import com.google.android.finsky.protos.nano.Messages.Common;
import com.google.android.finsky.protos.nano.Messages.AppDetails;

import java.text.DateFormat;
import java.util.Date;


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
    public final String appType;
    public final int syncVersion;

    public static AppInfo fromLocalPackage(@Nullable PackageInfo packageInfo, @NonNull String packageName, String appTitle, @Nullable ComponentName launchComponent)
    {
        if (packageInfo == null) {
            return new AppInfo(
                    packageName, 0, appTitle,
                    packageName, null, AppInfoMetadata.STATUS_DELETED, ""
            );
        }
        String iconUrl = null;
        if (launchComponent != null) {
            iconUrl = Uri.fromParts(AppIconLoader.SCHEME, launchComponent.flattenToShortString(), null).toString();
        }

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        String lastUpdate = dateFormat.format(new Date(packageInfo.lastUpdateTime));

        return new AppInfo(
                packageInfo.packageName, packageInfo.versionCode, packageInfo.versionName,
                appTitle, iconUrl, AppInfoMetadata.STATUS_NORMAL, lastUpdate
        );
    }

    private AppInfo(String packageName, int versionCode, String versionName, String title, String iconUrl, int status, String uploadDate) {
        this(-1, packageName, packageName, versionCode, versionName, title, null, iconUrl,
                status, uploadDate, null, null, 0, "details?doc=" + packageName, 0, "", 0);
    }

    public AppInfo(int rowId, String appId, String pname, int versionNumber, String versionName,
                   String title, String creator, String iconUrl, int status, String uploadDate,
                   String priceText, String priceCur, Integer priceMicros, String detailsUrl,
                   long refreshTime, String appType, int syncVersion) {
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
        this.appType = appType;
        this.syncVersion = syncVersion;
    }

    public AppInfo(Document doc) {
        super(doc.getDocId(), STATUS_NORMAL);
        this.rowId = 0;
        this.appId = doc.getDocId();
        this.detailsUrl = doc.getDetailsUrl();
        AppDetails app = doc.getAppDetails();
        this.packageName = app.packageName;
        this.title = doc.getTitle();
        this.versionNumber = app.versionCode;
        this.versionName = app.versionString;
        this.creator = doc.getCreator();
        this.uploadDate = app.uploadDate;
        this.appType = app.appType;

        Common.Offer offer = doc.getOffer();
        this.priceMicros = (int) offer.micros;
        this.priceText = offer.formattedAmount;
        this.priceCur = offer.currencyCode;

        this.iconUrl = doc.getIconUrl();
        this.refreshTime = DocUtils.extractDate(doc);
        this.syncVersion = 0;
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
