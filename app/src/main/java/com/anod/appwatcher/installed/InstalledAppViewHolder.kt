package com.anod.appwatcher.installed

import android.view.View
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.utils.SingleLiveEvent
import com.anod.appwatcher.watchlist.AppViewHolder
import com.anod.appwatcher.watchlist.WishListAction

/**
 * @author alex
 * *
 * @date 2015-08-31
 */
internal class InstalledAppViewHolder(
        itemView: View,
        resourceProvider: ResourceProvider,
        iconLoader: PicassoAppIcon,
        action: SingleLiveEvent<WishListAction>)
    : AppViewHolder(itemView, resourceProvider, iconLoader, action) {

    override val isLocalApp: Boolean
        get() = true
}
