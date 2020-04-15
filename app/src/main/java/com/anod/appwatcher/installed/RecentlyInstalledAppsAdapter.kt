package com.anod.appwatcher.installed

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.watchlist.AppViewHolder
import info.anodsplace.framework.view.setOnSafeClickListener

/**
 * @author alex
 * *
 * @date 2015-08-30
 */
open class RecentlyInstalledAppsAdapter(
        protected val context: Context,
        private val packageManager: PackageManager,
        protected val listener: AppViewHolder.OnClickListener?)
    : RecyclerView.Adapter<RecentlyInstalledAppsAdapter.ViewHolder>() {

    var recentlyInstalled: List<InstalledPairRow> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val iconLoader: PicassoAppIcon = Application.provide(context).iconLoader

    override fun getItemCount(): Int {
        return if (recentlyInstalled.isEmpty()) 0 else 1
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_recently_installed, parent, false)
        return ViewHolder(view, iconLoader, packageManager, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(recentlyInstalled)
    }

    class ViewHolder(
            itemView: View,
            private val iconLoader: PicassoAppIcon,
            private val packageManager: PackageManager,
            private val listener: AppViewHolder.OnClickListener?) : RecyclerView.ViewHolder(itemView) {

        private val appViews: List<RecentAppView> = arrayListOf(
                R.id.app1,
                R.id.app2,
                R.id.app3,
                R.id.app4,
                R.id.app5,
                R.id.app6,
                R.id.app7,
                R.id.app8,
                R.id.app9,
                R.id.app10,
                R.id.app11,
                R.id.app12,
                R.id.app13,
                R.id.app14,
                R.id.app15,
                R.id.app16
        ).map { itemView.findViewById<RecentAppView>(it) }

        fun bind(packages: List<Pair<String, Int>>) {
            appViews.forEachIndexed { index, view ->
                if (index >= packages.size) {
                    view.visibility = View.GONE
                } else {
                    val app = packageManager.packageToApp(packages[index].second, packages[index].first)
                    iconLoader.loadAppIntoImageView(app, view.icon, R.drawable.ic_notifications_black_24dp)
                    view.title.text = app.title
                    view.visibility = View.VISIBLE
                    view.watched.visibility = if (packages[index].second > 0) View.VISIBLE else View.INVISIBLE
                    view.findViewById<View>(R.id.content).setOnSafeClickListener {
                        listener?.onItemClick(app)
                    }
                }
            }
        }
    }

}
