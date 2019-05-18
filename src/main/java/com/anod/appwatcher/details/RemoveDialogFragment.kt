package com.anod.appwatcher.details

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.content.AddWatchAppAsyncTask
import com.anod.appwatcher.model.AppInfoMetadata
import info.anodsplace.framework.app.DialogMessage
import info.anodsplace.framework.os.BackgroundTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RemoveDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments!!.getString(ARG_TITLE)
        val rowId = arguments!!.getInt(ARG_ROW_ID)
        val message = getString(R.string.alert_dialog_remove_message, title)

        return DialogMessage(activity!!, R.style.AlertDialog, R.string.alert_dialog_remove_title, message) { builder ->
            val db = Application.provide(activity!!).database
            builder.setPositiveButton(R.string.alert_dialog_remove) { _, _ ->
                GlobalScope.launch(Dispatchers.Main) {
                    db.apps().updateStatus(rowId, AppInfoMetadata.STATUS_DELETED)
                    activity?.finish()
                }
            }

            builder.setNegativeButton(R.string.alert_dialog_cancel) { _, _ -> }
        }.create()
    }

    companion object {
        private const val ARG_ROW_ID = "rowId"
        private const val ARG_TITLE = "title"

        fun newInstance(title: String, rowId: Int): RemoveDialogFragment {
            val frag = RemoveDialogFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putInt(ARG_ROW_ID, rowId)
            frag.arguments = args
            return frag
        }
    }
}
