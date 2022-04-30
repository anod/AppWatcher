package com.anod.appwatcher.model

import android.os.Parcel
import android.os.Parcelable
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.utils.extractUploadDate
import finsky.api.model.Document

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

    constructor(doc: Document, uploadDateParserCache: UploadDateParserCache) : this(0, STATUS_NORMAL, doc, uploadDateParserCache)

    constructor(rowId: Int, status: Int, doc: Document, uploadDateParserCache: UploadDateParserCache) : super(doc.docId, status) {
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
        this.uploadTime = doc.extractUploadDate(uploadDateParserCache)
        this.updateTime = System.currentTimeMillis()
        this.recentFlag = true
    }

     constructor(`in`: Parcel) : super(`in`.readString()!!, `in`.readInt()) {
        rowId = `in`.readInt()
        packageName = `in`.readString()!!
        versionNumber = `in`.readInt()
        versionName = `in`.readString()!!
        title = `in`.readString()!!
        creator = `in`.readString()!!
        uploadDate = `in`.readString()!!

        priceText = `in`.readString()!!
        priceCur = `in`.readString()!!
        priceMicros = `in`.readInt()
        detailsUrl = `in`.readString()

        iconUrl = `in`.readString()!!
        uploadTime = `in`.readLong()
        appType = `in`.readString()!!
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
        if (other !is AppInfo) return false

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

    override fun hashCode(): Int {
        var result = appId.hashCode()
        result = 31 * result + (detailsUrl?.hashCode() ?: 0)
        result = 31 * result + packageName.hashCode()
        result = 31 * result + versionNumber
        result = 31 * result + versionName.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + creator.hashCode()
        result = 31 * result + uploadDate.hashCode()
        result = 31 * result + priceText.hashCode()
        result = 31 * result + priceCur.hashCode()
        result = 31 * result + (priceMicros ?: 0)
        result = 31 * result + iconUrl.hashCode()
        result = 31 * result + uploadTime.hashCode()
        result = 31 * result + appType.hashCode()
        result = 31 * result + updateTime.hashCode()
        result = 31 * result + recentFlag.hashCode()
        return result
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
    }
}