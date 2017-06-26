package com.anod.appwatcher.adapters

import android.content.Context
import android.view.View

import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.recyclerview.RecyclerViewCursorAdapter
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.InstalledAppsProvider

/**
 * @author alex
 * *
 * @date 2015-06-20
 */
class AppListCursorAdapterWrapper(
        context: Context,
        iap: InstalledAppsProvider,
        private val mListener: AppViewHolder.OnClickListener) : RecyclerViewCursorAdapter<AppViewHolder, AppListCursor>(context, R.layout.list_item_app) {

    private val mDataProvider: AppViewHolderDataProvider = AppViewHolderDataProvider(context, iap)
    private val mIconLoader: AppIconLoader = App.provide(context).iconLoader

    override fun onCreateViewHolder(itemView: View): AppViewHolder {
        return AppViewHolder(itemView, mDataProvider, mIconLoader, mListener)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int, cursor: AppListCursor) {
        val app = cursor.appInfo
        holder.bindView(cursor.position, app)
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    override fun swapData(newCursor: AppListCursor?) {
        mDataProvider.totalAppsCount = newCursor?.count ?: 0
        super.swapData(newCursor)
    }

    fun setNewAppsCount(newCount: Int, updatableCount: Int) {
        mDataProvider.setNewAppsCount(newCount, updatableCount)
    }
}
