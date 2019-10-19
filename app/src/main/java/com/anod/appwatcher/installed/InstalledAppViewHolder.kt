package com.anod.appwatcher.installed

import android.view.View
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.watchlist.AppViewHolder
import com.anod.appwatcher.watchlist.AppViewHolderBase

/**
 * @author alex
 * *
 * @date 2015-08-31
 */
internal class InstalledAppViewHolder(
        itemView: View,
        resourceProvider: ResourceProvider,
        iconLoader: PicassoAppIcon,
        listener: OnClickListener?)
    : AppViewHolder(itemView, resourceProvider, iconLoader, listener) {

    override val isLocalApp: Boolean
        get() = true
}
