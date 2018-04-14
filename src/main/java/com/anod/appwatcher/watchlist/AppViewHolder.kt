package com.anod.appwatcher.watchlist

import android.view.View
import android.widget.ImageView
import com.anod.appwatcher.R
import com.anod.appwatcher.details.AppDetailsView
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.utils.PicassoAppIcon

open class AppViewHolder(
        itemView: View,
        resourceProvider: ResourceProvider,
        iconLoader: PicassoAppIcon,
        private val onClickListener: OnClickListener?)
    : AppViewHolderBase(itemView, resourceProvider, iconLoader), View.OnClickListener {

    var app: AppInfo? = null
    var location: Int = 0

    val icon: ImageView = itemView.findViewById(R.id.icon)
    val newIndicator: View = itemView.findViewById(R.id.new_indicator)
    val detailsView: AppDetailsView

    open val isLocalApp: Boolean
        get() = false

    interface OnClickListener {
        fun onItemClick(app: AppInfo)
        fun onActionButton()
    }

    init {
        this.app = null
        this.location = 0

        this.detailsView = AppDetailsView(itemView, resourceProvider)

        itemView.findViewById<View>(android.R.id.content).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        onClickListener?.onItemClick(this.app!!)
    }

    override fun bindView(location: Int, app: AppInfo) {
        this.location = location
        this.app = app

        this.detailsView.fillDetails(app, isLocalApp)

        if (app.status == AppInfoMetadata.STATUS_UPDATED) {
            newIndicator.visibility = View.VISIBLE
        } else {
            newIndicator.visibility = View.INVISIBLE
        }

        iconLoader.loadAppIntoImageView(app, this.icon, R.drawable.ic_notifications_black_24dp)
    }
}