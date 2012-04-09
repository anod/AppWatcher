package com.anod.appwatcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class AboutDialog extends SherlockDialogFragment {
	
	public static AboutDialog newInstance() {
		AboutDialog frag = new AboutDialog();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	Spanned text = Html.fromHtml(getString(R.string.dialog_about_text));
        return new AlertDialog.Builder(getActivity())
            .setTitle(R.string.dialog_about_title)
            .setMessage(text)
            .setPositiveButton(android.R.string.ok, 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }
            )
            .create();
    }
}
