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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.appwatcher.accounts.AccountHelper;
import com.anod.appwatcher.fragments.AboutDialogFragment;
import com.anod.appwatcher.fragments.AccountChooserFragment;
import com.anod.appwatcher.fragments.AppWatcherListFragment;
import com.anod.appwatcher.sync.SyncAdapter;
import com.anod.appwatcher.utils.AppLog;
import com.anod.appwatcher.utils.IntentUtils;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;

public class AppWatcherActivity extends ActionBarActivity implements TextView.OnEditorActionListener, AccountChooserFragment.OnAccountSelectionListener, SearchView.OnQueryTextListener {

	private AccountHelper mAccountHelper;

	public interface QueryChangeListener {
		void onQueryTextChanged(String newQuery);
	}

	public interface RefreshListener {
		void onRefreshFinish();
	}

	private static final int TWO_HOURS_IN_SEC = 7200;
	private static final int SIX_HOURS_IN_SEC = 21600;
	protected String mAuthToken;
	private AppWatcherActivity mContext;
	private Preferences mPreferences;
	private MenuItem mWifiMenuItem;
	private Account mSyncAccount;
	private MenuItem mAutoSyncMenuItem;
	private MenuItem mRefreshMenuItem;
	private MenuItem mSearchMenuItem;

	private QueryChangeListener mQueryChangeListener;
	private RefreshListener mRefreshListener;

	private PullToRefreshAttacher mPullToRefreshAttacher;

	/*
		 * (non-Javadoc)
		 *
		 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
		 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		/**
		 * Here we create a PullToRefreshAttacher manually without an Options instance.
		 * PullToRefreshAttacher will manually create one using default values.
		 */
		mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

