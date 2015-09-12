package com.anod.appwatcher;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.anod.appwatcher.accounts.AccountChooserHelper;
import com.anod.appwatcher.fragments.AccountChooserFragment;
import com.anod.appwatcher.market.SearchEndpoint;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.ui.ToolbarActivity;
import com.anod.appwatcher.utils.DocUtils;
import com.anod.appwatcher.utils.PackageManagerUtils;
import com.google.android.finsky.api.model.Document;
import com.google.android.finsky.protos.Common;
import com.google.android.finsky.protos.DocDetails;

import java.util.HashMap;

import info.anodsplace.android.log.AppLog;

public class MarketSearchActivity extends ToolbarActivity implements AccountChooserHelper.OnAccountSelectionListener, AccountChooserFragment.OnAccountSelectionListener, SearchEndpoint.Listener {
    public static final String EXTRA_KEYWORD = "keyword";
    public static final String EXTRA_EXACT = "exact";
    public static final String EXTRA_SHARE = "share";

    private AppsAdapter mAdapter;
    private Context mContext;
    private LinearLayout mLoading;
    private RelativeLayout mDeviceIdMessage = null;
    private ListView mListView;
    private boolean mInitiateSearch = false;
    private boolean mShareSource = false;
    private HashMap<String, Boolean> mAddedApps;

