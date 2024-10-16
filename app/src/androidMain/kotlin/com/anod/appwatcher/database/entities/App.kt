package com.anod.appwatcher.database.entities

import android.content.ContentValues
import android.content.pm.PackageManager
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.BaseColumns
import com.anod.appwatcher.utils.date.UploadDateParserCache
import com.anod.appwatcher.utils.extractUploadDate
import com.anod.appwatcher.utils.packageNameToIconUrl
import com.anod.appwatcher.utils.toIconUrl
import finsky.api.Document
import info.anodsplace.framework.content.InstalledPackageApp
import info.anodsplace.framework.content.getAppTitle
import info.anodsplace.framework.content.getLaunchComponent
import info.anodsplace.framework.content.getPackageInfoOrNull
import java.text.DateFormat
import java.util.Date

fun PackageManager.packageToApp(rowId: Int, packageName: String): App {
    val packageInfo = this.getPackageInfoOrNull(packageName) ?: return fromLocalPackage(
        rowId = rowId,
        packageName = packageName,
        uploadTime = 0,
        versionCode = 0,
        versionName = "",
        appTitle = "",
        iconUrl = packageName.packageNameToIconUrl()
    )
    val launchComponent = this.getLaunchComponent(packageName)
    val appTitle = this.getAppTitle(packageInfo)
    val iconUrl = launchComponent?.toIconUrl() ?: packageName.packageNameToIconUrl()
    return fromLocalPackage(
        rowId = rowId,
        packageName = packageName,
        uploadTime = packageInfo.lastUpdateTime,
        versionCode = packageInfo.versionCode,
        versionName = packageInfo.versionName ?: "",
        appTitle = appTitle,
        iconUrl = iconUrl
    )
}

fun App(doc: Document, uploadDateParserCache: UploadDateParserCache): App = doc.extractUploadDate(uploadDateParserCache).let { parsedUploadTime ->
    App(
        rowId = 0,
        status = App.STATUS_NORMAL,
        doc = doc,
        uploadTime = parsedUploadTime,
        syncTime = if (parsedUploadTime > 0) parsedUploadTime else System.currentTimeMillis()
    )
}

 fun App(rowId: Int, installed: InstalledPackageApp): App = fromLocalPackage(
     rowId = rowId,
     packageName = installed.pkg.name,
     uploadTime = installed.pkg.updateTime,
     versionCode = installed.pkg.versionCode,
     versionName = installed.pkg.versionName,
     appTitle = installed.title,
     iconUrl = installed.launchComponent?.toIconUrl() ?: installed.pkg.name.packageNameToIconUrl()
)

fun App(rowId: Int, status: Int, doc: Document, uploadTime: Long, syncTime: Long) = App(
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
    syncTime = syncTime
)

fun fromLocalPackage(rowId: Int, packageName: String, uploadTime: Long, versionCode: Int, versionName: String, appTitle: String, iconUrl: String): App {
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
    val lastUpdate = dateFormat.format(Date(uploadTime))

    return App(
        rowId = rowId,
        packageName = packageName,
        versionCode = versionCode,
        versionName = versionName,
        title = appTitle,
        iconUrl = iconUrl,
        status = App.STATUS_DELETED,
        uploadDate = lastUpdate,
        uploadTime = uploadTime,
        syncTime = uploadTime
    )
}

val App.contentValues: ContentValues
    get() = ContentValues().apply {
        if (rowId > 0) {
            put(BaseColumns._ID, rowId)
        }
        put(AppListTable.Columns.appId, appId)
        put(AppListTable.Columns.packageName, packageName)
        put(AppListTable.Columns.title, title)
        put(AppListTable.Columns.versionNumber, versionNumber)
        put(AppListTable.Columns.versionName, versionName)
        put(AppListTable.Columns.creator, creator)
        put(AppListTable.Columns.status, status)
        put(AppListTable.Columns.uploadDate, uploadDate)

        put(AppListTable.Columns.priceText, price.text)
        put(AppListTable.Columns.priceCurrency, price.cur)
        put(AppListTable.Columns.priceMicros, price.micros)

        put(AppListTable.Columns.detailsUrl, detailsUrl)

        put(AppListTable.Columns.iconUrl, iconUrl)
        put(AppListTable.Columns.uploadTimestamp, uploadTime)

        put(AppListTable.Columns.appType, appType)
        put(AppListTable.Columns.syncTimestamp, syncTime)
    }