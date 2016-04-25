package com.anod.appwatcher;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.anod.appwatcher.accounts.AccountChooserHelper;
import com.anod.appwatcher.adapters.MarketSearchAdapter;
import com.anod.appwatcher.fragments.AccountChooserFragment;
import com.anod.appwatcher.market.SearchEndpoint;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.AddWatchAppHandler;
import com.anod.appwatcher.ui.ToolbarActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MarketSearchActivity extends ToolbarActivity implements AccountChooserHelper.OnAccountSelectionListener, SearchEndpoint.Listener, AddWatchAppHandler.Listener {
    public static final String EXTRA_KEYWORD = "keyword";
    public static final String EXTRA_EXACT = "exact";
    public static final String EXTRA_SHARE = "share";

    private MarketSearchAdapter mAdapter;
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

    private AccountChooserHelper mAccChooserHelper;
    private SearchEndpoint mSearchEngine;
    private AddWatchAppHandler mNewAppHandler;
    private AppListContentProviderClient mContentProviderClient;
    private String mSearchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_search);
        setupToolbar();
        mContext = this;

        ButterKnife.bind(this);

        mLoading.setVisibility(View.GONE);

        mSearchEngine = new SearchEndpoint(this);
        mNewAppHandler = new AddWatchAppHandler(this, this);
        mAdapter = new MarketSearchAdapter(this, mSearchEngine, mNewAppHandler);

        mListView.setLayoutManager(new LinearLayoutManager(this));
        mListView.setAdapter(mAdapter);

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
        mInitiateSearch = i.getBooleanExtra(EXTRA_EXACT, false);
        mShareSource = i.getBooleanExtra(EXTRA_SHARE, false);
    }

    @Override
    protected void onPause() {
        if (mContentProviderClient != null) {
            mContentProviderClient.release();
        }
        super.onPause();
        mNewAppHandler.setContentProvider(null);
        mSearchEngine.setListener(null);
    }

    @Override
    protected void onResume() {
        mContentProviderClient = new AppListContentProviderClient(mContext);
        super.onResume();

        mNewAppHandler.setContentProvider(mContentProviderClient);
        mSearchEngine.setListener(this);

        mAccChooserHelper = new AccountChooserHelper(this, new Preferences(this), this);
        mAccChooserHelper.init();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void searchResults() {

        mListView.setAdapter(mAdapter);

        mSearchEngine.reset();

        showLoading();

        if (!TextUtils.isEmpty(mSearchQuery)) {
            mSearchEngine.setQuery(mSearchQuery).startAsync();
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
                hideKeyboard();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return false;
            }
        });

        mSearchView.setQuery(mSearchQuery, true);
        hideKeyboard();
        return true;
    }

    private void hideKeyboard() {
        // hide virtual keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
    }

    private void searchResultsDelayed() {
        if (mSearchEngine.getAuthSubToken() == null) {
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
        mSearchEngine.setAccount(account, authSubToken);
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
    public AccountChooserFragment.OnAccountSelectionListener getAccountSelectionListener() {
        return mAccChooserHelper;
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
            mSearchEngine.startAsync();
        }
    }

    @Override
    public void onDataChanged() {
        if (mSearchEngine.getData().getCount() == 0) {
            showNoResults(mSearchEngine.getQuery());
        } else {
            showListView();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mLoading.setVisibility(View.GONE);
        showRetryButton();
    }

    @Override
    public void onAppAddSuccess(AppInfo info) {
        String msg = mContext.getString(R.string.app_stored, info.getTitle());
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

}
