package com.anod.appwatcher.adapters

import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v7.widget.RecyclerView
import android.view.View

import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.InstalledAppsProvider

/**
 * @author algavris
 * *
 * @date 22/05/2016.
 */

abstract class AppViewHolderBase(
        itemView: View,
        protected val dataProvider: AppViewHolderBase.DataProvider,
        protected val iconLoader: AppIconLoader) : RecyclerView.ViewHolder(itemView) {

     interface DataProvider {
        val installedText: String
        val totalAppsCount: Int
        val newAppsCount: Int
        val installedAppsProvider: InstalledAppsProvider
        fun formatVersionText(versionName: String, versionNumber: Int): String
        @ColorInt
        fun getColor(@ColorRes colorRes: Int): Int

        val updatableAppsCount: Int
    }

    abstract fun bindView(location: Int, app: AppInfo)
}
