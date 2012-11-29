package com.anod.appwatcher.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.anod.appwatcher.R;

public class WaitDialogFragment extends DialogFragment {

	public static WaitDialogFragment newInstance() {
		WaitDialogFragment frag = new WaitDialogFragment();
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setMessage(getString(R.string.please_wait));
		return dialog;
	}
}
