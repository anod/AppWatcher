package com.anod.appwatcher.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.accounts.MarketTokenHelper;
import com.anod.appwatcher.accounts.MarketTokenHelper.CallBack;
import com.anod.appwatcher.market.AppIconLoader;
import com.anod.appwatcher.market.AppLoader;
import com.anod.appwatcher.market.MarketSessionHelper;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.model.AppListTable;
import com.anod.appwatcher.utils.BitmapUtils;
import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.model.Market.App;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private final Context mContext;
    
    public static final String SYNC_STOP = "com.anod.appwatcher.sync.start";
    public static final String SYNC_PROGRESS = "com.anod.appwatcher.sync.progress";
    public static final String EXTRA_UPDATES_COUNT = "extra_updates_count";
    
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
        mContext = context;
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {

		Intent startIntent = new Intent(SYNC_PROGRESS);
		mContext.sendBroadcast(startIntent);
		
		Intent finishIntent = new Intent(SYNC_STOP);
		int updatesCount = 0;
		try {
			Preferences pref = new Preferences(mContext);
			MarketSession session = createAppInfoLoader(pref);
			AppIconLoader iconLoader = new AppIconLoader(session);
			AppLoader loader = new AppLoader(session, false);
			
			AppListCursor apps = loadApps(provider); 
			if (apps!=null && apps.moveToFirst()) {
				apps.moveToPosition(-1);
				while(apps.moveToNext()) {
					AppInfo localApp = apps.getAppInfo();
					Log.v("AppWatcher", "Checking for updates '"+localApp.getTitle()+"' ...");
					App marketApp = loader.load(localApp.getAppId());
					if (marketApp == null) {
						Log.e("AppWatcher", "Cannot retrieve information about application");
						continue;
					}
					if (marketApp.getVersionCode() > localApp.getVersionCode()) {
						Log.v("AppWatcher", "New version found ["+marketApp.getVersionCode()+"]");
						Bitmap icon = iconLoader.loadImageUncached(marketApp.getId());
						ContentValues values = createContentValues(marketApp, icon);
						Uri updateUri = AppListContentProvider.CONTENT_URI.buildUpon().appendPath(String.valueOf(localApp.getRowId())).build();
			            provider.update(updateUri, values, null, null);
			            updatesCount++;
					} else {
						Log.v("AppWatcher", "No update found.");
					}
				}
			}
		} catch (RemoteException e) {
			
			
		}
		finishIntent.putExtra(EXTRA_UPDATES_COUNT, updatesCount);
		mContext.sendBroadcast(finishIntent);
		
		Log.v("AppWatcher", "Finish::onPerformSync()");
	
	}

    private ContentValues createContentValues(App app, Bitmap icon) {
    	ContentValues values = new ContentValues();

   	    values.put(AppListTable.Columns.KEY_TITLE, app.getTitle());
   	    values.put(AppListTable.Columns.KEY_VERSION_NUMBER, app.getVersionCode());  	    
   	    values.put(AppListTable.Columns.KEY_VERSION_NAME, app.getVersion());
   	    values.put(AppListTable.Columns.KEY_CREATOR, app.getCreator());
   	    values.put(AppListTable.Columns.KEY_STATUS, AppInfo.STATUS_UPDATED );
   	    if (icon != null) {
   	    	byte[] iconData = BitmapUtils.flattenBitmap(icon);
   	   	    values.put(AppListTable.Columns.KEY_ICON_CACHE, iconData);
   	    }
   	    
   	    return values;
    }
    
	private MarketSession createAppInfoLoader(Preferences prefs) {
		MarketSessionHelper helper = new MarketSessionHelper(mContext);
		final MarketSession session = helper.create(prefs.getDeviceId(), null);

    	MarketTokenHelper tokenHelper = new MarketTokenHelper(mContext, false, new CallBack() {
			@Override
			public void onTokenReceive(String authToken) {
	        	if (authToken != null) {
	        		session.setAuthSubToken(authToken);
	        	}
			}
		});
    	tokenHelper.requestToken();
    	return session;
	}
	
	/**
	 * @param provider
	 * @throws RemoteException
	 */
	private AppListCursor loadApps(ContentProviderClient provider)
			throws RemoteException {
		Cursor cursor = provider.query(
			AppListContentProvider.CONTENT_URI,
			AppListTable.APPLIST_PROJECTION, 
			null, null, null
		);
		if (cursor!=null) {
			return new AppListCursor(cursor);
		}
		return null;
	}

}
