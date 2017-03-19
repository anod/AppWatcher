package com.anod.appwatcher.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.content.DbContentProviderClient;

public class RemoveDialogFragment extends DialogFragment {

    private static final String ARG_ROW_ID = "rowId";
    private static final String ARG_TITLE = "title";

    public static RemoveDialogFragment newInstance(String title, int rowId) {
        RemoveDialogFragment frag = new RemoveDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_ROW_ID, rowId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(ARG_TITLE);
        final int rowId = getArguments().getInt(ARG_ROW_ID);
        String message = getString(R.string.alert_dialog_remove_message, title);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.alert_dialog_remove_title)
                .setMessage(message)
                .setPositiveButton(R.string.alert_dialog_remove,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DbContentProviderClient cl = new DbContentProviderClient(getActivity());
                                cl.updateStatus(rowId, AppInfo.STATUS_DELETED);
                                cl.close();
                                getActivity().finish();
                            }
                        }
                )
                .setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }
                )
                .create();
    }
}
