package com.anod.appwatcher.tags

import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.databinding.ListItemEmptyBinding
import com.anod.appwatcher.utils.prefs
import com.anod.appwatcher.watchlist.EmptyViewHolder
import com.anod.appwatcher.watchlist.WatchListAction
import com.anod.appwatcher.watchlist.WatchListFragment
import com.anod.appwatcher.watchlist.WatchListPagingSource
import info.anodsplace.framework.app.FragmentContainerFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.component.KoinComponent

/**
 * @author Alex Gavrishev
 * *
 * @date 01/04/2017.
 */

class AppsTagListFragment : WatchListFragment(), KoinComponent {

    override fun mapAction(it: WatchListAction): WatchListAction {
        if (it is WatchListAction.EmptyButton) {
            return when (it.idx) {
                1 -> {
                    val tag = viewModel.viewState.tag!!
                    WatchListAction.AddAppToTag(tag)
                }
                else -> throw IllegalArgumentException("Unknown Idx")
            }
        }
        return it
    }

    override fun config() = WatchListPagingSource.Config(
            showRecentlyUpdated = prefs.showRecentlyUpdated,
            showOnDevice = false,
            showRecentlyInstalled = false
    )

    override fun createEmptyViewHolder(emptyBinding: ListItemEmptyBinding, action: MutableSharedFlow<WatchListAction>): EmptyViewHolder {
        emptyBinding.emptyText.setText(R.string.tags_list_is_empty)
        viewModel.viewState.tag?.let {
            val button = emptyBinding.button1
            button.setBackgroundColor(it.color)
            if (it.isLightColor) {
                button.setTextColor(ResourcesCompat.getColor(resources, R.color.alwaysBlack, context?.theme))
            } else {
                button.setTextColor(ResourcesCompat.getColor(resources, R.color.alwaysWhite, context?.theme))
            }
        }
        emptyBinding.button2.isVisible = false
        emptyBinding.button3.isVisible = false
        return EmptyViewHolder(emptyBinding, false, action)
    }

    class Factory(
            private val filterId: Int,
            private val sortId: Int,
            private val tag: Tag?,
            val title: String
    ) : FragmentContainerFactory("apps-tags-$filterId-$sortId-${tag?.hashCode()}") {

        override fun create(): Fragment = AppsTagListFragment().also {
            it.arguments = Bundle().apply {
                putInt(ARG_FILTER, filterId)
                putInt(ARG_SORT, sortId)
                tag?.let { tag ->
                    putParcelable(ARG_TAG, tag)
                }
            }
        }
    }

}