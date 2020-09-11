// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.content.pm.PackageManager
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.installed.InstalledTaskWorker
import com.anod.appwatcher.installed.RecentAppView
import com.anod.appwatcher.provide
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.utils.SingleLiveEvent
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.view.setOnSafeClickListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RecentlyInstalledViewHolder(
        itemView: View,
        private val lifecycleScope: LifecycleCoroutineScope,
        private val iconLoader: PicassoAppIcon,
        private val packageManager: PackageManager,
        private val action: SingleLiveEvent<WishListAction>) : RecyclerView.ViewHolder(itemView), BindableViewHolder<RecentItem> {

    private var loadJob: Job? = null

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

    private val shortAnimationDuration = itemView.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    private val appContext: ApplicationContext = ApplicationContext(itemView.context.applicationContext)
    private var animate = true
    private var item: RecentItem? = null

    override fun placeholder() {
        loadJob?.cancel()
        appViews.forEach { view ->
            view.isVisible = false
        }
    }

    override fun bind(item: RecentItem) {
        if (this.item == item) {
            return
        }
        placeholder()
        loadJob = lifecycleScope.launch {
            val installed = InstalledTaskWorker(appContext, item.sortId, item.titleFilter).run()
            if (installed.first.isEmpty()) {
                return@launch
            }
            val database = appContext.provide.database
            val watchingPackages = database.apps().loadRowIds(installed.first).associateBy({ it.packageName }, { it.rowId })
            appViews.forEachIndexed { index, appView ->
                if (index >= watchingPackages.size) {
                    appView.visibility = View.GONE
                } else {
                    val packageName = installed.first[index]
                    bindPackage(animate, index, appView, packageName, watchingPackages[packageName]
                            ?: -1)
                }
            }
            animate = false
        }
    }

    private fun bindPackage(animate: Boolean, index: Int, appView: RecentAppView, packageName: String, rowId: Int) {
        val app = packageManager.packageToApp(rowId, packageName)
        iconLoader.loadAppIntoImageView(app, appView.icon, R.drawable.ic_app_icon_placeholder)
        appView.title.text = app.title
        appView.watched.visibility = if (rowId > 0) View.VISIBLE else View.INVISIBLE
        appView.findViewById<View>(R.id.content).setOnSafeClickListener {
            action.value = ItemClick(app)
        }

        if (animate) {
            appView.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate()
                        .setStartDelay(index * 50L)
                        .alpha(1f)
                        .setDuration(shortAnimationDuration)
                        .setListener(null)
            }
        } else {
            appView.apply {
                alpha = 1f
                visibility = View.VISIBLE
            }
        }
    }

}