package com.anod.appwatcher.watchlist

import android.content.Context
import android.view.View

import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.utils.PicassoAppIcon
import info.anodsplace.appwatcher.framework.RecyclerViewCursorAdapter
import info.anodsplace.appwatcher.framework.InstalledApps

/**
 * @author alex
 * *
 * @date 2015-06-20
 */
class AppListCursorAdapter(
        context: Context,
        iap: InstalledApps,
        private val listener: AppViewHolder.OnClickListener) : RecyclerViewCursorAdapter<AppViewHolder, AppListCursor>(context, R.layout.list_item_app) {

    private val dataProvider: AppViewHolderDataProvider = AppViewHolderDataProvider(context, iap)
    private val appIcon: PicassoAppIcon = App.provide(context).iconLoader

    override fun onCreateViewHolder(itemView: View): AppViewHolder {
        return AppViewHolder(itemView, dataProvider, appIcon, listener)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int, cursor: AppListCursor) {
        val app = cursor.appInfo
        holder.bindView(cursor.position, app)
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    override fun swapData(newCursor: AppListCursor?) {
        dataProvider.totalAppsCount = newCursor?.count ?: 0
        super.swapData(newCursor)
    }

    fun setNewAppsCount(newCount: Int, updatableCount: Int) {
        dataProvider.setNewAppsCount(newCount, updatableCount)
    }
}
