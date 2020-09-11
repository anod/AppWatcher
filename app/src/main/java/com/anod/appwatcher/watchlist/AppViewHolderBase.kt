package com.anod.appwatcher.watchlist

import android.content.Context
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.PicassoAppIcon
import info.anodsplace.framework.content.InstalledApps
import java.util.*

/**
 * @author Alex Gavrishev
 * *
 * @date 22/05/2016.
 */

interface PlaceholderViewHolder {
    fun placeholder()
}

abstract class AppViewHolderBase<T>(
        itemView: View,
        protected val resourceProvider: ResourceProvider,
        protected val iconLoader: PicassoAppIcon) : RecyclerView.ViewHolder(itemView), PlaceholderViewHolder {

    interface ResourceProvider {
        val installedText: String
        val noRecentChangesText: String
        val installedApps: InstalledApps
        fun formatVersionText(versionName: String, versionNumber: Int, newVersionNumber: Int): String
        fun getString(@StringRes resId: Int): String

        @ColorInt
        fun getColor(@ColorRes colorRes: Int): Int
    }

    override fun placeholder() {}
}


open class AppViewHolderResourceProvider(
        private val context: Context,
        override val installedApps: InstalledApps)
    : AppViewHolderBase.ResourceProvider {

    override val installedText: String = context.resources.getString(R.string.installed).apply {
        toUpperCase(Locale.getDefault())
    }
    override val noRecentChangesText: String = context.resources.getString(R.string.no_recent_changes)

    override fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    @ColorInt
    override fun getColor(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(context, colorRes)
    }

    override fun formatVersionText(versionName: String, versionNumber: Int, newVersionNumber: Int): String {
        if (newVersionNumber > 0) {
            return context.getString(R.string.version_updated_text, versionName, versionNumber, newVersionNumber)
        }
        return context.getString(R.string.version_text, versionName, versionNumber)
    }

}