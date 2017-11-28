package com.anod.appwatcher.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anod.appwatcher.R
import com.anod.appwatcher.watchlist.WatchListFragment
import com.anod.appwatcher.model.Tag

/**
 * @author algavris
 * *
 * @date 01/04/2017.
 */

class AppsTagListFragment : WatchListFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_appstag_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(android.R.id.button1)?.setOnClickListener {
            startActivity(AppsTagSelectActivity.createIntent(tag!!, activity!!))
        }
    }

    companion object {
        fun newInstance(filterId: Int, sortId: Int, sectionProvider: WatchListFragment.SectionProvider, tag: Tag?): AppsTagListFragment {
            val frag = AppsTagListFragment()
            frag.arguments = WatchListFragment.createArguments(filterId, sortId, sectionProvider, tag)
            return frag
        }
    }
}
