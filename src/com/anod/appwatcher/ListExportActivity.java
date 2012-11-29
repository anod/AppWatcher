package com.anod.appwatcher;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.anod.appwatcher.fragments.WaitDialogFragment;

public class ListExportActivity extends FragmentActivity {

	private static final String WAIT_DIALOG = "wait_dialog";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.list_export);
		
	}

	
	public void showDialog() {
		DialogFragment dialogFragment = WaitDialogFragment.newInstance();
		dialogFragment.show(getSupportFragmentManager(), WAIT_DIALOG);
	}
	
	public void dismissDialog() {
		DialogFragment dialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(WAIT_DIALOG);
		if (dialogFragment != null) {
			dialogFragment.dismiss();
		}
	}
}
