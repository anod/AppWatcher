package com.anod.appwatcher.details

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.Theme
import java.sql.RowId

class DetailsDialog: DialogFragment(R.layout.activity_app_changelog) {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            setStyle(STYLE_NORMAL, Theme(context).themeDialogNoActionBar)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appId = requireArguments().getString(DetailsFragment.extraAppId) ?: ""
        val detailsUrl = requireArguments().getString(DetailsFragment.extraDetailsUrl) ?: ""
        val rowId = requireArguments().getInt(DetailsFragment.extraRowId, -1)
        if (savedInstanceState == null) {
            childFragmentManager.commitNow {
                replace(R.id.content, DetailsFragment.newInstance(appId, detailsUrl, rowId), DetailsFragment.tag)
            }
        }
    }

    companion object {

        fun show(appId: String, rowId: Int, detailsUrl: String?, fm: FragmentManager) {
            DetailsDialog().apply {
                arguments = bundleOf(
                        DetailsFragment.extraAppId to appId,
                        DetailsFragment.extraRowId to rowId,
                        DetailsFragment.extraDetailsUrl to detailsUrl
                )
                show(fm, DetailsDialog::class.java.simpleName)
            }
        }
    }
}