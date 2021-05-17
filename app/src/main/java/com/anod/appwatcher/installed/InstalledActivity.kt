// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import android.content.Context
import android.graphics.Rect
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.Keep
import androidx.fragment.app.commit
import com.anod.appwatcher.details.DetailsDialog
import com.anod.appwatcher.details.DetailsEmptyView
import com.anod.appwatcher.details.DetailsFragment
import com.anod.appwatcher.watchlist.AppDetailsRouter
import info.anodsplace.framework.R
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.FragmentFactory
import info.anodsplace.framework.app.FragmentToolbarActivity

@Keep
class InstalledActivity : FragmentToolbarActivity(), AppDetailsRouter {

    @get:IdRes
    override val detailsLayoutId = R.id.details

    @get:IdRes
    override val hingeLayoutId = R.id.hinge

    private val stateViewModel: InstalledActivityViewModel by viewModels()

    override fun updateWideLayout(isWideLayout: Boolean, hinge: Rect) {
        super.updateWideLayout(isWideLayout, hinge)
        stateViewModel.isWideLayout = isWideLayout
        if (stateViewModel.isWideLayout) {
            if (supportFragmentManager.findFragmentByTag(DetailsEmptyView.tag) == null) {
                supportFragmentManager.commit {
                    replace(R.id.details, DetailsEmptyView(), DetailsEmptyView.tag)
                }
            }
        }
    }

    override fun openAppDetails(appId: String, rowId: Int, detailsUrl: String?) {
        if (stateViewModel.isWideLayout) {
            supportFragmentManager.commit {
                add(R.id.details, DetailsFragment.newInstance(appId, detailsUrl
                        ?: "", rowId), DetailsFragment.tag)
                addToBackStack(DetailsFragment.tag)
            }
        } else {
            DetailsDialog.show(appId, rowId, detailsUrl, supportFragmentManager)
        }
    }

    companion object {
        fun intent(context: Context, factory: FragmentFactory, themeRes: Int, themeColors: CustomThemeColors) =
                intent(
                        context = context,
                        factory = factory,
                        themeRes = themeRes,
                        themeColors = themeColors,
                        clazz = InstalledActivity::class.java
                )
    }
}