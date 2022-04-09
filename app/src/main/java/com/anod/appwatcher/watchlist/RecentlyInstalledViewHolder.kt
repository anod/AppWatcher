// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import android.content.pm.PackageManager
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.packageToApp
import com.anod.appwatcher.installed.RecentAppView
import com.anod.appwatcher.utils.EventFlow
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.reveal
import info.anodsplace.framework.view.setOnSafeClickListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RecentlyInstalledViewHolder(
        itemView: View,
        lifecycleOwner: LifecycleOwner,
        private val packages: Flow<List<InstalledPackageRow>>,
        private val iconLoader: AppIconLoader,
        private val packageManager: PackageManager,
        private val action: EventFlow<WishListAction>
) : RecyclerView.ViewHolder(itemView), PlaceholderViewHolder {

    companion object {
        const val animateInitial = 0
        const val animateStart = 1
        const val animateDone = 2
    }

    private var loadJob: Job? = null
    private val shortAnimationDuration = itemView.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    private val initialAlpha = 0.3f
    private var animate = animateInitial

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
            R.id.app10
    ).map { itemView.findViewById(it) }

    init {
        placeholder()
        loadJob = lifecycleOwner.lifecycleScope.launch {

            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                packages.collect { packages ->
                    appViews.forEachIndexed { index, appView ->
                        if (index >= packages.size) {
                            appView.isVisible = false
                        } else {
                            val pair = packages[index]
                            bindPackage(animate == animateStart, index, appView, pair.first, pair.second)
                        }
                    }
                    animate = animateDone
                }
            }
        }
    }

    override fun placeholder() {
        loadJob?.cancel()
        appViews.forEach { view ->
            view.isVisible = true
            view.alpha = initialAlpha
            view.icon.setImageResource(R.drawable.ic_app_icon_placeholder)
            view.title.text = ""
            view.watched.isInvisible = true
            view.content.setOnClickListener(null)
        }
    }

    fun bind(item: RecentItem) {
        if (animate == animateInitial) {
            animate = animateStart
        }
    }

    private fun bindPackage(animate: Boolean, index: Int, appView: RecentAppView, packageName: String, rowId: Int) {
        val app = packageManager.packageToApp(rowId, packageName)
        iconLoader.loadAppIntoImageView(app, appView.icon, R.drawable.ic_app_icon_placeholder)
        appView.title.text = app.title
        appView.watched.isVisible = (rowId > 0)
        appView.content.setOnSafeClickListener {
            action.tryEmit(ItemClick(app, index))
        }
        appView.reveal(animate, startDelay = index * 50L, duration = shortAnimationDuration, fromAlpha = initialAlpha)
    }
}