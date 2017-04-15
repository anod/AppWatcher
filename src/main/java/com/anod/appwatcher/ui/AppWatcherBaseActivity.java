package com.anod.appwatcher.ui;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.appwatcher.AppWatcherActivity;
import com.anod.appwatcher.MarketSearchActivity;
import com.anod.appwatcher.R;
import com.anod.appwatcher.SettingsActivity;
import com.anod.appwatcher.installed.ImportInstalledActivity;
import com.anod.appwatcher.model.Filters;
import com.anod.appwatcher.sync.ManualSyncService;
import com.anod.appwatcher.sync.SyncAdapter;
import com.anod.appwatcher.tags.TagsEditorActivity;
import com.anod.appwatcher.utils.MenuItemAnimation;
import com.anod.appwatcher.utils.UpgradeCheck;
import com.google.android.gms.gcm.GcmTaskService;

import java.util.ArrayList;
import java.util.List;

import info.anodsplace.android.log.AppLog;

/**
 * @author algavris
 * @date 18/03/2017.
 */

public abstract class AppWatcherBaseActivity extends DrawerActivity implements
        TextView.OnEditorActionListener, SearchView.OnQueryTextListener {

    public static final String EXTRA_FROM_NOTIFICATION = "extra_noti";

    private boolean mSyncFinishedReceiverRegistered;

    private MenuItemAnimation mRefreshAnim;
    private ViewPager mViewPager;
    private String mFilterQuery = "";

    public interface EventListener {
        void onSortChanged(int sortIndex);
        void onQueryTextChanged(String newQuery);
        void onSyncStart();
        void onSyncFinish();
    }

    private MenuItem mSearchMenuItem;
    private ArrayList<AppWatcherActivity.EventListener> mEventListener = new ArrayList<>(3);

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("tab_id", mViewPager.getCurrentItem());
        outState.putString("filter", mFilterQuery);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentLayout());
        setupDrawer();

        int filterId = Filters.TAB_ALL;
        if (savedInstanceState != null) {
            filterId = savedInstanceState.getInt("tab_id", Filters.TAB_ALL);
            mFilterQuery = savedInstanceState.getString("filter");
            AppLog.d("Restore tab: " + filterId);
        }

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(createViewPagerAdapter());
        mViewPager.setCurrentItem(filterId);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mRefreshAnim = new MenuItemAnimation(this, R.anim.rotate);
    }

    protected abstract @LayoutRes int getContentLayout();
    protected abstract @MenuRes int getMenuResource();
    protected abstract AppWatcherBaseActivity.Adapter createViewPagerAdapter();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(getMenuResource(), menu);

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
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint(getString(R.string.search));

        if (!TextUtils.isEmpty(mFilterQuery)) {
            MenuItemCompat.expandActionView(mSearchMenuItem);
            searchView.setQuery(mFilterQuery, false);
        }

        mRefreshAnim.setMenuItem(menu.findItem(R.id.menu_act_refresh));
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Receive notifications from SyncAdapter
     */
    private BroadcastReceiver mSyncFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (GcmTaskService.SERVICE_ACTION_EXECUTE_TASK.equals(action) || SyncAdapter.SYNC_PROGRESS.equals(action)) {
                mRefreshAnim.start();
                notifySyncStart();
            } else if (SyncAdapter.SYNC_STOP.equals(action)) {
                int updatesCount = intent.getIntExtra(SyncAdapter.EXTRA_UPDATES_COUNT, 0);
                mRefreshAnim.stop();
                notifySyncStop();
                if (updatesCount == 0) {
                    Toast.makeText(AppWatcherBaseActivity.this, R.string.no_updates_found, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SyncAdapter.SYNC_PROGRESS);
        filter.addAction(SyncAdapter.SYNC_STOP);
        filter.addAction(GcmTaskService.SERVICE_ACTION_EXECUTE_TASK);
        registerReceiver(mSyncFinishedReceiver, filter);
        mSyncFinishedReceiverRegistered = true;
        super.onResume();

        notifySyncStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSyncFinishedReceiverRegistered) {
            unregisterReceiver(mSyncFinishedReceiver);
            mSyncFinishedReceiverRegistered = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                Intent addActivity = new Intent(this, MarketSearchActivity.class);
                startActivity(addActivity);
                return true;
            case R.id.menu_act_refresh:
                requestRefresh();
                return true;
            case R.id.menu_settings:
                Intent settingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(settingsActivity);
                return true;
            case R.id.menu_act_import:
                startActivity(new Intent(this, ImportInstalledActivity.class));
                return true;
            case R.id.menu_act_tags:
                startActivity(new Intent(this, TagsEditorActivity.class));
                return true;
            case R.id.menu_act_sort:
                showSortOptions();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortOptions() {
        final int selected = mPreferences.getSortIndex();
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(R.array.sort_titles, selected, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        mPreferences.setSortIndex(index);
                        notifySortChange(index);
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    public boolean requestRefresh() {
        AppLog.d("Refresh pressed");
        if (!isAuthenticated()) {
            showAccountsDialogWithCheck();
            return false;
        }

        Toast.makeText(this, "Refresh scheduled", Toast.LENGTH_SHORT).show();
        ManualSyncService.startActionSync(this);
        return false;
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        return false;
    }

    @Override
    public void onAccountSelected(Account account, String authToken) {
        super.onAccountSelected(account, authToken);
        if (UpgradeCheck.isNewVersion(mPreferences))
        {
            requestRefresh();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mFilterQuery = "";
        if (TextUtils.isEmpty(query)) {
            MenuItemCompat.collapseActionView(mSearchMenuItem);
        } else {
            Intent searchIntent = new Intent(this, MarketSearchActivity.class);
            searchIntent.putExtra(MarketSearchActivity.EXTRA_KEYWORD, query);
            searchIntent.putExtra(MarketSearchActivity.EXTRA_EXACT, true);
            startActivity(searchIntent);
            notifyQueryChange("");
            if (mSearchMenuItem != null) {
                MenuItemCompat.collapseActionView(mSearchMenuItem);
            }
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mFilterQuery = newText;
        notifyQueryChange(newText);
        return true;
    }

    private void notifySortChange(int sortIndex) {
        for (int idx = 0; idx < mEventListener.size(); idx++) {
            mEventListener.get(idx).onSortChanged(sortIndex);
        }
    }

    private void notifyQueryChange(String newTexr) {
        for (int idx = 0; idx < mEventListener.size(); idx++) {
            mEventListener.get(idx).onQueryTextChanged(newTexr);
        }
    }

    public int addQueryChangeListener(AppWatcherActivity.EventListener listener) {
        mEventListener.add(listener);

        if (!TextUtils.isEmpty(mFilterQuery))
        {
            notifyQueryChange(mFilterQuery);
        }

        return mEventListener.size() - 1;
    }

    public void removeQueryChangeListener(int index) {
        if (index < mEventListener.size()) {
            mEventListener.remove(index);
        }
    }

    private void notifySyncStart() {
        for (int idx = 0; idx < mEventListener.size(); idx++) {
            mEventListener.get(idx).onSyncStart();
        }
    }

    private void notifySyncStop() {
        for (int idx = 0; idx < mEventListener.size(); idx++) {
            mEventListener.get(idx).onSyncFinish();
        }
    }

    public  static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}