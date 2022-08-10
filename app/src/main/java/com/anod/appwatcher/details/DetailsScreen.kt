package com.anod.appwatcher.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.anod.appwatcher.R
import com.anod.appwatcher.databinding.ViewDetailsContainerBinding

@Composable
fun DetailsScreen(appId: String, rowId: Int, detailsUrl: String) {
    val localContext = LocalContext.current
    AndroidViewBinding(factory = ViewDetailsContainerBinding::inflate) {
        val supportFragmentManager = (localContext as FragmentActivity).supportFragmentManager
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.detailsContainerView, DetailsFragment.newInstance(appId, detailsUrl, rowId))
        }
    }
}