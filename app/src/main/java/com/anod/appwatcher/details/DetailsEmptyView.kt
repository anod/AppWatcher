// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.anod.appwatcher.R

class DetailsEmptyView : Fragment() {

    companion object {
        const val tag = "detail-empty-view"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail_empty, container, false)
    }
}
