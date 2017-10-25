package com.anod.appwatcher.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.anod.appwatcher.R
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.utils.ApplicationContext

class RemoveDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments.getString(ARG_TITLE)
        val rowId = arguments.getInt(ARG_ROW_ID)
        val message = getString(R.string.alert_dialog_remove_message, title)

        return AlertDialog.Builder(activity)
                .setTitle(R.string.alert_dialog_remove_title)
                .setMessage(message)
                .setPositiveButton(R.string.alert_dialog_remove) { _, _ ->
                    val cl = DbContentProviderClient(activity)
                    cl.updateStatus(rowId, AppInfoMetadata.STATUS_DELETED)
                    cl.close()
                    activity.finish()
                }
                .setNegativeButton(R.string.alert_dialog_cancel) { _, _ -> }
                .create()
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
