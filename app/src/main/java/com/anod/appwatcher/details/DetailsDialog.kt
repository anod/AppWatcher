package com.anod.appwatcher.details

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.annotation.FractionRes
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

    override fun onResume() {
        super.onResume()
        adjustToCenterScreen(R.fraction.dialog_width_percentage, R.fraction.dialog_height_percentage)
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


fun DialogFragment.adjustToCenterScreen(@FractionRes widthFraction: Int, @FractionRes heightFraction: Int) {
    val size = getScreenSize()
    setLayoutSize((size.x * resources.getFraction(widthFraction, 1, 1)).toInt(),
            (size.y * resources.getFraction(heightFraction, 1, 1)).toInt())
}

fun DialogFragment.setLayoutSize(width: Int, height: Int, gravity: Int = Gravity.CENTER) {
    val window = dialog?.window
    window?.setLayout(width, height)
    window?.setGravity(gravity)
}

fun DialogFragment.getScreenSize(): Point {
    val window = dialog?.window
    val size = Point()
    window?.windowManager?.defaultDisplay?.getSize(size)
    return size
}