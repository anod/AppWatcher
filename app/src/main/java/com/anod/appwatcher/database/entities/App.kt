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
    val packageInfo = this.getPackageInfoOrNull(packageName)
            ?: return App.fromLocalPackage(rowId, packageName, 0, 0, "", "", null)
    val launchComponent = this.getLaunchComponent(packageName)
    val appTitle = this.getAppTitle(packageInfo)
    return App.fromLocalPackage(rowId, packageName, packageInfo.lastUpdateTime, packageInfo.versionCode, packageInfo.versionName ?: "", appTitle, launchComponent)
}

@Entity(tableName = AppListTable.table)
data class App(
        @PrimaryKey
        @ColumnInfo(name = BaseColumns._ID)
        val rowId: Int,

        @ColumnInfo(name = AppListTable.Columns.appId)
        val appId: String,

        @ColumnInfo(name = AppListTable.Columns.packageName)
        val packageName: String,

        @ColumnInfo(name = AppListTable.Columns.versionNumber)
        val versionNumber: Int,

        @ColumnInfo(name = AppListTable.Columns.versionName)
        val versionName: String,

        @ColumnInfo(name = AppListTable.Columns.title)
        val title: String,

        @ColumnInfo(name = AppListTable.Columns.creator)
        val creator: String,

        @ColumnInfo(name = AppListTable.Columns.iconUrl)
        val iconUrl: String,

        @ColumnInfo(name = AppListTable.Columns.status)
        val status: Int,

        @ColumnInfo(name = AppListTable.Columns.uploadDate)
        val uploadDate: String,

        @Embedded
        val price: Price,

        @ColumnInfo(name = AppListTable.Columns.detailsUrl)
        val detailsUrl: String?,

        @ColumnInfo(name = AppListTable.Columns.uploadTimestamp)
        val uploadTime: Long,

        @ColumnInfo(name = AppListTable.Columns.appType)
        val appType: String,

        @ColumnInfo(name = AppListTable.Columns.updateTimestamp)
        val updateTime: Long,

        @Ignore
        val recentFlag: Boolean
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
        updateTime: Long,
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
        updateTime = updateTime,
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
        uploadTime: Long
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
        updateTime = 0,
        recentFlag = false
    )

    constructor(doc: Document, uploadDateParserCache: UploadDateParserCache) : this(
        rowId = 0,
        status = STATUS_NORMAL,
        doc = doc,
        uploadDateParserCache = uploadDateParserCache
    )

    constructor(rowId: Int, status: Int, doc: Document, uploadDateParserCache: UploadDateParserCache) : this(
        rowId = rowId,
        appId = doc.docId,
        status = status,
        detailsUrl = doc.detailsUrl,
        packageName = doc.appDetails.packageName ?: "",
        title = doc.title,
        versionNumber = doc.appDetails.versionCode,
        versionName = doc.appDetails.versionString ?: "",
        creator = doc.appDetails.developerName ?: "",
        uploadDate = doc.appDetails.uploadDate ?: "",
        appType = doc.appDetails.appType ?: "",
        price = doc.offer.let { offer ->
            Price(
                text = offer.formattedAmount ?: "",
                cur = offer.currencyCode ?: "",
                micros = offer.micros.toInt()
            )
        },
        iconUrl = doc.iconUrl ?: "",
        uploadTime = doc.extractUploadDate(uploadDateParserCache),
        updateTime = System.currentTimeMillis(),
        recentFlag = true
    )

    companion object {
        const val STATUS_NORMAL = 0
        const val STATUS_UPDATED = 1
        const val STATUS_DELETED = 2

        fun fromInstalledPackage(rowId: Int, installed: InstalledPackageApp): App = fromLocalPackage(
                rowId,
                installed.pkg.name, installed.pkg.updateTime, installed.pkg.versionCode, installed.pkg.versionName,
                installed.title, installed.launchComponent
        )

        fun fromLocalPackage(rowId: Int, packageName: String, uploadTime: Long, versionCode: Int, versionName: String, appTitle: String, launchComponent: ComponentName?): App {
            val iconUrl: String = if (launchComponent != null) {
                Uri.fromParts(RealAppIconLoader.SCHEME, launchComponent.flattenToShortString(), null).toString()
            } else {
                Uri.fromParts(RealAppIconLoader.SCHEME, ComponentName(packageName, packageName).flattenToShortString(), null).toString()
            }

            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
            val lastUpdate = dateFormat.format(Date(uploadTime))

            return App(rowId,
                    packageName, versionCode, versionName,
                    appTitle, iconUrl, STATUS_DELETED,
                    lastUpdate, uploadTime
            )
        }

        fun createDetailsUrl(packageName: String): String {
            return "details?doc=$packageName"
        }
    }
}