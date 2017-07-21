package com.anod.appwatcher.installed

import android.content.Context
import android.content.pm.PackageManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.adapters.AppViewHolder
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.PackageManagerUtils
import butterknife.bindViews

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

    var recentlyInstalled: List<String> = mutableListOf()
    private val iconLoader: AppIconLoader = App.provide(context).iconLoader

    override fun getItemCount(): Int {
        return if (recentlyInstalled.isEmpty()) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecentlyInstalledAppsAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_recently_installed, parent, false)
        return ViewHolder(view, iconLoader, packageManager)
    }

    override fun onBindViewHolder(holder: RecentlyInstalledAppsAdapter.ViewHolder, position: Int) {
        holder.bind(recentlyInstalled)
    }

    class ViewHolder(
            itemView: View,
            private val iconLoader: AppIconLoader,
            private val packageManager: PackageManager) : RecyclerView.ViewHolder(itemView) {

        val appViews: List<RecentAppView> by bindViews(R.id.app1, R.id.app2, R.id.app3, R.id.app4, R.id.app5)

        init {
            val sectionCount: TextView = itemView.findViewById(R.id.sec_header_count)
            val sectionButton: TextView = itemView.findViewById(R.id.sec_action_button)
            sectionCount.visibility = View.GONE
            sectionButton.visibility = View.GONE
            val sectionText: TextView = itemView.findViewById(R.id.sec_header_title)
            sectionText.setText(R.string.recently_installed)
        }

        fun bind(packageNames: List<String>) {
            appViews.forEachIndexed { index, view ->
                val packageName: String? = packageNames[index]
                if (packageName == null) {
                    view.visibility = View.GONE
                } else {
                    val app = PackageManagerUtils.packageToApp(packageName, packageManager)
                    iconLoader.loadAppIntoImageView(app, view.icon, R.drawable.ic_notifications_black_24dp)
                    view.title.text = app.title
                    view.visibility = View.VISIBLE
                }
            }
        }
    }

}