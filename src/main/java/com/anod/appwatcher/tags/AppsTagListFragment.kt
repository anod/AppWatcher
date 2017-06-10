package com.anod.appwatcher.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import butterknife.Optional
import com.anod.appwatcher.R
import com.anod.appwatcher.fragments.AppWatcherListFragment
import com.anod.appwatcher.model.Tag

/**
 * @author algavris
 * *
 * @date 01/04/2017.
 */

class AppsTagListFragment : AppWatcherListFragment() {

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_appstag_list, container, false)
    }

    @OnClick(android.R.id.button1)
    @Optional
    override fun onSearchButton() {
        startActivity(AppsTagSelectActivity.createIntent(mTag!!, activity))
    }

    companion object {
        fun newInstance(filterId: Int, sortId: Int, sectionProvider: AppWatcherListFragment.SectionProvider, tag: Tag?): AppsTagListFragment {
            val frag = AppsTagListFragment()
            frag.arguments = AppWatcherListFragment.createArguments(filterId, sortId, sectionProvider, tag)
            return frag
        }
    }
}
