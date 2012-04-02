package com.anod.appwatcher;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

public class AppChangelogActivity extends SherlockActivity {

	public static final String EXTRA_APP_ID = "app_id";

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_changelog);
		
		String appId = getIntent().getStringExtra(EXTRA_APP_ID);
	}

}
