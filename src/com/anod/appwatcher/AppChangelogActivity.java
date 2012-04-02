package com.anod.appwatcher;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.anod.appwatcher.market.AppExtendedInfoLoader;
import com.anod.appwatcher.market.MarketSessionHelper;
import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.model.Market.App;

public class AppChangelogActivity extends SherlockActivity {

	public static final String EXTRA_APP_ID = "app_id";
	private AppExtendedInfoLoader mLoader;
	private ProgressBar mLoadingView;
	private TextView mChangelog;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_changelog);
		
		Intent data = getIntent();
		
		String appId = data.getStringExtra(EXTRA_APP_ID);
        String authSubToken = data.getStringExtra(MarketSessionHelper.EXTRA_TOKEN);
        final Preferences prefs = new Preferences(this);
        String deviceId = prefs.getDeviceId();
        
        MarketSessionHelper helper = new MarketSessionHelper(this);
        final MarketSession session = helper.create(deviceId, authSubToken);
        
        mLoader = new AppExtendedInfoLoader(session, appId);
		
        mLoadingView = (ProgressBar)findViewById(R.id.progress_bar);
        mChangelog = (TextView)findViewById(R.id.changelog);
        mChangelog.setVisibility(View.GONE);
        new RetreiveResultsTask().execute(appId);
	}

	
    class RetreiveResultsTask extends AsyncTask<String, Void, App> {
        protected App doInBackground(String... queries) {
        	return mLoader.load();
        }
        
        @Override
        protected void onPostExecute(App app) {
        	mLoadingView.setVisibility(View.GONE);
        	mChangelog.setVisibility(View.VISIBLE);
        	mChangelog.setText(app.getExtendedInfo().getRecentChanges());
        }

    };	
}
