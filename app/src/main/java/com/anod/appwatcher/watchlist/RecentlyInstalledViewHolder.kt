// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.content.pm.PackageManager
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.installed.RecentAppView
import com.anod.appwatcher.provide
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.utils.SingleLiveEvent
import com.anod.appwatcher.utils.reveal
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.content.getRecentlyInstalled
import info.anodsplace.framework.view.setOnSafeClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecentlyInstalledViewHolder(
        itemView: View,
        private val lifecycleScope: LifecycleCoroutineScope,
        private val iconLoader: PicassoAppIcon,
        private val packageManager: PackageManager,
        private val action: SingleLiveEvent<WishListAction>) : RecyclerView.ViewHolder(itemView), PlaceholderViewHolder {

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
    private val moreButton: View = itemView.findViewById<View>(R.id.more)

    init {
        moreButton.setOnClickListener {
            action.value = RecentlyInstalled
        }
    }

    private val shortAnimationDuration = itemView.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    private val appContext: ApplicationContext = ApplicationContext(itemView.context.applicationContext)
    private var animate = true

    override fun placeholder() {
        loadJob?.cancel()
        moreButton.isVisible = false
        appViews.forEachIndexed { index, view ->
            if (index > 0) {
                view.isVisible = false
            } else {
                view.isInvisible = true
            }
            view.icon.setImageResource(R.drawable.ic_app_icon_placeholder)
            view.title.text = ""
            view.watched.isInvisible = true
            view.content.setOnClickListener(null)
        }
    }

    fun bind(item: RecentItem) {
        placeholder()
        loadJob = lifecycleScope.launch {
            val packages = loadRecentPackages()
            if (packages.isEmpty()) {
                return@launch
            }
            val database = appContext.provide.database
            val watchingPackages = database.apps().loadRowIds(packages).associateBy({ it.packageName }, { it.rowId })
            appViews.forEachIndexed { index, appView ->
                if (index >= packages.size) {
                    appView.visibility = View.GONE
                } else {
                    val packageName = packages[index]
                    bindPackage(animate, index, appView, packageName, watchingPackages[packageName]
                            ?: -1)
                }
            }
            moreButton.reveal(animate, startDelay = packages.size * 50L, duration = shortAnimationDuration)
            animate = false
        }
    }

    private suspend fun loadRecentPackages() = withContext(Dispatchers.Default) {
        return@withContext appContext.packageManager.getRecentlyInstalled().take(appViews.size)
    }

    private fun bindPackage(animate: Boolean, index: Int, appView: RecentAppView, packageName: String, rowId: Int) {
        val app = packageManager.packageToApp(rowId, packageName)
        iconLoader.loadAppIntoImageView(app, appView.icon, R.drawable.ic_app_icon_placeholder)
        appView.title.text = app.title
        appView.watched.visibility = if (rowId > 0) View.VISIBLE else View.INVISIBLE
        appView.content.setOnSafeClickListener {
            action.value = ItemClick(app, false)
        }
        appView.reveal(animate, startDelay = index * 50L, duration = shortAnimationDuration)
    }

}