package com.anod.appwatcher.tags

import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.databinding.ListItemEmptyBinding
import com.anod.appwatcher.watchlist.*
import info.anodsplace.framework.app.FragmentFactory
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * @author Alex Gavrishev
 * *
 * @date 01/04/2017.
 */

class AppsTagListFragment : WatchListFragment() {

    override fun mapAction(it: WishListAction): WishListAction {
        if (it is EmptyButton) {
            return when (it.idx) {
                1 -> {
                    val tag = viewModel.tag!!
                    AddAppToTag(tag)
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

    override fun createEmptyViewHolder(emptyBinding: ListItemEmptyBinding, action: MutableSharedFlow<WishListAction>): EmptyViewHolder {
        emptyBinding.emptyText.setText(R.string.tags_list_is_empty)
        viewModel.tag?.let {
            val button = emptyBinding.button1
            button.setBackgroundColor(it.color)
            if (it.isLightColor) {
                button.setTextColor(ResourcesCompat.getColor(resources, R.color.black, context?.theme))
            } else {
                button.setTextColor(ResourcesCompat.getColor(resources, R.color.white, context?.theme))
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
    ) : FragmentFactory("apps-tags-$filterId-$sortId-${tag?.hashCode()}") {

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
