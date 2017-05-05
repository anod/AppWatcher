package com.anod.appwatcher.model;

import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.anod.appwatcher.utils.AppDetailsUploadDate;
import com.anod.appwatcher.utils.AppIconLoader;
import com.google.android.finsky.api.model.Document;
import com.google.android.finsky.protos.nano.Messages.Common;

import java.text.DateFormat;
import java.util.Date;


public class AppInfo extends AppInfoMetadata implements Parcelable{

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    public static String createDetailsUrl(String packageName)
    {
        return "details?doc="+packageName;
    }

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
        String iconUrl;
        if (launchComponent != null) {
            iconUrl = Uri.fromParts(AppIconLoader.SCHEME, launchComponent.flattenToShortString(), null).toString();
        } else {
            iconUrl = Uri.fromParts(AppIconLoader.SCHEME, new ComponentName(packageName, packageName).flattenToShortString() , null).toString();
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
                status, uploadDate, null, null, 0, createDetailsUrl(packageName), 0, "", 0);
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
        com.google.android.finsky.protos.nano.Messages.AppDetails app = doc.getAppDetails();
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
        this.refreshTime = AppDetailsUploadDate.extract(doc);
        this.syncVersion = 0;
    }

    protected AppInfo(Parcel in) {
        super(in.readString(), in.readInt());
        rowId = in.readInt();
        packageName = in.readString();
        versionNumber = in.readInt();
        versionName = in.readString();
        title = in.readString();
        creator = in.readString();
        uploadDate = in.readString();

        priceText = in.readString();
        priceCur = in.readString();
        priceMicros = in.readInt();
        detailsUrl = in.readString();

        iconUrl = in.readString();
        refreshTime = in.readLong();
        appType = in.readString();
        syncVersion = in.readInt();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appId);
        dest.writeInt(status);
        dest.writeInt(rowId);
        dest.writeString(packageName);
        dest.writeInt(versionNumber);
        dest.writeString(versionName);
        dest.writeString(title);
        dest.writeString(creator);
        dest.writeString(uploadDate);

        dest.writeString(priceText);
        dest.writeString(priceCur);
        dest.writeInt(priceMicros);
        dest.writeString(detailsUrl);

        dest.writeString(iconUrl);
        dest.writeLong(refreshTime);
        dest.writeString(appType);
        dest.writeInt(syncVersion);
    }
}
