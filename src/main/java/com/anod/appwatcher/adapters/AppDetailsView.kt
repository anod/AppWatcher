package com.anod.appwatcher.adapters

import android.support.annotation.ColorInt
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.anod.appwatcher.R
import com.anod.appwatcher.content.AppChangeContentProviderClient
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.utils.Html

/**
 * @author algavris
 * @date 14/05/2016.
 */
class AppDetailsView(view: View, private val dataProvider: AppViewHolderBase.DataProvider) {
    private val textColor: Int
    @ColorInt private var accentColor: Int = 0

    val title: TextView = view.findViewById<TextView>(android.R.id.title)
    val details: TextView? = view.findViewById<TextView?>(R.id.details)
    val version: TextView? = view.findViewById<TextView?>(R.id.updated)
    val price: TextView? = view.findViewById<TextView?>(R.id.price)
    val updateDate: TextView? = view.findViewById<TextView?>(R.id.update_date)
    val recentChanges = view.findViewById<TextView?>(R.id.recent_changes)

    init {
        accentColor = dataProvider.getColor(R.color.theme_accent)
        textColor = dataProvider.getColor(R.color.primary_text)
    }

    fun fillDetails(app: AppInfo, isLocalApp: Boolean) {
        title.text = app.title
        details?.text = app.creator
        val uploadDate = app.uploadDate

        if (TextUtils.isEmpty(uploadDate)) {
            updateDate?.visibility = View.GONE
        } else {
            updateDate?.text = uploadDate
            updateDate?.visibility = View.VISIBLE
        }

        if (isLocalApp) {
            this.price?.visibility = View.GONE
            this.recentChanges?.visibility = View.GONE
            this.version?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0, 0, 0)
            this.version?.text = dataProvider.formatVersionText(app.versionName, app.versionNumber)
        } else {
            this.fillWatchAppView(app)
        }
    }

    private fun fillWatchAppView(app: AppInfo) {

        val isInstalled = dataProvider.installedAppsProvider.getInfo(app.packageName).isInstalled
        version?.text = dataProvider.formatVersionText(app.versionName, app.versionNumber)
        if (app.status == AppInfoMetadata.STATUS_UPDATED) {
            version?.setTextColor(accentColor)
            this.recentChanges?.visibility = View.VISIBLE
            val appChange = dataProvider.appChangeContentProvider.query(app.appId, app.versionNumber)
            this.recentChanges?.text = if (appChange?.details?.isBlank() != false) dataProvider.noRecentChangesText else Html.parse(appChange.details)
        } else {
            this.recentChanges?.visibility = View.GONE
            version?.setTextColor(textColor)
        }

        price?.setTextColor(accentColor)
        if (isInstalled) {
            val installed = dataProvider.installedAppsProvider.getInfo(app.packageName)
            price?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0, 0, 0)
            if (TextUtils.isEmpty(installed.versionName)) {
                price?.text = dataProvider.installedText
            } else {
                price?.text = dataProvider.formatVersionText(installed.versionName, installed.versionCode)
            }
            if (app.versionNumber > installed.versionCode) {
                version?.setTextColor(dataProvider.getColor(R.color.material_amber_800))
            } else {
                version?.setTextColor(textColor)
            }
        } else {
            price?.setCompoundDrawables(null, null, null, null)
            if (app.priceMicros == 0) {
                price?.setText(R.string.free)
            } else {
                price?.text = app.priceText
            }
        }
    }

    fun updateAccentColor(@ColorInt color: Int, app: AppInfo) {
        accentColor = color
        price!!.setTextColor(accentColor)
        if (app.status == AppInfoMetadata.STATUS_UPDATED) {
            version!!.setTextColor(accentColor)
        }
    }
}
