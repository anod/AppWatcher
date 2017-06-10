package com.anod.appwatcher.model

import android.content.ComponentName
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.anod.appwatcher.utils.AppDetailsUploadDate
import com.anod.appwatcher.utils.AppIconLoader
import com.google.android.finsky.api.model.Document
import java.text.DateFormat
import java.util.*


class AppInfo : AppInfoMetadata, Parcelable {

    var rowId: Int = 0
    var detailsUrl: String? = null

    val packageName: String
    val versionNumber: Int
    val versionName: String
    val title: String
    val creator: String
    val uploadDate: String
    val priceText: String
    val priceCur: String
    val priceMicros: Int?
    val iconUrl: String
    val refreshTime: Long
    val appType: String
    val syncVersion: Int

    private constructor(packageName: String, versionCode: Int, versionName: String, title: String, iconUrl: String, status: Int, uploadDate: String)
            : this(-1, packageName, packageName, versionCode, versionName, title, null, iconUrl,
            status, uploadDate, null, null, 0, createDetailsUrl(packageName), 0, "", 0)

    constructor(rowId: Int, appId: String, pname: String, versionNumber: Int, versionName: String,
                title: String, creator: String?, iconUrl: String, status: Int, uploadDate: String,
                priceText: String?, priceCur: String?, priceMicros: Int?, detailsUrl: String,
                refreshTime: Long, appType: String, syncVersion: Int) : super(appId, status) {
        this.rowId = rowId
        this.packageName = pname
        this.versionNumber = versionNumber
        this.versionName = versionName
        this.title = title
        this.creator = creator ?: ""
        this.uploadDate = uploadDate

        this.priceText = priceText ?: ""
        this.priceCur = priceCur ?: ""
        this.priceMicros = priceMicros
        this.detailsUrl = detailsUrl

        this.iconUrl = iconUrl
        this.refreshTime = refreshTime
        this.appType = appType
        this.syncVersion = syncVersion
    }

    constructor(doc: Document) : super(doc.docId, AppInfoMetadata.STATUS_NORMAL)
    {
        this.rowId = 0
        this.appId = doc.docId
        this.detailsUrl = doc.detailsUrl
        val app = doc.appDetails
        this.packageName = app.packageName
        this.title = doc.title
        this.versionNumber = app.versionCode
        this.versionName = app.versionString
        this.creator = doc.creator
        this.uploadDate = app.uploadDate
        this.appType = app.appType

        val offer = doc.offer
        this.priceMicros = offer.micros.toInt()
        this.priceText = offer.formattedAmount
        this.priceCur = offer.currencyCode

        this.iconUrl = doc.iconUrl
        this.refreshTime = AppDetailsUploadDate.extract(doc)
        this.syncVersion = 0
    }

     constructor(`in`: Parcel) : super(`in`.readString(), `in`.readInt()) {
        rowId = `in`.readInt()
        packageName = `in`.readString()
        versionNumber = `in`.readInt()
        versionName = `in`.readString()
        title = `in`.readString()
        creator = `in`.readString()
        uploadDate = `in`.readString()

        priceText = `in`.readString()
        priceCur = `in`.readString()
        priceMicros = `in`.readInt()
        detailsUrl = `in`.readString()

        iconUrl = `in`.readString()
        refreshTime = `in`.readLong()
        appType = `in`.readString()
        syncVersion = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(appId)
        dest.writeInt(status)
        dest.writeInt(rowId)
        dest.writeString(packageName)
        dest.writeInt(versionNumber)
        dest.writeString(versionName)
        dest.writeString(title)
        dest.writeString(creator)
        dest.writeString(uploadDate)

        dest.writeString(priceText)
        dest.writeString(priceCur)
        dest.writeInt(priceMicros!!)
        dest.writeString(detailsUrl)

        dest.writeString(iconUrl)
        dest.writeLong(refreshTime)
        dest.writeString(appType)
        dest.writeInt(syncVersion)
    }

    companion object {

        val CREATOR: Parcelable.Creator<AppInfo> = object : Parcelable.Creator<AppInfo> {
            override fun createFromParcel(`in`: Parcel): AppInfo {
                return AppInfo(`in`)
            }

            override fun newArray(size: Int): Array<AppInfo?> {
                return arrayOfNulls(size)
            }
        }

        fun createDetailsUrl(packageName: String): String {
            return "details?doc=" + packageName
        }

        fun fromLocalPackage(packageInfo: PackageInfo?, packageName: String, appTitle: String, launchComponent: ComponentName?): AppInfo {
            if (packageInfo == null) {
                return AppInfo(
                        packageName, 0, appTitle,
                        packageName, "", AppInfoMetadata.STATUS_DELETED, ""
                )
            }
            val iconUrl: String
            if (launchComponent != null) {
                iconUrl = Uri.fromParts(AppIconLoader.SCHEME, launchComponent.flattenToShortString(), null).toString()
            } else {
                iconUrl = Uri.fromParts(AppIconLoader.SCHEME, ComponentName(packageName, packageName).flattenToShortString(), null).toString()
            }

            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
            val lastUpdate = dateFormat.format(Date(packageInfo.lastUpdateTime))

            return AppInfo(
                    packageInfo.packageName, packageInfo.versionCode, packageInfo.versionName,
                    appTitle, iconUrl, AppInfoMetadata.STATUS_NORMAL, lastUpdate
            )
        }
    }
}
