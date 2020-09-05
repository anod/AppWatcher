// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.content.pm.PackageManager
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.installed.RecentAppView
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.utils.SingleLiveEvent
import info.anodsplace.framework.view.setOnSafeClickListener

class RecentlyInstalledViewHolder(
        itemView: View,
        private val iconLoader: PicassoAppIcon,
        private val packageManager: PackageManager,
        private val action: SingleLiveEvent<WishListAction>) : RecyclerView.ViewHolder(itemView), BindableViewHolder<List<Pair<String, Int>>> {

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

    init {
        itemView.findViewById<View>(R.id.more).setOnClickListener {
            action.value = ImportInstalled
        }
    }

    override fun bind(item: List<Pair<String, Int>>) {
        appViews.forEachIndexed { index, view ->
            if (index >= item.size) {
                view.visibility = View.GONE
            } else {
                val app = packageManager.packageToApp(item[index].second, item[index].first)
                iconLoader.loadAppIntoImageView(app, view.icon, R.drawable.ic_notifications_black_24dp)
                view.title.text = app.title
                view.visibility = View.VISIBLE
                view.watched.visibility = if (item[index].second > 0) View.VISIBLE else View.INVISIBLE
                view.findViewById<View>(R.id.content).setOnSafeClickListener {
                    action.value = ItemClick(app)
                }
            }
        }
    }

    override fun placeholder() {
        TODO("Not yet implemented")
    }
}