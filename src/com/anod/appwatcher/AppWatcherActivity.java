package com.anod.appwatcher;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.anod.appwatcher.accounts.MarketTokenHelper;
import com.anod.appwatcher.accounts.MarketTokenHelper.CallBack;
import com.anod.appwatcher.market.MarketSessionHelper;
import com.anod.appwatcher.sync.Authenticator;
import com.anod.appwatcher.sync.SyncAdapter;

public class AppWatcherActivity extends SherlockFragmentActivity {
	private static final int MENU_REFRESH_IDX = 1;
	protected String mAuthToken;
	private AppWatcherActivity mContext;
	private Animation mAnimRotation;
	private ImageView mRefreshView;
	private MenuItem mRefreshMenuItem;

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

        setSync();
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        mRefreshMenuItem = menu.getItem(MENU_REFRESH_IDX);       
        return true;
    }
    
    private void setSync() {
    	ContentResolver.setIsSyncable(Authenticator.getAccount(), AppListContentProvider.AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(Authenticator.getAccount(), AppListContentProvider.AUTHORITY, true);
    }
    
    private BroadcastReceiver mSyncFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        	if (intent.getAction().equals(SyncAdapter.SYNC_PROGRESS)) {
        		//StartAnimation
        		mRefreshMenuItem.setActionView(mRefreshView);
        		mRefreshView.startAnimation(mAnimRotation);
        	} else if (intent.getAction().equals(SyncAdapter.SYNC_STOP)) {
        		//StopAnimation
        		mRefreshView.clearAnimation();
        		mRefreshMenuItem.setActionView(null);
        		//int updatesCount = intent.getIntExtra(SyncAdapter.EXTRA_UPDATES_COUNT, 0);
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
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    unregisterReceiver(mSyncFinishedReceiver);
	}

	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_add:
        	MarketTokenHelper helper = new MarketTokenHelper(this, true, new CallBack() {
				@Override
				public void onTokenReceive(String authToken) {
		        	if (authToken == null) {
		        		Toast.makeText(AppWatcherActivity.this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
		        	} else {
		        		Intent intent = new Intent(mContext, MarketSearchActivity.class);
		        		intent.putExtra(MarketSessionHelper.EXTRA_TOKEN, authToken);
		        		startActivity(intent);
		        	}
				}
			});
        	helper.requestToken();
        	return true;
        case R.id.menu_refresh:
        	Log.d("AppWatcher", "Refresh pressed");
            Bundle params = new Bundle();
            params.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, false);
            params.putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, false);
            params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            
        	ContentResolver.requestSync(Authenticator.getAccount(), AppListContentProvider.AUTHORITY, params);
        	return true;       
        case R.id.menu_device_id:
			Intent intent = new Intent(mContext, DeviceIdActivity.class);
			startActivity(intent);
        	return false;
        default:
            return onOptionsItemSelected(item);
        }
    }    

	
}
