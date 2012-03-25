package com.anod.appwatcher;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockListActivity;

public class AppWatcherActivity extends SherlockListActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}