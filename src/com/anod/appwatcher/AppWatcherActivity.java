package com.anod.appwatcher;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.anod.appwatcher.sync.Authenticator;
import com.anod.appwatcher.sync.SyncAdapter;
import com.anod.appwatcher.utils.AppLog;

public class AppWatcherActivity extends SherlockFragmentActivity {
	private static final int MENU_REFRESH_IDX = 1;
	private static final int MENU_AUTOSYNC_IDX = 2;
	private static final int MENU_WIFI_IDX = 3;
	private static final int THREE_HOURS_IN_SEC = 10800;
	private static final int EIGHT_HOURS_IN_SEC = 28800;
	protected String mAuthToken;
	private AppWatcherActivity mContext;
	private Animation mAnimRotation;
	private ImageView mRefreshView;
	private MenuItem mRefreshMenuItem;
	private Preferences mPreferences;
	private MenuItem mWifiMenuItem;
	private Account mSyncAccount;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mContext = this;
        
	    mAnimRotation = AnimationUtils.loadAnimation(this, R.anim.rotate);
	    mAnimRotation.setRepeatCount(Animation.INFINITE);

	    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    mRefreshView = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);
	    mPreferences = new Preferences(this);
	    	    
        AccountManager accountManager = AccountManager.get(this);
        mSyncAccount = Authenticator.getAccount(this);
        if (accountManager.getAccountsByType(mSyncAccount.type).length == 0) {
        	accountManager.addAccountExplicitly(mSyncAccount, null, null);
        }
	    
        setSync();
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        mRefreshMenuItem = menu.getItem(MENU_REFRESH_IDX);
        MenuItem autoSync = menu.getItem(MENU_AUTOSYNC_IDX);
        mWifiMenuItem = menu.getItem(MENU_WIFI_IDX);

        boolean useAutoSync = mPreferences.isAutoSync();
        autoSync.setChecked(useAutoSync);
        mWifiMenuItem.setChecked(mPreferences.isWifiOnly());
        if (useAutoSync == false) {
        	mWifiMenuItem.setEnabled(false);
        }
        return true;
    }
    
    private void setSync() {
    	Bundle params = new Bundle();

    	//initialize for 1st time
    	if (ContentResolver.getIsSyncable(mSyncAccount, AppListContentProvider.AUTHORITY) < 1) {
    		ContentResolver.setIsSyncable(mSyncAccount, AppListContentProvider.AUTHORITY, 1);
    	}
    	
    	if (mPreferences.isAutoSync()) { 
    		long pollFrequency = (mPreferences.isWifiOnly()) ?  THREE_HOURS_IN_SEC : EIGHT_HOURS_IN_SEC;
    		ContentResolver.setSyncAutomatically(mSyncAccount, AppListContentProvider.AUTHORITY, true);
    		ContentResolver.addPeriodicSync(mSyncAccount, AppListContentProvider.AUTHORITY, params, pollFrequency);
    	} else {
    		ContentResolver.removePeriodicSync(mSyncAccount, AppListContentProvider.AUTHORITY, params);
    		ContentResolver.setSyncAutomatically(mSyncAccount, AppListContentProvider.AUTHORITY, false);   		
    	}
    	
    }
    
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
	    }
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    unregisterReceiver(mSyncFinishedReceiver);
	}

	/**
	 * 
	 */
	private void stopRefreshAnim() {
		//StopAnimation
		mRefreshView.clearAnimation();
		mRefreshMenuItem.setActionView(null);
	}

	/**
	 * 
	 */
	private void startRefreshAnim() {
		//StartAnimation
		mRefreshMenuItem.setActionView(mRefreshView);
		mRefreshView.startAnimation(mAnimRotation);
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
            Bundle params = new Bundle();
            params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);            
        	ContentResolver.requestSync(mSyncAccount, AppListContentProvider.AUTHORITY, params);
        	return true;       
        case R.id.menu_auto_update:
        	boolean useAutoSync = !item.isChecked();
        	item.setChecked(useAutoSync);
       		mPreferences.saveAutoSync(useAutoSync);
            if (useAutoSync == false) {
            	mWifiMenuItem.setEnabled(false);
            } else {
            	mWifiMenuItem.setEnabled(true);
            }
       		setSync();
        	return true;
        case R.id.menu_wifi_only:
        	boolean useWifiOnly = !item.isChecked();
        	item.setChecked(useWifiOnly);
       		mPreferences.saveWifiOnly(useWifiOnly);
       		setSync();
        	return true;
        case R.id.menu_about:
        	AboutDialog aboutDialog = AboutDialog.newInstance();
        	aboutDialog.show(getSupportFragmentManager(), "aboutDialog");        	
        	return true;
        default:
            return true;
        }
    }    

	
}
