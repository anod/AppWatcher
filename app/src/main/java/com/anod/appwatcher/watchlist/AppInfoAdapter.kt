package com.anod.appwatcher.watchlist

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.utils.PicassoAppIcon
import info.anodsplace.framework.content.InstalledApps

/**
 * @author Alex Gavrishev
 * @date 13/04/2018
 */

class AppInfoAdapter(private val context: Context,
                     installedApps: InstalledApps,
                     private val listener: AppViewHolder.OnClickListener): RecyclerView.Adapter<AppViewHolder>() {

    private val itemDataProvider = AppViewHolderResourceProvider(context, installedApps)
    private var data: List<AppListItem> = emptyList()

    private val itemCallback = object: DiffUtil.ItemCallback<AppListItem>() {
        override fun areItemsTheSame(oldItem: AppListItem, newItem: AppListItem): Boolean {
            return oldItem.app.appId == newItem.app.appId
        }

        override fun areContentsTheSame(oldItem: AppListItem, newItem: AppListItem): Boolean {
            return oldItem == newItem
        }
    }

    private val appIcon: PicassoAppIcon = Application.provide(context).iconLoader

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_item_app, parent, false)
        return AppViewHolder(itemView, itemDataProvider, appIcon, listener)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = data[position]
        holder.bindView(appInfo)
    }

    override fun onViewRecycled(holder: AppViewHolder) {
        holder.recycle()
        super.onViewRecycled(holder)
    }

    class DiffCallback<OB>(private val oldList: List<OB>, private val newList: List<OB>, private val callback: DiffUtil.ItemCallback<OB>): DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return callback.areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return callback.areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }
    }

    fun updateList(list: List<AppListItem>) {
        if (list.isEmpty() || data.isEmpty()) {
            data = list
            notifyDataSetChanged()
            return
        }
        val callback = DiffCallback(data, list, itemCallback)
        val result = DiffUtil.calculateDiff(callback, true)
        data = list
        result.dispatchUpdatesTo(this)
    }
}