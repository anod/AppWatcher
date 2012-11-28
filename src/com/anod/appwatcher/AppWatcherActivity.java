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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.anod.appwatcher.actionbarcompat.ActionBarActivity;
import com.anod.appwatcher.fragments.AboutDialogFragment;
import com.anod.appwatcher.fragments.AppWatcherListFragment;
import com.anod.appwatcher.sync.AccountHelper;
import com.anod.appwatcher.sync.SyncAdapter;
import com.anod.appwatcher.utils.AppLog;
import com.anod.appwatcher.utils.IntentUtils;

public class AppWatcherActivity extends ActionBarActivity {

	private static final int MENU_AUTOSYNC_IDX = 2;
	private static final int MENU_WIFI_IDX = 3;
	private static final int TWO_HOURS_IN_SEC = 7200;
	private static final int SIX_HOURS_IN_SEC = 21600;
	protected String mAuthToken;
	private AppWatcherActivity mContext;
	private Preferences mPreferences;
	private MenuItem mWifiMenuItem;
	private Account mSyncAccount;
	private MenuItem mAutoSyncMenuItem;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		AppWatcherListFragment newFragment = new AppWatcherListFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.fragment_container, newFragment);
		transaction.commit();

		mContext = this;
		mPreferences = new Preferences(this);

		mSyncAccount = AccountHelper.getAccount(this);
		if (mSyncAccount == null) {
			Toast.makeText(this, R.string.google_account_not_found, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		boolean autoSync = true;
		if (!mPreferences.checkFirstLaunch()) {
			autoSync = ContentResolver.getSyncAutomatically(mSyncAccount, AppListContentProvider.AUTHORITY);
		}
		setSync(autoSync);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);
		mAutoSyncMenuItem = menu.getItem(MENU_AUTOSYNC_IDX);
		mWifiMenuItem = menu.getItem(MENU_WIFI_IDX);
		refreshMenuState();
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
		if (!ContentResolver.isSyncActive(mSyncAccount, AppListContentProvider.AUTHORITY)) {
			stopRefreshAnim();
		} else {
			startRefreshAnim();
		}
		AppLog.d("Mark updates as viewed.");
		mPreferences.markViewed(true);
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
		getActionBarHelper().setRefreshActionItemState(false);
	}

	/**
	 * Animate refresh button
	 */
	private void startRefreshAnim() {
		getActionBarHelper().setRefreshActionItemState(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			Intent intent = new Intent(mContext, MarketSearchActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_refresh:
			AppLog.d("Refresh pressed");
			if (ContentResolver.isSyncPending(mSyncAccount, AppListContentProvider.AUTHORITY)) {
				AppLog.d("Sync requested already. Skipping... ");
				return true;
			}
			startRefreshAnim();
			Bundle params = new Bundle();
			params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			ContentResolver.requestSync(mSyncAccount, AppListContentProvider.AUTHORITY, params);
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
		default:
			return true;
		}
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

}
