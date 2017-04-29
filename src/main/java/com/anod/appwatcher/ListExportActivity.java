package com.anod.appwatcher;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.anod.appwatcher.backup.DbBackupManager;

public class ListExportActivity extends FragmentActivity {
	private DbBackupManager mBackupManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_list_import);

		mBackupManager = new DbBackupManager(this);

	}

	public DbBackupManager getBackupManager() {
		return mBackupManager;
	}

}
