package com.anod.appwatcher.installed

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.watchlist.AppViewHolder
import com.anod.appwatcher.watchlist.AppViewHolderBase
import com.anod.appwatcher.watchlist.AppViewHolderResourceProvider
import info.anodsplace.framework.content.InstalledPackage

/**
 * @author alex
 * *
 * @date 2015-08-30
 */
open class InstalledAppsAdapter(
        protected val context: Context,
        private val packageManager: PackageManager,
        private val dataProvider: AppViewHolderResourceProvider,
        protected val listener: AppViewHolder.OnClickListener?)
    : RecyclerView.Adapter<AppViewHolderBase>() {

    var installedPackages: List<InstalledPackage> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    internal val iconLoader: PicassoAppIcon = Application.provide(context).iconLoader

    override fun getItemCount(): Int {
        return this.installedPackages.size
    }

    override fun getItemViewType(position: Int): Int {
        return 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolderBase {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_app, parent, false)
        return InstalledAppViewHolder(v, dataProvider, iconLoader, listener)
    }

    override fun onBindViewHolder(holder: AppViewHolderBase, position: Int) {
        val installedPackage = installedPackages[position]
        val app = packageManager.packageToApp(-1, installedPackage.packageName)
        holder.bindView(AppListItem(app, "", false, false))
    }

}
