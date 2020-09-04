package com.anod.appwatcher.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.utils.SingleLiveEvent
import com.anod.appwatcher.watchlist.*
import info.anodsplace.framework.app.FragmentFactory

/**
 * @author Alex Gavrishev
 * *
 * @date 01/04/2017.
 */

class AppsTagListFragment : WatchListFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_appstag_list, container, false)
    }

    override fun mapEmptyAction(it: WishListAction): WishListAction {
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

    override fun config(filterId: Int) = WatchListPagingSource.Config(
            showRecentlyUpdated = prefs.showRecentlyUpdated,
            showOnDevice = false,
            showRecentlyInstalled = false
    )

    override fun createEmptyViewHolder(emptyView: View, action: SingleLiveEvent<WishListAction>): EmptyViewHolder {
        emptyView.findViewById<TextView>(R.id.emptyText).setText(R.string.tags_list_is_empty)
        emptyView.findViewById<Button>(R.id.button2).isVisible = false
        emptyView.findViewById<Button>(R.id.button3).isVisible = false
        return EmptyViewHolder(emptyView, true, action)
    }

    class Factory(
            private val filterId: Int,
            private val sortId: Int,
            private val tag: Tag?
    ) : FragmentFactory("apps-tags-$filterId-$sortId-${tag?.hashCode()}") {

        override fun create(): Fragment? = AppsTagListFragment().also {
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
