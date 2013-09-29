package com.anod.appwatcher;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.appwatcher.accounts.AccountHelper;
import com.anod.appwatcher.market.AppLoader;
import com.anod.appwatcher.market.DeviceIdHelper;
import com.anod.appwatcher.market.MarketSessionHelper;
import com.anod.appwatcher.utils.AppLog;
import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.model.Market.App;

public class ChangelogActivity extends FragmentActivity{

	public static final String EXTRA_APP_ID = "app_id";
	private AppLoader mLoader;
	private ProgressBar mLoadingView;
	private TextView mChangelog;
	private MarketSession mMarketSession;
	private String mAppId;
	private Button mRetryButton;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_changelog);
        
		Intent data = getIntent();

		mAppId = data.getStringExtra(EXTRA_APP_ID);

		final Preferences prefs = new Preferences(this);
        String deviceId = DeviceIdHelper.getDeviceId(this, prefs);
        
        MarketSessionHelper helper = new MarketSessionHelper(this);
        mMarketSession = helper.create(deviceId, null);
        
        mLoader = new AppLoader(mMarketSession, true);
		
        mLoadingView = (ProgressBar)findViewById(R.id.progress_bar);
        mChangelog = (TextView)findViewById(R.id.changelog);
		mRetryButton = (Button)findViewById(R.id.retry);
		mLoadingView.setVisibility(View.VISIBLE);
		mRetryButton.setVisibility(View.GONE);
		mChangelog.setVisibility(View.GONE);
		mRetryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new RetrieveResultsTask().execute(mAppId);
			}
		});

		AccountHelper accHelper = new AccountHelper(this);
		accHelper.requestToken(this, prefs.getAccount(), new AccountHelper.AuthenticateCallback() {
			@Override
			public void onAuthTokenAvailable(String token) {
				mMarketSession.setAuthSubToken(token);
				new RetrieveResultsTask().execute(mAppId);
			}

			@Override
			public void onUnRecoverableException(String errorMessage) {

			}
		});

	}
	
    class RetrieveResultsTask extends AsyncTask<String, Void, App> {

		@Override
		protected void onPreExecute() {
			mLoadingView.setVisibility(View.VISIBLE);
			mRetryButton.setVisibility(View.GONE);
			mChangelog.setVisibility(View.GONE);
		}

		protected App doInBackground(String... appsId) {
			AppLog.d("App Id: "+appsId[0]);
			try {
				return mLoader.load(appsId[0]);
			} catch (Exception e) {
				AppLog.e("Retrieve change log error", e);
				return null;
			}
        }
        
        @Override
        protected void onPostExecute(App app) {
        	mLoadingView.setVisibility(View.GONE);
        	mChangelog.setVisibility(View.VISIBLE);
        	mChangelog.setAutoLinkMask(Linkify.ALL);
			if (app == null) {
				mChangelog.setText(getString(R.string.error_fetchin_info));
				mRetryButton.setVisibility(View.VISIBLE);
				return;
			}
			mRetryButton.setVisibility(View.GONE);
        	String changes = "";
			if (app.getExtendedInfo() != null) {
				changes = app.getExtendedInfo().getRecentChanges();
			}
        	if (changes.equals("")) {
        		mChangelog.setText(R.string.no_recent_changes);
        	} else {
        		mChangelog.setText(app.getExtendedInfo().getRecentChanges());
        	}
        }
    }

}
