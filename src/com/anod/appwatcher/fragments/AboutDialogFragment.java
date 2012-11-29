package com.anod.appwatcher.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.R;

public class AboutDialogFragment extends DialogFragment {
	private static final int DATE_FORMAT = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR;

	public static AboutDialogFragment newInstance() {
		AboutDialogFragment frag = new AboutDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	String versionName = "";
		try {
			versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {	}

    	
		String title = getString(R.string.dialog_about_title, getString(R.string.app_name), versionName);
		
		Preferences pref = new Preferences(getActivity());
		long time = pref.getLastUpdateTime();
		
		String text = getString(R.string.dialog_about_text);
		if (time > 0) {
			String lastUpdate = getString(R.string.last_update, DateUtils.formatDateTime(getActivity(), time, DATE_FORMAT));
			text += "<br/><br/>" + lastUpdate;
		}

		
    	Spanned message = Html.fromHtml(text);
    	LayoutInflater inflater = getActivity().getLayoutInflater();
    	TextView messageView = (TextView)inflater.inflate(R.layout.about, null);
    	messageView.setMovementMethod(LinkMovementMethod.getInstance());
    	messageView.setText(message);
    	
        return new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setView(messageView)
            .setPositiveButton(android.R.string.ok, 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }
            )
            .create();
    }
}
