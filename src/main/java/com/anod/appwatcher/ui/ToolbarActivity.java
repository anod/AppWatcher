package com.anod.appwatcher.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.R;

/**
 * @author alex
 * @date 2015-06-20
 */
public class ToolbarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Preferences prefs = new Preferences(this);
        AppCompatDelegate.setDefaultNightMode(prefs.getNightMode());
        super.onCreate(savedInstanceState);
    }

    protected void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //set the Toolbar as ActionBar
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //if (!getSupportFragmentManager().popBackStackImmediate()) {
                //    NavUtils.navigateUpFromSameTask(this);
                //}
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected Bundle getIntentExtras()
    {
        if (getIntent() == null || getIntent().getExtras() == null)
        {
            return new Bundle();
        }
        return getIntent().getExtras();
    }
}
