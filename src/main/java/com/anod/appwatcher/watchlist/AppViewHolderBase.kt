package com.anod.appwatcher.watchlist

import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v7.widget.RecyclerView
import android.view.View
import com.anod.appwatcher.content.AppChangeContentProviderClient

import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.framework.InstalledApps

/**
 * @author algavris
 * *
 * @date 22/05/2016.
 */

abstract class AppViewHolderBase(
        itemView: View,
        protected val dataProvider: DataProvider,
        protected val iconLoader: PicassoAppIcon) : RecyclerView.ViewHolder(itemView) {

     interface DataProvider {
        val installedText: String
        val noRecentChangesText: String
        val totalAppsCount: Int
        val newAppsCount: Int
        val installedApps: InstalledApps
        val appChangeContentProvider: AppChangeContentProviderClient
        fun formatVersionText(versionName: String, versionNumber: Int): String
        @ColorInt
        fun getColor(@ColorRes colorRes: Int): Int

        val updatableAppsCount: Int
    }

    abstract fun bindView(location: Int, app: AppInfo)
}
