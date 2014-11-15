package com.anod.appwatcher.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.R;
import com.anod.appwatcher.utils.ErrorReport;
import com.anod.appwatcher.utils.IntentUtils;

import de.psdev.licensesdialog.LicensesDialog;

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

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View aboutView = inflater.inflate(R.layout.about, null);

		String text = getString(R.string.dialog_about_text);
		TextView lastUpdateView = (TextView)aboutView.findViewById(R.id.last_update);
		if (time > 0) {
			String lastUpdate = getString(R.string.last_update, DateUtils.formatDateTime(getActivity(), time, DATE_FORMAT));
			lastUpdateView.setText(lastUpdate);
			lastUpdateView.setVisibility(View.VISIBLE);
		} else {
			lastUpdateView.setVisibility(View.GONE);
		}

    	Spanned message = Html.fromHtml(text);
    	TextView messageView = (TextView)aboutView.findViewById(R.id.about_text);
    	messageView.setMovementMethod(LinkMovementMethod.getInstance());
    	messageView.setText(message);

		Button licencesButton = (Button)aboutView.findViewById(R.id.licences);
		licencesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
			   new LicensesDialog(getActivity(), R.raw.notices, false, true).show();
			}
		});

		Button rateBtn = (Button)aboutView.findViewById(R.id.rate_btn);
		rateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String pkg = getActivity().getPackageName();
				Intent rateIntent = IntentUtils.createPlayStoreIntent(pkg);
				startActivity(rateIntent);
			}
		});


		Button reportBtn = (Button)aboutView.findViewById(R.id.report_btn);
		reportBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
            ErrorReport.reportByEmail(getActivity());
			}
		});

        return new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setView(aboutView)
            .setPositiveButton(android.R.string.ok, 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }
            )
            .create();
    }
}