    private int mColorBgWhite;
    private int mColorBgGray;
    private SearchView mSearchView;
    private AccountChooserHelper mAccChooserHelper;
    private LinearLayout mRetryView;
    private Button mRetryButton;
    private SearchEndpoint mSearchEngine;
    private int mIconSize = -1;
    private AppListContentProviderClient mContentProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_market_search);

        mContext = this;

        Resources r = mContext.getResources();
        mColorBgGray = r.getColor(R.color.row_inactive);
        mColorBgWhite = r.getColor(R.color.white);

        mAddedApps = new HashMap<String, Boolean>();

        mLoading = (LinearLayout) findViewById(R.id.loading);
        mLoading.setVisibility(View.GONE);

        mSearchEngine = new SearchEndpoint(this);

        mAdapter = new AppsAdapter(this);

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setEmptyView(findViewById(android.R.id.empty));
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(itemClickListener);

        mRetryView = (LinearLayout) findViewById(R.id.retry_box);
        mRetryButton = (Button) findViewById(R.id.retry);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrySearchResult();
            }
        });

        mListView.setVisibility(View.GONE);
        mListView.getEmptyView().setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
        mRetryView.setVisibility(View.GONE);

    }

    @Override
    protected void onPause() {
        if (mContentProviderClient != null) {
            mContentProviderClient.release();
        }
        super.onPause();
        mSearchEngine.setListener(null);
    }

    @Override
    protected void onResume() {
        mSearchEngine.setListener(this);
        mContentProviderClient = new AppListContentProviderClient(mContext);
        super.onResume();
    }

    @Override
    protected void onStop() {
        AppWatcherApplication.get(this).getObjectGraph().reset();
        super.onStop();
    }

    private void searchResults() {
        String query = mSearchView.getQuery().toString();

        // hide virtual keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);

        mListView.setAdapter(mAdapter);

        mSearchEngine.reset();

        showLoading();

        if (query.length() > 0) {
            mSearchEngine.setQuery(query).startAsync();
        } else {
            showNoResults("");
        }
    }


    private void showDeviceIdMessage() {
        if (mDeviceIdMessage != null) {
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.flyin);
            mDeviceIdMessage.setAnimation(anim);
            anim.start();
            mDeviceIdMessage.setVisibility(View.VISIBLE);
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
            public boolean onQueryTextSubmit(String s) {
                searchResultsDelayed();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        initFromIntent(getIntent());

        mAccChooserHelper = new AccountChooserHelper(this, new Preferences(this), this);
        mAccChooserHelper.init();

        return true;
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
                return onOptionsItemSelected(item);
        }
    }

    final OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Document doc = (Document) mAdapter.getItem(position);
            String appId = doc.getDocId();
            if (mAddedApps.containsKey(doc.getDocId())) {
                return;
            }

            final View bgView = view.findViewById(R.id.approw);

            AppInfo existingApp = mContentProviderClient.queryAppId(doc.getDocId());
            if (existingApp != null) {
                Toast.makeText(mContext, R.string.app_already_added, Toast.LENGTH_SHORT).show();
                bgView.setBackgroundColor(mColorBgGray);
                mAddedApps.put(appId, true);
                return;
            }

            String imageUrl = DocUtils.getIconUrl(doc);
            final AppInfo info = new AppInfo(doc, null);
            if (imageUrl != null) {
                if (mIconSize == -1) {
                    mIconSize = mContext.getResources().getDimensionPixelSize(R.dimen.icon_size);
                }
                AppWatcherApplication.provide(mContext).imageLoader().get(imageUrl, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        info.setIcon(response.getBitmap());
                        insertApp(info, bgView);
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        AppLog.e(error);
                        insertApp(info, bgView);
                    }
                }, mIconSize, mIconSize);
            } else {
                insertApp(info, bgView);
            }

        }
    };

    private void insertApp(final AppInfo info, View bgView) {
        Uri uri = mContentProviderClient.insert(info);

        if (uri == null) {
            Toast.makeText(mContext, R.string.error_insert_app, Toast.LENGTH_SHORT).show();
        } else {
            String msg = getString(R.string.app_stored, info.getTitle());
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            bgView.setBackgroundColor(mColorBgGray);
            mAddedApps.put(info.getAppId(), true);
            if (mShareSource) {
                finish();
            }
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
        if (mInitiateSearch && !TextUtils.isEmpty(mSearchView.getQuery())) {
            searchResults();
        } else {
            // hide virtual keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mSearchView, 0);
        }
    }

    @Override
    public void onHelperAccountNotFound() {
        Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
        finish();

    }

    @Override
    public void onDialogAccountSelected(Account account) {
        mAccChooserHelper.onDialogAccountSelected(account);
    }

    @Override
    public void onDialogAccountNotFound() {
        mAccChooserHelper.onDialogAccountNotFound();
    }

    private void showRetryButton() {
        mListView.setVisibility(View.GONE);
        mListView.getEmptyView().setVisibility(View.GONE);
        mRetryView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        mListView.setVisibility(View.GONE);
        mListView.getEmptyView().setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
        mRetryView.setVisibility(View.GONE);
    }

    private void showNoResults(String query) {
        mLoading.setVisibility(View.GONE);
        String noResStr = (query.length() > 0) ? getString(R.string.no_result_found, query) : getString(R.string.search_for_app);
        TextView tv = (TextView) mListView.getEmptyView();
        tv.setText(noResStr);
        tv.setVisibility(View.VISIBLE);
        showDeviceIdMessage();
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
        mLoading.setVisibility(View.GONE);
        if (mSearchEngine.getData().getCount() == 0) {
            showNoResults(mSearchEngine.getQuery());
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mLoading.setVisibility(View.GONE);
        showRetryButton();
    }

    class AppsAdapter extends BaseAdapter {
        private final PackageManagerUtils mPMUtils;
        private final ImageLoader mImageLoader;

        public AppsAdapter(Context context) {
            mContext = context;
            mPMUtils = new PackageManagerUtils(context.getPackageManager());
            mImageLoader = AppWatcherApplication.provide(context).imageLoader();
        }

        class ViewHolder {
            View row;
            TextView title;
            TextView details;
            TextView updated;
            TextView price;
            NetworkImageView icon;
        }

        @Override
        public int getCount() {
            return mSearchEngine.getCount();
        }


        @Override
        public Object getItem(int position) {
            return mSearchEngine.getData().getItem(position, false);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item_market_app, null);
                holder = new ViewHolder();
                holder.row = v.findViewById(R.id.approw);
                holder.title = (TextView) v.findViewById(android.R.id.title);
                holder.details = (TextView) v.findViewById(R.id.details);
                holder.updated = (TextView) v.findViewById(R.id.updated);
                holder.price = (TextView) v.findViewById(R.id.price);
                holder.icon = (NetworkImageView) v.findViewById(android.R.id.icon);
                holder.icon.setDefaultImageResId(R.drawable.ic_android_black_24dp);
                holder.icon.setErrorImageResId(R.drawable.ic_android_black_24dp);

                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            Document doc = (Document) getItem(position);

            DocDetails.AppDetails app = doc.getAppDetails();

            holder.title.setText(doc.getTitle());
            holder.details.setText(doc.getCreator());
            holder.updated.setText(app.uploadDate);

            if (mAddedApps.containsKey(doc.getBackendDocId())) {
                holder.row.setBackgroundColor(mColorBgGray);
            } else {
                holder.row.setBackgroundColor(mColorBgWhite);
            }

            String imageUrl = DocUtils.getIconUrl(doc);
            holder.icon.setImageUrl(imageUrl, mImageLoader);

            boolean isInstalled = mPMUtils.isAppInstalled(app.packageName);
            if (isInstalled) {
                holder.price.setText(R.string.installed);
            } else {
                Common.Offer offer = DocUtils.getOffer(doc);
                if (offer.micros == 0) {
                    holder.price.setText(R.string.free);
                } else {
                    holder.price.setText(offer.formattedAmount);
                }
            }

            return v;
        }

    }


    private void initFromIntent(Intent i) {
        if (i == null) {
            return;
        }
        String keyword = i.getStringExtra(EXTRA_KEYWORD);
        if (keyword != null) {
            mSearchView.setQuery(keyword, true);
        }
        mInitiateSearch = i.getBooleanExtra(EXTRA_EXACT, false);
        mShareSource = i.getBooleanExtra(EXTRA_SHARE, false);
    }
}