		AppWatcherListFragment newFragment = new AppWatcherListFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.fragment_container, newFragment);
		transaction.commit();

		mContext = this;
		mPreferences = new Preferences(this);

		mSyncAccount = mPreferences.getAccount();
		if (mSyncAccount == null) {
			AccountChooserFragment accountsDialog = AccountChooserFragment.newInstance();
			accountsDialog.show(getSupportFragmentManager(), "accountsDialog");
		} else {
			mAccountHelper = new AccountHelper(mContext);
			mAccountHelper.requestToken(this);

			initAutoSync();
		}
	}

	public PullToRefreshAttacher getPullToRefreshAttacher() {
		return mPullToRefreshAttacher;
	}

	private void initAutoSync() {
		boolean autoSync = true;
		if (!mPreferences.checkFirstLaunch()) {
			autoSync = ContentResolver.getSyncAutomatically(mSyncAccount, AppListContentProvider.AUTHORITY);
		}

		setSync(autoSync);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu
		getMenuInflater().inflate(R.menu.main, menu);

		mAutoSyncMenuItem = menu.findItem(R.id.menu_auto_update);
		mWifiMenuItem = menu.findItem(R.id.menu_wifi_only);
		mRefreshMenuItem = menu.findItem(R.id.menu_refresh);

		mSearchMenuItem = menu.findItem(R.id.menu_filter);
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

		refreshMenuState();

		if (!ContentResolver.isSyncActive(mSyncAccount, AppListContentProvider.AUTHORITY)) {
			stopRefreshAnim();
		} else {
			startRefreshAnim();
		}

		return super.onCreateOptionsMenu(menu);
	}
	/**
	 * Update menu states when activity restored
	 */
	private void refreshMenuState() {
		boolean useAutoSync = ContentResolver.getSyncAutomatically(mSyncAccount, AppListContentProvider.AUTHORITY);
		mAutoSyncMenuItem.setChecked(useAutoSync);
		mWifiMenuItem.setChecked(mPreferences.isWifiOnly());
		if (useAutoSync == false) {
			mWifiMenuItem.setEnabled(false);
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			menuTitleUpdateCompat(mAutoSyncMenuItem, R.string.menu_auto_update);
			menuTitleUpdateCompat(mWifiMenuItem, R.string.menu_wifi_only);
		}
	}

	/**
	 * setup sync according to current settings
	 */
	private void setSync(boolean autoSync) {
		Bundle params = new Bundle();

		// initialize for 1st time
		if (ContentResolver.getIsSyncable(mSyncAccount, AppListContentProvider.AUTHORITY) < 1) {
			ContentResolver.setIsSyncable(mSyncAccount, AppListContentProvider.AUTHORITY, 1);
		}

		if (autoSync) {
			long pollFrequency = (mPreferences.isWifiOnly()) ? TWO_HOURS_IN_SEC : SIX_HOURS_IN_SEC;
			ContentResolver.setSyncAutomatically(mSyncAccount, AppListContentProvider.AUTHORITY, true);
			ContentResolver.addPeriodicSync(mSyncAccount, AppListContentProvider.AUTHORITY, params, pollFrequency);
		} else {
			ContentResolver.removePeriodicSync(mSyncAccount, AppListContentProvider.AUTHORITY, params);
			ContentResolver.setSyncAutomatically(mSyncAccount, AppListContentProvider.AUTHORITY, false);
		}

	}

	/**
	 * Receive notifications from SyncAdapter
	 */
	private BroadcastReceiver mSyncFinishedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(SyncAdapter.SYNC_PROGRESS)) {
				// startRefreshAnim();
			} else if (intent.getAction().equals(SyncAdapter.SYNC_STOP)) {
				int updatesCount = intent.getIntExtra(SyncAdapter.EXTRA_UPDATES_COUNT, 0);
				stopRefreshAnim();
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
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(SyncAdapter.SYNC_PROGRESS);
		filter.addAction(SyncAdapter.SYNC_STOP);
		registerReceiver(mSyncFinishedReceiver, filter);

		AppLog.d("Mark updates as viewed.");
		mPreferences.markViewed(true);

		notifyQueryChange("");
		if (mSearchMenuItem != null) {
			MenuItemCompat.collapseActionView(mSearchMenuItem);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mSyncFinishedReceiver);
	}

	/**
	 * stop refresh button animation
	 */
	private void stopRefreshAnim() {
		View actionView = MenuItemCompat.getActionView(mRefreshMenuItem);
		if (actionView != null) {
			actionView.clearAnimation();
			MenuItemCompat.setActionView(mRefreshMenuItem,null);
		}
	}

	/**
	 * Animate refresh button
	 */
	private void startRefreshAnim() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

		Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate);
		rotation.setRepeatCount(Animation.INFINITE);
		iv.startAnimation(rotation);

		MenuItemCompat.setActionView(mRefreshMenuItem, iv);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
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
			setSync(useAutoSync);
			return true;
		case R.id.menu_wifi_only:
			boolean useWifiOnly = !item.isChecked();
			item.setChecked(useWifiOnly);
			mPreferences.saveWifiOnly(useWifiOnly);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				menuTitleUpdateCompat(item, R.string.menu_wifi_only);
			}
			setSync(true);
			return true;
		case R.id.menu_rateapp:
			String pkg = getPackageName();
			Intent rateIntent = IntentUtils.createPlayStoreIntent(pkg);
			startActivity(rateIntent);
			return true;
		case R.id.menu_about:
			AboutDialogFragment aboutDialog = AboutDialogFragment.newInstance();
			aboutDialog.show(getSupportFragmentManager(), "aboutDialog");
			return true;
		case R.id.menu_import_export:
			Intent listExport = new Intent(this, ListExportActivity.class);
			startActivity(listExport);
			return true;
		case R.id.menu_accounts:
			AccountChooserFragment accountsDialog = AccountChooserFragment.newInstance();
			accountsDialog.show(getSupportFragmentManager(), "accountsDialog");
			return true;
		default:
			return true;
		}
	}

	public void requestRefresh() {
		AppLog.d("Refresh pressed");
		if (ContentResolver.isSyncPending(mSyncAccount, AppListContentProvider.AUTHORITY)) {
			AppLog.d("Sync requested already. Skipping... ");
			return;
		}
		startRefreshAnim();
		Bundle params = new Bundle();
		params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(mSyncAccount, AppListContentProvider.AUTHORITY, params);
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
	public void onAccountSelected(Account account) {
		if (mSyncAccount == null) {
			mSyncAccount = account;
			mAccountHelper.requestToken(this);
			initAutoSync();
		} else {
			mAccountHelper.requestToken(this);
			mSyncAccount = account;
		}
	}

	@Override
	public void onAccountNotFound() {
		finish();
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
