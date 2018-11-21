package com.anod.appwatcher.details

import androidx.annotation.ColorInt
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
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

    private val title: TextView = view.findViewById(android.R.id.title)
    private val creator: TextView? = view.findViewById<TextView?>(R.id.creator)
    private val price: TextView? = view.findViewById<TextView?>(R.id.price)
    private val updateDate: TextView? = view.findViewById<TextView?>(R.id.update_date)
    private val recentChanges = view.findViewById<TextView?>(R.id.recent_changes)

    init {
        accentColor = resourceProvider.getColor(R.color.theme_accent)
        textColor = resourceProvider.getColor(R.color.primary_text)
        warningColor = resourceProvider.getColor(R.color.material_amber_800)
    }

    fun fillDetails(app: App, recentFlag: Boolean, changeDetails: String, isLocalApp: Boolean) {
        title.text = app.title
        creator?.text = app.creator
        val uploadDate = app.uploadDate

        if (TextUtils.isEmpty(uploadDate)) {
            updateDate?.visibility = View.GONE
        } else {
            updateDate?.text = uploadDate
            updateDate?.visibility = View.VISIBLE
        }

        if (isLocalApp) {
            this.creator?.visibility = View.GONE
            this.recentChanges?.visibility = View.GONE
            this.price?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0, 0, 0)
            this.price?.setTextColor(textColor)
            this.price?.text = resourceProvider.formatVersionText(app.versionName, app.versionNumber, 0)
        } else {
            this.creator?.visibility = View.VISIBLE
            this.recentChanges?.text = if (changeDetails.isBlank()) resourceProvider.noRecentChangesText else Html.parse(changeDetails)
            this.fillWatchAppView(app, recentFlag)
        }
    }

    private fun fillWatchAppView(app: App, recentFlag: Boolean) {
        val price = this.price ?: return

        // Price field
        val isInstalled = resourceProvider.installedApps.packageInfo(app.packageName).isInstalled
        when {
            app.versionNumber == 0 -> {
                this.recentChanges?.visibility = View.GONE
                price.setTextColor(warningColor)
                price.text = resourceProvider.getString(R.string.updates_not_available)
            }
            isInstalled -> {
                val installed = resourceProvider.installedApps.packageInfo(app.packageName)
                price.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stat_communication_stay_primary_portrait, 0, 0, 0)
                when {
                    app.versionNumber > installed.versionCode -> {
                        price.setTextColor(warningColor)
                        price.text = resourceProvider.formatVersionText(installed.versionName, installed.versionCode, app.versionNumber)
                    }
                    app.status == AppInfoMetadata.STATUS_UPDATED || recentFlag -> {
                        price.setTextColor(accentColor)
                        price.text = resourceProvider.formatVersionText(installed.versionName, installed.versionCode, 0)
                    }
                    else -> {
                        price.setTextColor(textColor)
                        price.text = resourceProvider.formatVersionText(installed.versionName, installed.versionCode, 0)
                    }
                }
            }
            else -> {
                price.setCompoundDrawables(null, null, null, null)
                if (app.price.isFree) {
                    price.setText(R.string.free)
                } else {
                    price.text = app.price.text
                }

                when {
                    app.status == AppInfoMetadata.STATUS_UPDATED || recentFlag -> {
                        price.setTextColor(accentColor)
                    }
                    else -> {
                        price.setTextColor(textColor)
                    }
                }
            }
        }

        // Recent changes
        when {
            app.status == AppInfoMetadata.STATUS_UPDATED || recentFlag -> {
                this.recentChanges?.visibility = View.VISIBLE
            }
            else -> {
                this.recentChanges?.visibility = View.GONE
            }
        }
    }

    fun updateAccentColor(@ColorInt color: Int) {
        accentColor = color
        price!!.setTextColor(accentColor)
    }
}
