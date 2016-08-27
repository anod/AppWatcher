package com.anod.appwatcher;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.anod.appwatcher.accounts.AccountChooserHelper;
import com.anod.appwatcher.fragments.AccountChooserFragment;
import com.anod.appwatcher.market.CompositeStateEndpoint;
import com.anod.appwatcher.market.DetailsEndpoint;
import com.anod.appwatcher.market.PlayStoreEndpointBase;
import com.anod.appwatcher.market.SearchEndpoint;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.AddWatchAppHandler;
import com.anod.appwatcher.search.ResultsAdapter;
import com.anod.appwatcher.search.ResultsAdapterDetails;
import com.anod.appwatcher.search.ResultsAdapterSearch;
import com.anod.appwatcher.ui.ToolbarActivity;
import com.anod.appwatcher.utils.DocUtils;
import com.anod.appwatcher.utils.Keyboard;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MarketSearchActivity extends ToolbarActivity implements AccountChooserHelper.OnAccountSelectionListener, AddWatchAppHandler.Listener, CompositeStateEndpoint.Listener {
    public static final String EXTRA_KEYWORD = "keyword";
    public static final String EXTRA_EXACT = "exact";
    public static final String EXTRA_SHARE = "share";
    public static final String EXTRA_FOCUS = "focus";
    public static final String EXTRA_PACKAGE = "package";

    private static final int DETAILS_ENDPOINT_ID = 0;
    private static final int SEARCH_ENDPOINT_ID = 1;

    private ResultsAdapter mAdapter;
    private Context mContext;

    @Bind(R.id.loading)
    LinearLayout mLoading;
    @Bind(android.R.id.list)
    RecyclerView mListView;
    @Bind(android.R.id.empty)
    TextView mEmptyView;
    @Bind(R.id.retry_box)
    LinearLayout mRetryView;
    @Bind(R.id.retry)
    Button mRetryButton;
    SearchView mSearchView;

    private boolean mInitiateSearch = false;
    private boolean mShareSource = false;

    private AccountChooserHelper mAccountChooserHelper;
    private AddWatchAppHandler mNewAppHandler;
    private AppListContentProviderClient mContentProviderClient;
    private String mSearchQuery;
    private boolean mFocus;
    private boolean mPackageSearch;
    private CompositeStateEndpoint mEndpoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_search);
        setupToolbar();
        mContext = this;

        ButterKnife.bind(this);

        mLoading.setVisibility(View.GONE);

        mEndpoints = new CompositeStateEndpoint(this);
        mEndpoints.add(SEARCH_ENDPOINT_ID, new SearchEndpoint(this));
        mEndpoints.add(DETAILS_ENDPOINT_ID, new DetailsEndpoint(this));

        mNewAppHandler = new AddWatchAppHandler(this, this);

        mListView.setLayoutManager(new LinearLayoutManager(this));

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrySearchResult();
            }
        });

        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
        mRetryView.setVisibility(View.GONE);

        initFromIntent(getIntent());
    }

    private void initFromIntent(Intent i) {
        if (i == null) {
            return;
        }
        String keyword = i.getStringExtra(EXTRA_KEYWORD);
        if (keyword != null) {
            mSearchQuery = keyword;
        }
        mPackageSearch = i.getBooleanExtra(EXTRA_PACKAGE, false);
        mInitiateSearch = i.getBooleanExtra(EXTRA_EXACT, false);
        mShareSource = i.getBooleanExtra(EXTRA_SHARE, false);
        mFocus = i.getBooleanExtra(EXTRA_FOCUS, false);


        if (mPackageSearch) {
            mEndpoints.setActive(DETAILS_ENDPOINT_ID);
            mAdapter = new ResultsAdapterDetails(this, (DetailsEndpoint) mEndpoints.get(DETAILS_ENDPOINT_ID), mNewAppHandler);
        } else {
            mEndpoints.setActive(SEARCH_ENDPOINT_ID);
            mAdapter = new ResultsAdapterSearch(this, (SearchEndpoint) mEndpoints.get(SEARCH_ENDPOINT_ID), mNewAppHandler);
        }
    }

    @Override
    protected void onPause() {
        if (mContentProviderClient != null) {
            mContentProviderClient.release();
        }
        super.onPause();
        mNewAppHandler.setContentProvider(null);
        mEndpoints.setListener(null);
    }

    @Override
    protected void onResume() {
        mContentProviderClient = new AppListContentProviderClient(mContext);
        super.onResume();

        mNewAppHandler.setContentProvider(mContentProviderClient);

        mAccountChooserHelper = new AccountChooserHelper(this, new Preferences(this), this);
        mAccountChooserHelper.init();
    }

    private void searchResults() {
        mListView.setAdapter(mAdapter);
        mEndpoints.reset();
        showLoading();

        if (!TextUtils.isEmpty(mSearchQuery)) {
            if (mPackageSearch)
            {
                String url = DocUtils.getUrl(mSearchQuery);
                ((DetailsEndpoint) mEndpoints.get(DETAILS_ENDPOINT_ID)).setUrl(url);
            }
            ((SearchEndpoint) mEndpoints.get(SEARCH_ENDPOINT_ID)).setQuery(mSearchQuery);
            mEndpoints.active().startAsync();
        } else {
            showNoResults("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchbox, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setIconifiedByDefault(false);
        MenuItemCompat.expandActionView(searchItem);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchQuery = query;
                searchResultsDelayed();
                Keyboard.hide(mSearchView, MarketSearchActivity.this);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return false;
            }
        });

        mSearchView.setQuery(mSearchQuery, true);
        if (mFocus) {
            mSearchView.post(new Runnable() {
                @Override
                public void run() {
                    mSearchView.requestFocus();
                }
            });
        } else {
            Keyboard.hide(mSearchView, this);
        }
        return true;
    }

    private void searchResultsDelayed() {
        if (mEndpoints.getAuthSubToken() == null) {
            mInitiateSearch = true;
        } else {
            searchResults();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                searchResultsDelayed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onHelperAccountSelected(Account account, String authSubToken) {
        if (authSubToken == null) {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mEndpoints.setAccount(account, authSubToken);
        if (mInitiateSearch && !TextUtils.isEmpty(mSearchQuery)) {
            searchResults();
        } else {
            showNoResults("");
        }
    }

    @Override
    public void onHelperAccountNotFound() {
        Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mAccountChooserHelper.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    public AccountChooserFragment.OnAccountSelectionListener getAccountSelectionListener() {
        return mAccountChooserHelper;
    }

    private void showRetryButton() {
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
        mRetryView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        mListView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
        mRetryView.setVisibility(View.GONE);
    }

    private void showNoResults(String query) {
        mLoading.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        mRetryView.setVisibility(View.GONE);
        String noResStr = (query.length() > 0) ? getString(R.string.no_result_found, query) : getString(R.string.search_for_app);
        mEmptyView.setText(noResStr);
        mEmptyView.setVisibility(View.VISIBLE);
    }

    private void showListView() {
        mListView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
        mRetryView.setVisibility(View.GONE);
    }

    private void retrySearchResult() {
        if (mAdapter.isEmpty()) {
            searchResultsDelayed();
        } else {
            mEndpoints.active().startAsync();
        }
    }

    @Override
    public void onAppAddSuccess(AppInfo info) {
        String msg = mContext.getString(R.string.app_stored, info.title);
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        mAdapter.notifyDataSetChanged();
        if (mShareSource) {
            finish();
        }
    }

    @Override
    public void onAppAddError(AppInfo info, int error) {
        if (AddWatchAppHandler.ERROR_ALREADY_ADDED == error) {
            Toast.makeText(mContext, R.string.app_already_added, Toast.LENGTH_SHORT).show();
            mAdapter.notifyDataSetChanged();
        } else if (error == AddWatchAppHandler.ERROR_INSERT) {
            Toast.makeText(mContext, R.string.error_insert_app, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDataChanged(int id, PlayStoreEndpointBase endpoint) {
        if (id == DETAILS_ENDPOINT_ID)
        {
            if (((DetailsEndpoint)endpoint).getDocument() != null)
            {
                showListView();
                mAdapter.notifyDataSetChanged();
            } else {
                mAdapter = new ResultsAdapterSearch(this, (SearchEndpoint) mEndpoints.get(SEARCH_ENDPOINT_ID), mNewAppHandler);
                mListView.setAdapter(mAdapter);
                mEndpoints.setActive(SEARCH_ENDPOINT_ID).startAsync();
            }
        } else {
            SearchEndpoint searchEndpoint = (SearchEndpoint)endpoint;
            if (searchEndpoint.getData().getCount() == 0) {
                showNoResults(searchEndpoint.getQuery());
            } else {
                showListView();
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onErrorResponse(int id, PlayStoreEndpointBase endpoint, VolleyError error) {
        if (id == DETAILS_ENDPOINT_ID)
        {
            mAdapter = new ResultsAdapterSearch(this, (SearchEndpoint) mEndpoints.get(SEARCH_ENDPOINT_ID), mNewAppHandler);
            mListView.setAdapter(mAdapter);
            mEndpoints.setActive(SEARCH_ENDPOINT_ID).startAsync();
        } else {
            mLoading.setVisibility(View.GONE);
            showRetryButton();
        }
    }
}
