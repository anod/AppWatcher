package com.anod.appwatcher.watchlist

import android.content.Context
import android.support.v7.util.DiffUtil
import android.view.View
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.PicassoAppIcon
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.framework.widget.recyclerview.RecyclerViewCursorListAdapter

/**
 * @author alex
 * *
 * @date 2015-06-20
 */
class AppListCursorAdapter(
        context: Context,
        iap: InstalledApps,
        private val listener: AppViewHolder.OnClickListener) : RecyclerViewCursorListAdapter<AppViewHolder, AppInfo, AppListCursor>(context, R.layout.list_item_app) {

    override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        return oldItem.rowId == newItem.rowId
    }

    override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        return oldItem == newItem
    }

    private val dataProvider: AppViewHolderDataProvider = AppViewHolderDataProvider(context, iap)
    private val appIcon: PicassoAppIcon = App.provide(context).iconLoader

    override fun onCreateViewHolder(itemView: View): AppViewHolder {
        return AppViewHolder(itemView, dataProvider, appIcon, listener)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int, appInfo: AppInfo) {
        holder.bindView(position, appInfo)
    }

    override fun swapData(newCursor: AppListCursor?) {
        dataProvider.totalAppsCount = newCursor?.count ?: 0
        super.swapData(newCursor)
    }

    fun setNewAppsCount(newCount: Int, updatableCount: Int, recentlyUpdatedCount: Int) {
        dataProvider.setNewAppsCount(newCount, updatableCount, recentlyUpdatedCount)
    }
}
