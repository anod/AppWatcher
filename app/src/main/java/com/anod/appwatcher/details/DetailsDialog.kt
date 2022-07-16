package com.anod.appwatcher.details

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.utils.prefs
import org.koin.core.component.KoinComponent

class DetailsDialog : DialogFragment(R.layout.activity_app_changelog), KoinComponent {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setStyle(STYLE_NORMAL, Theme(context, prefs).themeDialogNoActionBar)
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
        fun dismiss(fm: FragmentManager): Boolean {
            val dialog = fm.findFragmentByTag(DetailsDialog::class.java.simpleName) as? DetailsDialog
            if (dialog != null) {
                dialog.dismiss()
                return true
            }
            return false
        }

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