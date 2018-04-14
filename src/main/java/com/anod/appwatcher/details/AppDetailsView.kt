package com.anod.appwatcher.details

import android.support.annotation.ColorInt
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.anod.appwatcher.R
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.watchlist.AppViewHolderBase
import info.anodsplace.framework.text.Html

/**
 * @author algavris
 * @date 14/05/2016.
 */
class AppDetailsView(view: View, private val resourceProvider: AppViewHolderBase.ResourceProvider) {
    @ColorInt private val textColor: Int
    @ColorInt private var accentColor: Int
    @ColorInt private var warningColor: Int

    val title: TextView = view.findViewById(android.R.id.title)
    val details: TextView? = view.findViewById<TextView?>(R.id.details)
    val version: TextView? = view.findViewById<TextView?>(R.id.updated)
    val price: TextView? = view.findViewById<TextView?>(R.id.price)
    val updateDate: TextView? = view.findViewById<TextView?>(R.id.update_date)
    val recentChanges = view.findViewById<TextView?>(R.id.recent_changes)

    init {
        accentColor = resourceProvider.getColor(R.color.theme_accent)
        textColor = resourceProvider.getColor(R.color.primary_text)
        warningColor = resourceProvider.getColor(R.color.material_amber_800)
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
            this.price?.visibility = View.INVISIBLE
            this.recentChanges?.visibility = View.GONE
            this.version?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0, 0, 0)
            this.version?.text = resourceProvider.formatVersionText(app.versionName, app.versionNumber)
        } else {
            this.fillWatchAppView(app)
        }
    }

    private fun fillWatchAppView(app: AppInfo) {

        val isInstalled = resourceProvider.installedApps.packageInfo(app.packageName).isInstalled
        when {
            app.versionNumber == 0 -> {
                version?.setTextColor(warningColor)
                version?.text = resourceProvider.getString(R.string.updates_not_available)
            }
            app.status == AppInfoMetadata.STATUS_UPDATED -> {
                version?.text = resourceProvider.formatVersionText(app.versionName, app.versionNumber)
                version?.setTextColor(accentColor)
                this.recentChanges?.visibility = View.VISIBLE
                val appChange = resourceProvider.appChangeContentProvider.query(app.appId, app.versionNumber)
                this.recentChanges?.text = if (appChange?.details?.isBlank() != false) resourceProvider.noRecentChangesText else Html.parse(appChange.details)
            }
            app.recentFlag -> {
                version?.text = resourceProvider.formatVersionText(app.versionName, app.versionNumber)
                this.recentChanges?.visibility = View.VISIBLE
                val appChange = resourceProvider.appChangeContentProvider.query(app.appId, app.versionNumber)
                this.recentChanges?.text = if (appChange?.details?.isBlank() != false) resourceProvider.noRecentChangesText else Html.parse(appChange.details)
            }
            else -> {
                version?.text = resourceProvider.formatVersionText(app.versionName, app.versionNumber)
                this.recentChanges?.visibility = View.GONE
                version?.setTextColor(textColor)
            }
        }

        price?.setTextColor(accentColor)
        if (isInstalled) {
            val installed = resourceProvider.installedApps.packageInfo(app.packageName)
            price?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0, 0, 0)
            if (TextUtils.isEmpty(installed.versionName)) {
                price?.text = resourceProvider.installedText
            } else {
                price?.text = resourceProvider.formatVersionText(installed.versionName, installed.versionCode)
            }
            when {
                app.versionNumber > installed.versionCode -> version?.setTextColor(warningColor)
                app.versionNumber > 0 -> version?.setTextColor(textColor)
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
        price?.setTextColor(accentColor)
        if (app.status == AppInfoMetadata.STATUS_UPDATED) {
            version?.setTextColor(accentColor)
        }
    }
}
