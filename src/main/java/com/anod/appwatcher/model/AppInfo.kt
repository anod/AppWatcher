package com.anod.appwatcher.model

import android.content.ComponentName
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.utils.extractUploadDate
import finsky.api.model.Document
import info.anodsplace.framework.content.getAppTitle
import info.anodsplace.framework.content.getLaunchComponent
import info.anodsplace.framework.content.getPackageInfo
import java.text.DateFormat
import java.util.*

fun PackageManager.packageToApp(rowId: Int, packageName: String): AppInfo {
    val packageInfo = this.getPackageInfo(packageName, this) ?: return AppInfo.fromLocalPackage(rowId, null, packageName, "", null)
    val launchComponent = this.getLaunchComponent(packageInfo, this)
    val appTitle = this.getAppTitle(packageInfo, this)
    return AppInfo.fromLocalPackage(rowId, packageInfo, packageName, appTitle, launchComponent)
}

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
    val uploadTime: Long
    val appType: String
    val updateTime: Long
    val recentFlag: Boolean

    private constructor(rowId: Int, packageName: String, versionCode: Int, versionName: String, title: String, iconUrl: String, status: Int, uploadDate: String)
            : this(rowId, packageName, packageName, versionCode, versionName, title, null, iconUrl,
            status, uploadDate, null, null, 0, createDetailsUrl(packageName), 0, "", 0, false)

    constructor(rowId: Int, appId: String, pname: String, versionNumber: Int, versionName: String,
                title: String, creator: String?, iconUrl: String, status: Int, uploadDate: String,
                priceText: String?, priceCur: String?, priceMicros: Int?, detailsUrl: String,
                uploadTime: Long, appType: String, updateTime: Long, recentFlag: Boolean) : super(appId, status) {
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
        this.uploadTime = uploadTime
        this.appType = appType
        this.updateTime = updateTime
        this.recentFlag = recentFlag
    }

    constructor(doc: Document) : this(0, AppInfoMetadata.STATUS_NORMAL, doc)

    constructor(rowId: Int, status: Int, doc: Document) : super(doc.docId, status)
    {
        this.rowId = rowId
        this.appId = doc.docId
        this.detailsUrl = doc.detailsUrl
        val app = doc.appDetails
        this.packageName = app.packageName ?: ""
        this.title = doc.title
        this.versionNumber = app.versionCode
        this.versionName = app.versionString ?: ""
        this.creator = doc.creator
        this.uploadDate = app.uploadDate ?: ""
        this.appType = app.appType ?: ""

        val offer = doc.offer
        this.priceMicros = offer.micros.toInt()
        this.priceText = offer.formattedAmount ?: ""
        this.priceCur = offer.currencyCode ?: ""

        this.iconUrl = doc.iconUrl ?: ""
        this.uploadTime = doc.extractUploadDate()
        this.updateTime = System.currentTimeMillis()
        this.recentFlag = true
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
        uploadTime = `in`.readLong()
        appType = `in`.readString()
        updateTime = `in`.readLong()
        recentFlag = `in`.readInt() == 1
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
        dest.writeLong(uploadTime)
        dest.writeString(appType)
        dest.writeLong(updateTime)
        dest.writeInt(if (recentFlag) 1 else 0)
    }

    override fun equals(other: Any?): Boolean {
        other as? AppInfo ?: return false

        return when {
            appId != other.appId -> false
            status != other.status -> false
            packageName != other.packageName -> false
            versionNumber != other.versionNumber -> false
            versionName != other.versionName -> false
            creator != other.creator -> false
            uploadDate != other.uploadDate -> false
            priceText != other.priceText -> false
            priceCur != other.priceCur -> false
            priceMicros != other.priceMicros -> false
            detailsUrl != other.detailsUrl -> false
            iconUrl != other.iconUrl -> false
            uploadTime != other.uploadTime -> false
            appType != other.appType -> false
            updateTime != other.updateTime -> false
            else -> true
        }
    }


    companion object {

        @JvmField val CREATOR: Parcelable.Creator<AppInfo> = object : Parcelable.Creator<AppInfo> {
            override fun createFromParcel(`in`: Parcel): AppInfo {
                return AppInfo(`in`)
            }

            override fun newArray(size: Int): Array<AppInfo?> {
                return arrayOfNulls(size)
            }
        }

        fun createDetailsUrl(packageName: String): String {
            return "details?doc=$packageName"
        }

        fun fromLocalPackage(rowId: Int, packageInfo: PackageInfo?, packageName: String, appTitle: String, launchComponent: ComponentName?): AppInfo {
            if (packageInfo == null) {
                return AppInfo(rowId,
                        packageName, 0, appTitle,
                        packageName, "", AppInfoMetadata.STATUS_DELETED, ""
                )
            }
            val iconUrl: String
            if (launchComponent != null) {
                iconUrl = Uri.fromParts(PicassoAppIcon.SCHEME, launchComponent.flattenToShortString(), null).toString()
            } else {
                iconUrl = Uri.fromParts(PicassoAppIcon.SCHEME, ComponentName(packageName, packageName).flattenToShortString(), null).toString()
            }

            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
            val lastUpdate = dateFormat.format(Date(packageInfo.lastUpdateTime))
            val versionName = packageInfo.versionName ?: ""

            return AppInfo(rowId,
                    packageInfo.packageName, packageInfo.versionCode, versionName,
                    appTitle, iconUrl, AppInfoMetadata.STATUS_NORMAL, lastUpdate
            )
        }
    }
}
