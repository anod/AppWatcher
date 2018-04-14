package com.anod.appwatcher.watchlist

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.anod.appwatcher.R
import com.anod.appwatcher.content.AppChangeContentProviderClient

import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.PicassoAppIcon
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.InstalledApps

/**
 * @author algavris
 * *
 * @date 22/05/2016.
 */

abstract class AppViewHolderBase(
        itemView: View,
        protected val resourceProvider: ResourceProvider,
        protected val iconLoader: PicassoAppIcon) : RecyclerView.ViewHolder(itemView) {

     interface ResourceProvider {
        val installedText: String
        val noRecentChangesText: String
        val installedApps: InstalledApps
        val appChangeContentProvider: AppChangeContentProviderClient
        fun formatVersionText(versionName: String, versionNumber: Int): String
        fun getString(@StringRes resId: Int): String
        @ColorInt
        fun getColor(@ColorRes colorRes: Int): Int
     }

    abstract fun bindView(location: Int, app: AppInfo)
}


open class AppViewHolderResourceProvider(
        private val context: Context,
        override val installedApps: InstalledApps)
    : AppViewHolderBase.ResourceProvider {

    override val installedText = context.resources.getString(R.string.installed)!!
    override val noRecentChangesText = context.resources.getString(R.string.no_recent_changes)!!
    override val appChangeContentProvider = AppChangeContentProviderClient(context)

    override fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    @ColorInt
    override fun getColor(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(context, colorRes)
    }

    override fun formatVersionText(versionName: String, versionNumber: Int): String {
        return context.getString(R.string.version_text, versionName, versionNumber)
    }

}