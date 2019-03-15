package com.anod.appwatcher.watchlist

import android.view.View
import android.widget.ImageView
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.details.AppDetailsView
import com.anod.appwatcher.utils.PicassoAppIcon

open class AppViewHolder(
        itemView: View,
        resourceProvider: ResourceProvider,
        iconLoader: PicassoAppIcon,
        private val onClickListener: OnClickListener?)
    : AppViewHolderBase(itemView, resourceProvider, iconLoader), View.OnClickListener {

    private var item: AppListItem? = null
    private val icon: ImageView = itemView.findViewById(R.id.icon)
    private val detailsView: AppDetailsView = AppDetailsView(itemView, resourceProvider)

    open val isLocalApp: Boolean
        get() = false

    interface OnClickListener {
        fun onItemClick(app: App)
    }

    init {
        itemView.findViewById<View>(android.R.id.content).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        onClickListener?.onItemClick(this.item!!.app)
    }

    override fun bindView(item: AppListItem) {
        this.item = item

        this.detailsView.fillDetails(item.app, item.recentFlag, item.changeDetails ?: "", isLocalApp)
        iconLoader.loadAppIntoImageView(item.app, this.icon, R.drawable.ic_notifications_black_24dp)
    }
}