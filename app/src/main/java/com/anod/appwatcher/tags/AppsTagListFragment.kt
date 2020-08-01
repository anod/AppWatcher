package com.anod.appwatcher.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.paging.ExperimentalPagingApi
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.utils.SingleLiveEvent
import com.anod.appwatcher.watchlist.AddAppToTag
import com.anod.appwatcher.watchlist.Section
import com.anod.appwatcher.watchlist.WatchListFragment
import com.anod.appwatcher.watchlist.WishListAction
import info.anodsplace.framework.app.FragmentFactory
import info.anodsplace.framework.view.setOnSafeClickListener
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * @author Alex Gavrishev
 * *
 * @date 01/04/2017.
 */

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class AppsTagListFragment : WatchListFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_appstag_list, container, false)
    }

    override fun configureEmptyView(emptyView: View, action: SingleLiveEvent<WishListAction>) {
        val tag = section.viewModel(this).tag!!
        emptyView.findViewById<TextView>(R.id.emptyText).setText(R.string.tags_list_is_empty)
        emptyView.findViewById<Button>(R.id.button1)?.let {
            it.setBackgroundColor(tag.color)
            it.setOnSafeClickListener {
                action.value = AddAppToTag(tag)
            }
        }
        emptyView.findViewById<Button>(R.id.button2).isVisible = false
        emptyView.findViewById<Button>(R.id.button3).isVisible = false
    }

    class Factory(
            private val filterId: Int,
            private val sortId: Int,
            private val sectionClass: Class<Section>,
            private val tag: Tag?
    ) : FragmentFactory("apps-tags-$filterId-$sortId-${sectionClass.name}-${tag?.hashCode()}") {

        override fun create(): Fragment? = AppsTagListFragment().also {
            it.arguments = Bundle().apply {
                putInt(ARG_FILTER, filterId)
                putInt(ARG_SORT, sortId)
                putString(ARG_SECTION_PROVIDER, sectionClass.name)
                tag?.let { tag ->
                    putParcelable(ARG_TAG, tag)
                }
            }
        }
    }

}
