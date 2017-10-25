package com.anod.appwatcher.adapters

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.anod.appwatcher.R
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.utils.PicassoAppIcon

open class AppViewHolder(
        itemView: View,
        dataProvider: AppViewHolderBase.DataProvider,
        iconLoader: PicassoAppIcon,
        private val onClickListener: AppViewHolder.OnClickListener?)
    : AppViewHolderBase(itemView, dataProvider, iconLoader), View.OnClickListener {

    var app: AppInfo? = null
    var location: Int = 0

    val section: View = itemView.findViewById(R.id.sec_header)
    val sectionText: TextView = itemView.findViewById(R.id.sec_header_title)
    val sectionCount: TextView = itemView.findViewById(R.id.sec_header_count)
    val icon: ImageView = itemView.findViewById(R.id.icon)
    val newIndicator: View = itemView.findViewById(R.id.new_indicator)
    val actionButton: Button = itemView.findViewById(R.id.sec_action_button)
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

        this.detailsView = AppDetailsView(itemView, dataProvider)

        itemView.findViewById<View>(android.R.id.content).setOnClickListener(this)

        this.actionButton.setOnClickListener {
            onClickListener?.onActionButton()
        }
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
        bindSectionView()
    }

    open fun bindSectionView() {
        if (location == dataProvider.newAppsCount) {
            sectionText.setText(R.string.watching)
            sectionCount.text = (dataProvider.totalAppsCount - dataProvider.newAppsCount).toString()
            section.visibility = View.VISIBLE
            actionButton.visibility = View.GONE
        } else if (location == 0 && dataProvider.newAppsCount > 0) {
            sectionText.setText(R.string.recently_updated)
            section.visibility = View.VISIBLE
            if (dataProvider.updatableAppsCount > 0) {
                actionButton.visibility = View.VISIBLE
                sectionCount.visibility = View.GONE
            } else {
                actionButton.visibility = View.GONE
                sectionCount.text = dataProvider.newAppsCount.toString()
                sectionCount.visibility = View.VISIBLE
            }
        } else if (section.visibility == View.VISIBLE) {
            section.visibility = View.GONE
        }
    }
}