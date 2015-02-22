package com.anod.appwatcher;

import android.accounts.Account;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.anod.appwatcher.accounts.AccountHelper;
import com.anod.appwatcher.market.DetailsEndpoint;
import com.anod.appwatcher.market.DeviceIdHelper;
import com.anod.appwatcher.market.PlayStoreEndpoint;
import com.anod.appwatcher.utils.AppLog;


public class ChangelogActivity extends ActionBarActivity implements PlayStoreEndpoint.Listener {

	public static final String EXTRA_APP_ID = "app_id";
	private DetailsEndpoint mDetailsEndpoint;
	private ProgressBar mLoadingView;
	private TextView mChangelog;
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

        mDetailsEndpoint = new DetailsEndpoint(deviceId,this,this);

        mLoadingView = (ProgressBar)findViewById(R.id.progress_bar);
        mChangelog = (TextView)findViewById(R.id.changelog);
		mRetryButton = (Button)findViewById(R.id.retry);
		mLoadingView.setVisibility(View.VISIBLE);
		mRetryButton.setVisibility(View.GONE);
		mChangelog.setVisibility(View.GONE);
		mRetryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                mDetailsEndpoint.start();
			}
		});

		AccountHelper accHelper = new AccountHelper(this);
        final Account account = prefs.getAccount();
		accHelper.requestToken(this, account, new AccountHelper.AuthenticateCallback() {
			@Override
			public void onAuthTokenAvailable(String token) {
                mDetailsEndpoint.setAccount(account, token);
                mDetailsEndpoint.setAppId(mAppId);
                mDetailsEndpoint.start();
			}

			@Override
			public void onUnRecoverableException(String errorMessage) {

			}
		});

	}

    @Override
    public void onDataChanged() {
        mLoadingView.setVisibility(View.GONE);
        mChangelog.setVisibility(View.VISIBLE);
        mChangelog.setAutoLinkMask(Linkify.ALL);

        mRetryButton.setVisibility(View.GONE);
        String changes = "";
//        if (app.getExtendedInfo() != null) {
//            changes = app.getExtendedInfo().getRecentChanges();
//        }
        if (changes.equals("")) {
            mChangelog.setText(R.string.no_recent_changes);
        } else {
 //           mChangelog.setText(app.getExtendedInfo().getRecentChanges());
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mLoadingView.setVisibility(View.GONE);
        mChangelog.setVisibility(View.VISIBLE);
        mChangelog.setAutoLinkMask(Linkify.ALL);

        mChangelog.setText(getString(R.string.error_fetchin_info));
        mRetryButton.setVisibility(View.VISIBLE);
    }



}
