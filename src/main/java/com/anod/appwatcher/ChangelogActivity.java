package com.anod.appwatcher;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
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
import com.google.android.finsky.protos.DocDetails;


public class ChangelogActivity extends ActionBarActivity implements PlayStoreEndpoint.Listener {

	public static final String EXTRA_APP_ID = "app_id";
    public static final String EXTRA_DETAILS_URL = "url";
    private DetailsEndpoint mDetailsEndpoint;
	private ProgressBar mLoadingView;
	private TextView mChangelog;
	private String mAppId;
	private Button mRetryButton;
    private String mDetailsUrl;

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_changelog);
        
		Intent data = getIntent();

		mAppId = data.getStringExtra(EXTRA_APP_ID);
        mDetailsUrl = data.getStringExtra(EXTRA_DETAILS_URL);


        mDetailsEndpoint = new DetailsEndpoint(this,this);
        mDetailsEndpoint.setUrl(mDetailsUrl);

        mLoadingView = (ProgressBar)findViewById(R.id.progress_bar);
        mChangelog = (TextView)findViewById(R.id.changelog);
		mRetryButton = (Button)findViewById(R.id.retry);
		mLoadingView.setVisibility(View.VISIBLE);
		mRetryButton.setVisibility(View.GONE);
		mChangelog.setVisibility(View.GONE);
		mRetryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                mDetailsEndpoint.startAsync();
			}
		});

		AccountHelper accHelper = new AccountHelper(this);
        final Preferences prefs = new Preferences(this);
        final Account account = prefs.getAccount();
		accHelper.requestToken(this, account, new AccountHelper.AuthenticateCallback() {
			@Override
			public void onAuthTokenAvailable(String token) {
                mDetailsEndpoint.setAccount(account, token);
                mDetailsEndpoint.startAsync();
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
        String changes = mDetailsEndpoint.getRecentChanges();
        if (changes == null || changes.equals("")) {
            mChangelog.setText(R.string.no_recent_changes);
        } else {
            mChangelog.setText(Html.fromHtml(changes));
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
