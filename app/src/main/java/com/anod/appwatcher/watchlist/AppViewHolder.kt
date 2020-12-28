package com.anod.appwatcher.watchlist

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.view.isVisible
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.details.AppDetailsView
import com.anod.appwatcher.utils.EventFlow
import com.anod.appwatcher.utils.PicassoAppIcon
import info.anodsplace.framework.view.setOnSafeClickListener

class AppViewHolder(
        itemView: View,
        resourceProvider: ResourceProvider,
        iconLoader: PicassoAppIcon,
        private val action: EventFlow<WishListAction>)
    : AppViewHolderBase<AppItem>(itemView, resourceProvider, iconLoader) {

    enum class Selection {
        None, Disabled, NotSelected, Selected;

        val enabled: Boolean
            get() = this != None && this != Disabled
    }

    private var index = -1
    private var app: App? = null
    private val icon: ImageView = itemView.findViewById(R.id.icon)
    private val detailsView: AppDetailsView = AppDetailsView(itemView, resourceProvider)
    private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    private val watched: ImageView = itemView.findViewById(R.id.watched)

    init {
        val content = itemView.findViewById<View>(R.id.content)
        content.setOnSafeClickListener {
            action.tryEmit(ItemClick(this.app!!, index))
        }

        content.setOnLongClickListener {
            action.tryEmit(ItemLongClick(this.app!!, index))
            true
        }
    }

    private fun updateSelection(selection: Selection) {
        this.checkBox.isVisible = selection != Selection.None
        this.checkBox.isChecked = selection.enabled && selection == Selection.Selected
    }

    fun bind(index: Int, item: AppListItem, isLocal: Boolean, selection: Selection) {
        this.index = index
        this.app = item.app
        this.watched.isVisible = isLocal && item.app.rowId >= 0
        this.updateSelection(selection)

        this.detailsView.fillDetails(
                item.app, item.recentFlag, item.changeDetails ?: "",
                item.noNewDetails, isLocal)
        iconLoader.loadAppIntoImageView(item.app, this.icon, R.drawable.ic_app_icon_placeholder)
    }

    override fun placeholder() {
        app = null
        this.checkBox.isVisible = false
        this.detailsView.placeholder()
        icon.setImageResource(R.drawable.ic_app_icon_placeholder)
    }

}