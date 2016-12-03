package com.anod.appwatcher;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.anod.appwatcher.backup.ListBackupManager;

public class ListExportActivity extends FragmentActivity {
	private ListBackupManager mBackupManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_list_import);

		mBackupManager = new ListBackupManager(this);

	}

	public ListBackupManager getBackupManager() {
		return mBackupManager;
	}

}
