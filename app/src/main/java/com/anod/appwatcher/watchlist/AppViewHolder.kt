package com.anod.appwatcher.watchlist

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.view.isVisible
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.details.AppDetailsView
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.utils.SingleLiveEvent
import info.anodsplace.framework.view.setOnSafeClickListener

class AppViewHolder(
        itemView: View,
        resourceProvider: ResourceProvider,
        iconLoader: PicassoAppIcon,
        private val action: SingleLiveEvent<WishListAction>)
    : AppViewHolderBase<AppItem>(itemView, resourceProvider, iconLoader) {

    enum class Selection {
        None, Disabled, NotSelected, Selected;

        val enabled: Boolean
            get() = this != None && this != Disabled
    }

    private var selectionMode = false
    private var app: App? = null
    private val icon: ImageView = itemView.findViewById(R.id.icon)
    private val detailsView: AppDetailsView = AppDetailsView(itemView, resourceProvider)
    private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

    init {
        val content = itemView.findViewById<View>(R.id.content)
        content.setOnSafeClickListener {
            if (selectionMode) {
                this.checkBox.isChecked = !this.checkBox.isChecked
            }
            action.value = ItemClick(this.app!!, this.checkBox.isChecked)
        }

        content.setOnLongClickListener {
            action.value = ItemLongClick(this.app!!)
            true
        }
    }

    fun bind(item: AppListItem, isLocal: Boolean, selection: Selection) {
        this.app = item.app
        this.selectionMode = selection.enabled
        this.checkBox.isVisible = selection != Selection.None
        this.checkBox.isEnabled = selection != Selection.Disabled
        this.checkBox.isChecked = selection.enabled && selection == Selection.Selected

        this.detailsView.fillDetails(
                item.app, item.recentFlag, item.changeDetails ?: "",
                item.noNewDetails, isLocal)
        iconLoader.loadAppIntoImageView(item.app, this.icon, R.drawable.ic_app_icon_placeholder)
    }

    override fun placeholder() {
        this.checkBox.isVisible = false
        this.detailsView.placeholder()
        icon.setImageResource(R.drawable.ic_app_icon_placeholder)
    }

}