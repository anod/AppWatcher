package com.anod.appwatcher.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.watchlist.Section
import com.anod.appwatcher.watchlist.WatchListFragment
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = section.viewModel(this)
        view.findViewById<Button>(android.R.id.button1)?.let {
            it.setBackgroundColor(viewModel.tag!!.color)
            it.setOnClickListener {
                startActivityForResult(AppsTagSelectActivity.createIntent(viewModel.tag!!, requireActivity()), REQUEST_TAGS_SELECT)
            }
        }
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

    companion object {
        private const val REQUEST_TAGS_SELECT = 2
    }
}
