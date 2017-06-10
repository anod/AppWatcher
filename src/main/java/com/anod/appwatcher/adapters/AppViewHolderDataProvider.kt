package com.anod.appwatcher.adapters

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat

import com.anod.appwatcher.R
import com.anod.appwatcher.utils.InstalledAppsProvider

/**
 * @author alex
 * *
 * @date 2015-08-30
 */
open class AppViewHolderDataProvider(private val mContext: Context, override val installedAppsProvider: InstalledAppsProvider)
    : AppViewHolderBase.DataProvider {
    override val installedText = mContext.resources.getString(R.string.installed)!!
    override var totalAppsCount = 0
    final override var newAppsCount = 0
        private set
    final override var updatableAppsCount = 0
        private set

    internal fun setNewAppsCount(newAppsCount: Int, updatableAppsCount: Int) {
        this.newAppsCount = newAppsCount
        this.updatableAppsCount = updatableAppsCount
    }

    @ColorInt
    override fun getColor(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(mContext, colorRes)
    }

    override fun formatVersionText(versionName: String, versionNumber: Int): String {
        return mContext.getString(R.string.version_text, versionName, versionNumber)
    }

}
