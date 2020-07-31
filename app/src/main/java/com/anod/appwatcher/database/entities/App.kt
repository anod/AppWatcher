package com.anod.appwatcher.database.entities

import android.content.ComponentName
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.provider.BaseColumns
import androidx.room.*
import com.anod.appwatcher.R
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.utils.PicassoAppIcon
import info.anodsplace.framework.content.getAppTitle
import info.anodsplace.framework.content.getLaunchComponent
import info.anodsplace.framework.content.getPackageInfo
import java.text.DateFormat
import java.util.*

/**
 * @author Alex Gavrishev
 * @date 21/05/2018
 */

fun PackageManager.packageToApp(rowId: Int, packageName: String): App {
    val packageInfo = this.getPackageInfo(packageName, this)
            ?: return App.fromLocalPackage(rowId, null, packageName, "", null)
    val launchComponent = this.getLaunchComponent(packageInfo, this)
    val appTitle = this.getAppTitle(packageInfo, this)
    return App.fromLocalPackage(rowId, packageInfo, packageName, appTitle, launchComponent)
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
        val updateTime: Long
) {
    @Ignore
    var testing: Int = 0

    private constructor(rowId: Int, packageName: String, versionCode: Int, versionName: String, title: String, iconUrl: String, status: Int, uploadDate: String)
            : this(rowId, packageName, packageName, versionCode, versionName, title, "", iconUrl, status,
            uploadDate, Price("", "", 0), createDetailsUrl(packageName), 0, "", 0)

    companion object {
        fun fromLocalPackage(rowId: Int, packageInfo: PackageInfo?, packageName: String, appTitle: String, launchComponent: ComponentName?): App {
            if (packageInfo == null) {
                return App(rowId,
                        packageName, 0, appTitle,
                        packageName, "", AppInfoMetadata.STATUS_DELETED, ""
                )
            }
            val iconUrl: String = if (launchComponent != null) {
                Uri.fromParts(PicassoAppIcon.SCHEME, launchComponent.flattenToShortString(), null).toString()
            } else {
                Uri.fromParts(PicassoAppIcon.SCHEME, ComponentName(packageName, packageName).flattenToShortString(), null).toString()
            }

            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
            val lastUpdate = dateFormat.format(Date(packageInfo.lastUpdateTime))
            val versionName = packageInfo.versionName ?: ""

            return App(rowId,
                    packageInfo.packageName, packageInfo.versionCode, versionName,
                    appTitle, iconUrl, AppInfoMetadata.STATUS_DELETED, lastUpdate
            )
        }

        fun createDetailsUrl(packageName: String): String {
            return "details?doc=$packageName"
        }
    }
}

fun App.generateTitle(resources: Resources): CharSequence {
    var generated = title
    if (testing != 0) {
        generated += " (" + resources.getString(R.string.beta) + ")"
    }
    return generated
}
