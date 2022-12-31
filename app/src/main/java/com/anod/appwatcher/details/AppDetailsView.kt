package com.anod.appwatcher.details

import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.generateTitle
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.watchlist.AppViewHolderBase
import info.anodsplace.framework.text.Html

/**
 * @author Alex Gavrishev
 * @date 14/05/2016.
 */
class AppDetailsView(view: View, private val resourceProvider: AppViewHolderBase.ResourceProvider) {

    @ColorInt
    private val textColor: Int

    @ColorInt
    private var accentColor: Int

    @ColorInt
    private var warningColor: Int

    val title: TextView = view.findViewById(R.id.title)
    val creator: TextView? = view.findViewById(R.id.creator)
    private val price: TextView? = view.findViewById(R.id.price)
    private val updateDate: TextView? = view.findViewById(R.id.update_date)
    private val resources = view.resources
    private var app: App? = null

    init {
        accentColor = resourceProvider.getColorOfAttribute(com.google.android.material.R.attr.colorPrimary)
        textColor = resourceProvider.getColorOfAttribute(com.google.android.material.R.attr.colorOnSurface)
        warningColor = resourceProvider.getColorOfResource(R.color.material_amber_800)
    }

    fun fillDetails(app: App, isLocalApp: Boolean) {
        this.app = app
        title.text = app.generateTitle(resources)
        creator?.text = app.creator
        val uploadDate = app.uploadDate

        if (uploadDate.isEmpty()) {
            updateDate?.visibility = View.GONE
        } else {
            updateDate?.text = uploadDate
            updateDate?.visibility = View.VISIBLE
        }

        if (isLocalApp) {
            this.creator?.isVisible = false
            this.price?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0, 0, 0)
            this.price?.setTextColor(textColor)
            this.price?.text = resourceProvider.formatVersionText(app.versionName, app.versionNumber, 0)
        } else {
            this.creator?.isVisible = false
            this.fillWatchAppView(app)
        }
    }

    private fun fillWatchAppView(app: App) {
        val versionDetailsTextView = this.price ?: return

        // Price field
        val packageInfo = resourceProvider.installedApps.packageInfo(app.packageName)
        when {
            app.versionNumber == 0 -> {
                versionDetailsTextView.setTextColor(warningColor)
                versionDetailsTextView.text = resourceProvider.getString(R.string.updates_not_available)
            }
            packageInfo.isInstalled -> {
                versionDetailsTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0, 0, 0)
                when {
                    app.versionNumber > packageInfo.versionCode -> {
                        versionDetailsTextView.setTextColor(warningColor)
                        versionDetailsTextView.text = resourceProvider.formatVersionText(packageInfo.versionName, packageInfo.versionCode, app.versionNumber)
                    }
                    app.status == AppInfoMetadata.STATUS_UPDATED -> {
                        versionDetailsTextView.setTextColor(accentColor)
                        versionDetailsTextView.text = resourceProvider.formatVersionText(packageInfo.versionName, packageInfo.versionCode, 0)
                    }
                    else -> {
                        versionDetailsTextView.setTextColor(textColor)
                        versionDetailsTextView.text = resourceProvider.formatVersionText(packageInfo.versionName, packageInfo.versionCode, 0)
                    }
                }
            }
            else -> {
                versionDetailsTextView.setCompoundDrawables(null, null, null, null)
                if (app.price.isFree) {
                    versionDetailsTextView.setText(R.string.free)
                } else {
                    versionDetailsTextView.text = app.price.text
                }

                when (app.status) {
                    AppInfoMetadata.STATUS_UPDATED -> {
                        versionDetailsTextView.setTextColor(accentColor)
                    }
                    else -> {
                        versionDetailsTextView.setTextColor(textColor)
                    }
                }
            }
        }
    }

    fun updateAccentColor(@ColorInt color: Int) {
        accentColor = color
        this.app?.let {
            fillWatchAppView(it)
        }
    }

}