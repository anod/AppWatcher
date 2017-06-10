package com.anod.appwatcher.adapters

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.anod.appwatcher.R
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.utils.AppIconLoader

open class AppViewHolder(
        itemView: View,
        dataProvider: AppViewHolderBase.DataProvider,
        iconLoader: AppIconLoader,
        private val mListener: AppViewHolder.OnClickListener?)
    : AppViewHolderBase(itemView, dataProvider, iconLoader), View.OnClickListener {

    var app: AppInfo? = null
    var location: Int = 0

    @BindView(R.id.sec_header) lateinit var section: View
    @BindView(R.id.sec_header_title) lateinit var sectionText: TextView
    @BindView(R.id.sec_header_count) lateinit var sectionCount: TextView
    @BindView(android.R.id.icon) lateinit var icon: ImageView
    @BindView(R.id.new_indicator) lateinit var newIndicator: View
    @BindView(R.id.sec_action_button) lateinit var actionButton: Button
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
        ButterKnife.bind(this, itemView)

        this.detailsView = AppDetailsView(itemView, dataProvider)

        itemView.findViewById<View>(android.R.id.content).setOnClickListener(this)
    }

    @OnClick(R.id.sec_action_button)
    fun onAction() {
        mListener?.onActionButton()
    }

    override fun onClick(v: View) {
        mListener?.onItemClick(this.app!!)
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
        } else {
            section.visibility = View.GONE
        }
    }
}