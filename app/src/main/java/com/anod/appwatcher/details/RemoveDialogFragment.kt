package com.anod.appwatcher.details

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.anod.appwatcher.R
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.utils.appScope
import com.anod.appwatcher.utils.appsDatabase
import info.anodsplace.framework.app.DialogMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class RemoveDialogFragment : DialogFragment(), KoinComponent {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = requireArguments().getString(ARG_TITLE)
        val rowId = requireArguments().getInt(ARG_ROW_ID)
        val message = getString(R.string.alert_dialog_remove_message, title)

        return DialogMessage(requireActivity(), R.style.AppTheme_Dialog, R.string.alert_dialog_remove_title, message) { builder ->
            builder.setPositiveButton(R.string.alert_dialog_remove) { _, _ ->
                appScope.launch(Dispatchers.Main) {
                    appsDatabase.apps().updateStatus(rowId, AppInfoMetadata.STATUS_DELETED)
                }
            }

            builder.setNegativeButton(R.string.alert_dialog_cancel) { _, _ -> }
        }.create()
    }

    companion object {
        private const val ARG_ROW_ID = "rowId"
        private const val ARG_TITLE = "title"

        fun newInstance(title: String, rowId: Int) = RemoveDialogFragment().apply {
            arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_ROW_ID to rowId
            )
        }
    }
}