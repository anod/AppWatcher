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
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anod.appwatcher.accounts.AccountChooserHelper;
import com.anod.appwatcher.fragments.AccountChooserFragment;
import com.anod.appwatcher.fragments.AppWatcherListFragment;
import com.anod.appwatcher.sync.SyncAdapter;
import com.anod.appwatcher.utils.AppLog;
import com.anod.appwatcher.utils.TranslucentActionBarActivity;

import org.acra.ACRA;

public class AppWatcherActivity extends TranslucentActionBarActivity implements
        TextView.OnEditorActionListener, SearchView.OnQueryTextListener,
        AccountChooserHelper.OnAccountSelectionListener, AccountChooserFragment.OnAccountSelectionListener,
        ActionBar.OnNavigationListener {

    public static final String EXTRA_FROM_NOTIFICATION = "extra_noti";
    private boolean mSyncFinishedReceiverRegistered;

    public static final int NAV_ALL = 0;
    public static final int NAV_INSTALLED = 1;
    public static final int NAV_NOTINSTALLED = 2;

    public interface QueryChangeListener {
        void onNavigationChanged(int navId);
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
	private MenuItem mRefreshMenuItem;
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

    /*
     * (non-Javadoc)
     *
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

        initSystemBar();

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getSupportActionBar().getThemedContext(),
                R.array.filter_list, android.R.layout.simple_spinner_dropdown_item);

        Intent i = getIntent();
        if (i != null) {
            mOpenChangelog =i.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false);
            i.removeExtra(EXTRA_FROM_NOTIFICATION);
        }

        int nav_id = NAV_ALL;
        if (savedInstanceState != null && !mOpenChangelog) {
            nav_id = savedInstanceState.getInt("nav_id",NAV_ALL);
            AppLog.d("Restore nav id: "+nav_id);
        }

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, this);
        getSupportActionBar().setSelectedNavigationItem(nav_id);

        if (savedInstanceState == null) {
            AppWatcherListFragment newFragment = AppWatcherListFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, newFragment);
            transaction.commit();
        }

		mContext = this;
		mPreferences = new Preferences(this);

		mAccountChooserHelper = new AccountChooserHelper(this, mPreferences, this);
		mAccountChooserHelper.init();


	}

    public boolean isOpenChangelog() {
        return mOpenChangelog;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu
		getMenuInflater().inflate(R.menu.main, menu);

		MenuItem autoSyncMenuItem = menu.findItem(R.id.menu_auto_update);
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
		if (!ContentResolver.isSyncActive(mSyncAccount, AppListContentProvider.AUTHORITY)) {
			stopRefreshAnim();
		} else {
			startRefreshAnim();
		}
	}


	/**
	 * Receive notifications from SyncAdapter
	 */
	private BroadcastReceiver mSyncFinishedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(SyncAdapter.SYNC_PROGRESS)) {
				startRefreshAnim();
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

		ACRA.getErrorReporter().setDefaultReportSenders();

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

	/**
	 * stop refresh button animation
	 */
	private void stopRefreshAnim() {
		if (mRefreshMenuItem == null) {
			return;
		}
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
		if (mRefreshMenuItem == null) {
			return;
		}
		View actionView = MenuItemCompat.getActionView(mRefreshMenuItem);
		//already animating
		if (actionView != null) {
			return;
		}
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
		default:
			return true;
		}
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
		startRefreshAnim();
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

    @Override
    public boolean onNavigationItemSelected(int position, long itemId) {
        AppLog.d("Navigation changed: "+position);
        if (mQueryChangeListener != null) {
            mQueryChangeListener.onNavigationChanged(position);
        }
        return false;
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
