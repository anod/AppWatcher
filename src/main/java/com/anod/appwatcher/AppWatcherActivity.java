package com.anod.appwatcher;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.appwatcher.accounts.AccountChooserHelper;
import com.anod.appwatcher.fragments.AccountChooserFragment;
import com.anod.appwatcher.watchlist.AppWatcherListFragment;
import com.anod.appwatcher.model.Filters;
import com.anod.appwatcher.sync.SyncAdapter;
import com.anod.appwatcher.utils.AppLog;
import com.anod.appwatcher.utils.MenuItemAnimation;
import com.anod.appwatcher.utils.DrawerActivity;

public class AppWatcherActivity extends DrawerActivity implements
        TextView.OnEditorActionListener, SearchView.OnQueryTextListener,
        AccountChooserHelper.OnAccountSelectionListener, AccountChooserFragment.OnAccountSelectionListener {

    public static final String EXTRA_FROM_NOTIFICATION = "extra_noti";
    private boolean mSyncFinishedReceiverRegistered;

    private MenuItemAnimation mRefreshAnim;

    public interface QueryChangeListener {
        void onQueryTextChanged(String newQuery);
    }

    public interface RefreshListener {

        void onRefreshFinish();
    }

    protected String mAuthToken;

    private AppWatcherActivity mContext;
    private Preferences mPreferences;
    private MenuItem mWifiMenuItem;
    private Account mSyncAccount;
    private MenuItem mSearchMenuItem;
    private QueryChangeListener mQueryChangeListener;

    private RefreshListener mRefreshListener;

    private AccountChooserHelper mAccountChooserHelper;
    private boolean mOpenChangelog;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("nav_id", getSupportActionBar().getSelectedNavigationIndex());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.activity_main);
        setupDrawer();

        mContext = this;

        mPreferences = new Preferences(this);

        Intent i = getIntent();
        if (i != null) {
            mOpenChangelog = i.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false);
            i.removeExtra(EXTRA_FROM_NOTIFICATION);
        }

        int filter_id = Filters.NAVDRAWER_ITEM_ALL;
        if (savedInstanceState != null && !mOpenChangelog) {
            filter_id = savedInstanceState.getInt("nav_id", Filters.NAVDRAWER_ITEM_ALL);
            AppLog.d("Restore filter id: " + filter_id);
        }

        if (savedInstanceState == null) {
            AppWatcherListFragment newFragment = AppWatcherListFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, newFragment);
            transaction.commit();
        }

        mRefreshAnim = new MenuItemAnimation(this, R.anim.rotate);

        mAccountChooserHelper = new AccountChooserHelper(this, mPreferences, this);
        mAccountChooserHelper.init();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem autoSyncMenuItem = menu.findItem(R.id.menu_auto_update);
        mWifiMenuItem = menu.findItem(R.id.menu_wifi_only);
        MenuItem refreshMenuItem = menu.findItem(R.id.menu_act_refresh);
        mRefreshAnim.setMenuItem(refreshMenuItem);

        mSearchMenuItem = menu.findItem(R.id.menu_act_filter);
        MenuItemCompat.setOnActionExpandListener(mSearchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                notifyQueryChange("");
                return true;
            }
        });

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchMenuItem);
        searchView.setOnQueryTextListener(this);

        boolean useAutoSync = ContentResolver.getSyncAutomatically(mSyncAccount, AppListContentProvider.AUTHORITY);
        autoSyncMenuItem.setChecked(useAutoSync);

        mWifiMenuItem.setChecked(mPreferences.isWifiOnly());
        if (useAutoSync == false) {
            mWifiMenuItem.setEnabled(false);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            menuTitleUpdateCompat(autoSyncMenuItem, R.string.menu_auto_update);
            menuTitleUpdateCompat(mWifiMenuItem, R.string.menu_wifi_only);
        }

        updateSyncStatus();

        return super.onCreateOptionsMenu(menu);
    }

    private void updateSyncStatus() {
        if (mSyncAccount != null && ContentResolver.isSyncActive(mSyncAccount, AppListContentProvider.AUTHORITY)) {
            mRefreshAnim.start();
        } else {
            mRefreshAnim.stop();
        }
    }


    /**
     * Receive notifications from SyncAdapter
     */
    private BroadcastReceiver mSyncFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SyncAdapter.SYNC_PROGRESS)) {
                mRefreshAnim.start();
            } else if (intent.getAction().equals(SyncAdapter.SYNC_STOP)) {
                int updatesCount = intent.getIntExtra(SyncAdapter.EXTRA_UPDATES_COUNT, 0);
                mRefreshAnim.stop();
                if (updatesCount == 0) {
                    Toast.makeText(AppWatcherActivity.this, R.string.no_updates_found, Toast.LENGTH_SHORT).show();
                }
                if (mRefreshListener != null) {
                    mRefreshListener.onRefreshFinish();
                }
            }
        }

    };

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SyncAdapter.SYNC_PROGRESS);
        filter.addAction(SyncAdapter.SYNC_STOP);
        registerReceiver(mSyncFinishedReceiver, filter);
        mSyncFinishedReceiverRegistered = true;
        super.onResume();

        AppLog.d("Activity::onResume - Mark updates as viewed.");
        mPreferences.markViewed(true);

        notifyQueryChange("");
        if (mSearchMenuItem != null) {
            MenuItemCompat.collapseActionView(mSearchMenuItem);
        }

        updateSyncStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSyncFinishedReceiverRegistered) {
            unregisterReceiver(mSyncFinishedReceiver);
            mSyncFinishedReceiverRegistered = false;
        }
        AppLog.d("Activity::onPause");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_act_refresh:
                requestRefresh();
                return true;
            case R.id.menu_auto_update:
                boolean useAutoSync = !item.isChecked();
                item.setChecked(useAutoSync);
                if (useAutoSync == false) {
                    mWifiMenuItem.setEnabled(false);
                } else {
                    mWifiMenuItem.setEnabled(true);
                }
                // on old version there is no checkboxes
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    menuTitleUpdateCompat(item, R.string.menu_auto_update);
                }
                mAccountChooserHelper.setSync(useAutoSync);
                return true;
            case R.id.menu_wifi_only:
                boolean useWifiOnly = !item.isChecked();
                item.setChecked(useWifiOnly);
                mPreferences.saveWifiOnly(useWifiOnly);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    menuTitleUpdateCompat(item, R.string.menu_wifi_only);
                }
                mAccountChooserHelper.setSync(true);
                return true;
            case R.id.menu_accounts:
                mAccountChooserHelper.showAccountsDialog();
                return true;
            case R.id.menu_more:
                Intent gdriveSync = new Intent(this, SettingsActivity.class);
                startActivity(gdriveSync);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean requestRefresh() {
        AppLog.d("Refresh pressed");

        if (mAuthToken == null) {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
            mAccountChooserHelper.showAccountsDialog();
            return false;
        }

        if (ContentResolver.isSyncPending(mSyncAccount, AppListContentProvider.AUTHORITY) || ContentResolver.isSyncActive(mSyncAccount, AppListContentProvider.AUTHORITY)) {
            AppLog.d("Sync requested already. Skipping... ");
            return true;
        }
        //mRefreshAnim.start();
        Bundle params = new Bundle();
        params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(mSyncAccount, AppListContentProvider.AUTHORITY, params);
        return true;
    }

    /**
     * For devices prior to honeycomb add enable/disable text
     *
     * @param item
     * @param titleRes
     */
    private void menuTitleUpdateCompat(MenuItem item, int titleRes) {

        String title = getString(titleRes);
        String state = null;
        if (item.isChecked()) {
            state = getString(R.string.enabled);
        } else {
            state = getString(R.string.disabled);
        }
        item.setTitle(String.format("%s (%s)", title, state));
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        // TODO

        return false;
    }

    @Override
    public void onHelperAccountSelected(Account account, String authToken) {
        mAuthToken = authToken;
        if (authToken == null) {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
            return;
        }
        if (mSyncAccount == null) {
            mSyncAccount = account;
        } else {
            mSyncAccount = account;
        }
    }

    @Override
    public void onDialogAccountSelected(Account account) {
        mAccountChooserHelper.onDialogAccountSelected(account);
    }

    @Override
    public void onDialogAccountNotFound() {
        mAccountChooserHelper.onDialogAccountNotFound();
    }

    @Override
    public void onHelperAccountNotFound() {
        Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if (TextUtils.isEmpty(s)) {
            MenuItemCompat.collapseActionView(mSearchMenuItem);
        } else {
            Intent searchIntent = new Intent(mContext, MarketSearchActivity.class);
            searchIntent.putExtra(MarketSearchActivity.EXTRA_KEYWORD, s);
            searchIntent.putExtra(MarketSearchActivity.EXTRA_EXACT, true);
            startActivity(searchIntent);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        notifyQueryChange(s);
        return true;
    }
//
//    @Override
//    public boolean onNavigationItemSelected(int position, long itemId) {
//        AppLog.d("Navigation changed: "+position);
//        if (mQueryChangeListener != null) {
//            mQueryChangeListener.onNavigationChanged(position);
//        }
//        return false;
//    }

    public void notifyQueryChange(String s) {
        if (mQueryChangeListener != null) {
            mQueryChangeListener.onQueryTextChanged(s);
        }
    }

    public void setQueryChangeListener(QueryChangeListener listener) {
        mQueryChangeListener = listener;
    }

    public void setRefreshListener(RefreshListener listener) {
        mRefreshListener = listener;
    }
}
