package com.anod.appwatcher.database.entities

import android.content.ComponentName
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.utils.RealAppIconLoader
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.utils.extractUploadDate
import finsky.api.Document
import info.anodsplace.framework.content.InstalledPackageApp
import info.anodsplace.framework.content.getAppTitle
import info.anodsplace.framework.content.getLaunchComponent
import info.anodsplace.framework.content.getPackageInfoOrNull
import java.text.DateFormat
import java.util.Date

/**
 * @author Alex Gavrishev
 * @date 21/05/2018
 */

fun PackageManager.packageToApp(rowId: Int, packageName: String): App {
    val packageInfo = this.getPackageInfoOrNull(packageName) ?: return App.fromLocalPackage(rowId, packageName, 0, 0, "", "", null)
    val launchComponent = this.getLaunchComponent(packageName)
    val appTitle = this.getAppTitle(packageInfo)
    return App.fromLocalPackage(rowId, packageName, packageInfo.lastUpdateTime, packageInfo.versionCode, packageInfo.versionName ?: "", appTitle, launchComponent)
}

@Entity(tableName = AppListTable.TABLE)
data class App(
    @PrimaryKey @ColumnInfo(name = BaseColumns._ID) val rowId: Int,

    @ColumnInfo(name = AppListTable.Columns.APP_ID) val appId: String,

    @ColumnInfo(name = AppListTable.Columns.PACKAGE_NAME) val packageName: String,

    @ColumnInfo(name = AppListTable.Columns.VERSION_NUMBER) val versionNumber: Int,

    @ColumnInfo(name = AppListTable.Columns.VERSION_NAME) val versionName: String,

    @ColumnInfo(name = AppListTable.Columns.TITLE) val title: String,

    @ColumnInfo(name = AppListTable.Columns.CREATOR) val creator: String,

    @ColumnInfo(name = AppListTable.Columns.ICON_URL) val iconUrl: String,

    @ColumnInfo(name = AppListTable.Columns.STATUS) val status: Int,

    @ColumnInfo(name = AppListTable.Columns.UPLOAD_DATE) val uploadDate: String,

    @Embedded val price: Price,

    @ColumnInfo(name = AppListTable.Columns.DETAILS_URL) val detailsUrl: String?,

    @ColumnInfo(name = AppListTable.Columns.UPLOAD_TIMESTAMP) val uploadTime: Long,

    @ColumnInfo(name = AppListTable.Columns.APP_TYPE) val appType: String,

    @ColumnInfo(name = AppListTable.Columns.SYNC_TIMESTAMP) val syncTime: Long,

    @Ignore val recentFlag: Boolean
) {
    constructor(
        rowId: Int,
        appId: String,
        packageName: String,
        versionNumber: Int,
        versionName: String,
        title: String,
        creator: String,
        iconUrl: String,
        status: Int,
        uploadDate: String,
        price: Price,
        detailsUrl: String?,
        uploadTime: Long,
        appType: String,
        syncTime: Long,
    ) : this(
        rowId = rowId,
        appId = appId,
        packageName = packageName,
        versionNumber = versionNumber,
        versionName = versionName,
        title = title,
        creator = creator,
        iconUrl = iconUrl,
        status = status,
        uploadDate = uploadDate,
        price = price,
        detailsUrl = detailsUrl,
        uploadTime = uploadTime,
        appType = appType,
        syncTime = syncTime,
        recentFlag = false
    )

    private constructor(
        rowId: Int,
        packageName: String,
        versionCode: Int,
        versionName: String,
        title: String,
        iconUrl: String,
        status: Int,
        uploadDate: String,
        uploadTime: Long,
        syncTime: Long
    ) : this(
        rowId = rowId,
        appId = packageName,
        packageName = packageName,
        versionNumber = versionCode,
        versionName = versionName,
        title = title,
        creator = "",
        iconUrl = iconUrl,
        status = status,
        uploadDate = uploadDate,
        price = Price("", "", 0),
        detailsUrl = createDetailsUrl(packageName),
        uploadTime = uploadTime,
        appType = "",
        syncTime = syncTime,
        recentFlag = false
    )

    constructor(doc: Document, uploadDateParserCache: UploadDateParserCache) : this(doc, doc.extractUploadDate(uploadDateParserCache))

    private constructor(doc: Document, parsedUploadTime: Long) : this(
        rowId = -1,
        status = STATUS_NORMAL,
        doc = doc,
        uploadTime = parsedUploadTime,
        syncTime = if (parsedUploadTime > 0) parsedUploadTime else System.currentTimeMillis()
    )

    constructor(rowId: Int, status: Int, doc: Document, uploadTime: Long, syncTime: Long) : this(
        rowId = rowId,
        appId = doc.docId,
        status = status,
        detailsUrl = doc.detailsUrl,
        packageName = doc.appDetails.packageName ?: "",
        title = doc.title,
        versionNumber = doc.appDetails.versionCode,
        versionName = doc.appDetails.versionString ?: "",
        creator = if (doc.appDetails.developerName.isNullOrBlank()) doc.creator else doc.appDetails.developerName,
        uploadDate = doc.appDetails.uploadDate ?: "",
        appType = doc.appDetails.appType ?: "",
        price = doc.offer.let { offer ->
            Price(
                text = offer.formattedAmount ?: "", cur = offer.currencyCode ?: "", micros = offer.micros.toInt()
            )
        },
        iconUrl = doc.iconUrl ?: "",
        uploadTime = uploadTime,
        syncTime = syncTime,
        recentFlag = true
    )

    companion object {
        const val STATUS_NORMAL = 0
        const val STATUS_UPDATED = 1
        const val STATUS_DELETED = 2

        fun fromInstalledPackage(rowId: Int, installed: InstalledPackageApp): App = fromLocalPackage(
            rowId, installed.pkg.name, installed.pkg.updateTime, installed.pkg.versionCode, installed.pkg.versionName, installed.title, installed.launchComponent
        )

        fun fromLocalPackage(
            rowId: Int,
            packageName: String,
            uploadTime: Long,
            versionCode: Int,
            versionName: String,
            appTitle: String,
            launchComponent: ComponentName?
        ): App {
            val iconUrl: String = if (launchComponent != null) {
                Uri.fromParts(RealAppIconLoader.SCHEME, launchComponent.flattenToShortString(), null).toString()
            } else {
                Uri.fromParts(RealAppIconLoader.SCHEME, ComponentName(packageName, packageName).flattenToShortString(), null).toString()
            }

            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
            val lastUpdate = dateFormat.format(Date(uploadTime))

            return App(
                rowId = rowId,
                packageName = packageName,
                versionCode = versionCode,
                versionName = versionName,
                title = appTitle,
                iconUrl = iconUrl,
                status = STATUS_DELETED,
                uploadDate = lastUpdate,
                uploadTime = uploadTime,
                syncTime = uploadTime
            )
        }

        fun createDetailsUrl(packageName: String): String {
            return "details?doc=$packageName"
        }
    }
}