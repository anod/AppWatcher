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
import kotlinx.serialization.Serializable

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

fun Document.toApp(uploadDateParserCache: UploadDateParserCache): App = toApp(extractUploadDate(uploadDateParserCache))

/**
 * Play's update-purpose bulk-details response (`?au=1`) can omit optional metadata fields
 * such as title, creator, icon, details URL, version name, upload date, app type and price
 * for documents that are unchanged. Restore the previously cached values for any field that
 * came back blank so a sparse response doesn't clobber metadata that a details screen or
 * icon loader depends on. The version number is intentionally never preserved from cache:
 * this function is only applied when a genuinely newer version was detected.
 */
fun App.preserveCachedMetadata(cached: App): App = copy(
    title = title.ifBlank { cached.title },
    creator = creator.ifBlank { cached.creator },
    iconUrl = iconUrl.ifBlank { cached.iconUrl },
    detailsUrl = if (detailsUrl.isNullOrBlank()) cached.detailsUrl else detailsUrl,
    versionName = versionName.ifBlank { cached.versionName },
    uploadDate = uploadDate.ifBlank { cached.uploadDate },
    appType = appType.ifBlank { cached.appType },
    price = if (price.text.isBlank() && price.cur.isBlank() && (price.micros == null || price.micros == 0)) cached.price else price
)

/**
 * Fills in title/creator/iconUrl/detailsUrl on this (already cached) app from a freshly
 * fetched, non-sparse [fresh] app, but only for fields that are currently blank here.
 * Used to self-heal rows that were previously clobbered by a sparse `?au=1` update-check
 * response (before [preserveCachedMetadata] existed), without touching fields that already
 * hold a value.
 */
fun App.fillBlankMetadata(fresh: App): App = copy(
    title = title.ifBlank { fresh.title },
    creator = creator.ifBlank { fresh.creator },
    iconUrl = iconUrl.ifBlank { fresh.iconUrl },
    detailsUrl = if (detailsUrl.isNullOrBlank()) fresh.detailsUrl else detailsUrl
)

private fun Document.toApp(parsedUploadTime: Long): App = toApp(
    rowId = -1,
    status = App.STATUS_NORMAL,
    uploadTime = parsedUploadTime,
    syncTime = if (parsedUploadTime > 0) parsedUploadTime else System.currentTimeMillis()
)

fun Document.toApp(
    rowId: Int,
    status: Int,
    uploadTime: Long,
    syncTime: Long
): App = App(
    rowId = rowId,
    appId = docId,
    status = status,
    detailsUrl = detailsUrl,
    packageName = appDetails.packageName ?: "",
    title = title,
    versionNumber = appDetails.versionCode,
    versionName = appDetails.versionString ?: "",
    creator = if (appDetails.developerName.isNullOrBlank()) creator else appDetails.developerName,
    uploadDate = appDetails.uploadDate ?: "",
    appType = appDetails.appType ?: "",
    price = offer.let { offer ->
        Price(
            text = offer.formattedAmount ?: "", cur = offer.currencyCode ?: "", micros = offer.micros.toInt()
        )
    },
    iconUrl = iconUrl ?: "",
    uploadTime = uploadTime,
    syncTime = syncTime,
    recentFlag = true
)

@Entity(tableName = AppListTable.TABLE)
@Serializable
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

        fun createDetailsUrl(packageName: String): String = "details?doc=$packageName"
    }
}