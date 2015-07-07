package com.anod.appwatcher;

import android.accounts.Account;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.anod.appwatcher.accounts.AuthTokenProvider;
import com.anod.appwatcher.market.DetailsEndpoint;
import com.anod.appwatcher.market.PlayStoreEndpoint;
import com.anod.appwatcher.ui.ToolbarActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ChangelogActivity extends ToolbarActivity implements PlayStoreEndpoint.Listener {

    public static final String EXTRA_APP_ID = "app_id";
    public static final String EXTRA_DETAILS_URL = "url";

    @InjectView(R.id.progress_bar)
    ProgressBar mLoadingView;
    @InjectView(R.id.changelog)
    TextView mChangelog;
    @InjectView(R.id.retry)
    Button mRetryButton;

    private String mDetailsUrl;
    private String mAppId;

    private DetailsEndpoint mDetailsEndpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_changelog);
        ButterKnife.inject(this);
        setupToolbar();

        Intent data = getIntent();

        mAppId = data.getStringExtra(EXTRA_APP_ID);
        mDetailsUrl = data.getStringExtra(EXTRA_DETAILS_URL);

        mDetailsEndpoint = new DetailsEndpoint(this, this);
        mDetailsEndpoint.setUrl(mDetailsUrl);

        mLoadingView.setVisibility(View.VISIBLE);
        mRetryButton.setVisibility(View.GONE);
        mChangelog.setVisibility(View.GONE);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDetailsEndpoint.startAsync();
            }
        });

        AuthTokenProvider accHelper = new AuthTokenProvider(this);
        final Preferences prefs = new Preferences(this);
        final Account account = prefs.getAccount();
        accHelper.requestToken(this, account, new AuthTokenProvider.AuthenticateCallback() {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.changelog, menu);
        return true;
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
