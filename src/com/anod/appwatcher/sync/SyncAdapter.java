package com.anod.appwatcher.sync;

import java.util.ArrayList;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.AppWatcherActivity;
import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.R;
import com.anod.appwatcher.accounts.MarketTokenHelper;
import com.anod.appwatcher.market.AppIconLoader;
import com.anod.appwatcher.market.AppLoader;
import com.anod.appwatcher.market.DeviceIdHelper;
import com.anod.appwatcher.market.MarketSessionHelper;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.model.AppListTable;
import com.anod.appwatcher.utils.AppLog;
import com.anod.appwatcher.utils.BitmapUtils;
import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.model.Market.App;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final int ONE_SEC_IN_MILLIS = 1000;

	private final Context mContext;
    
	private static final int NOTIFICATION_ID = 1;
	
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

		Preferences pref = new Preferences(mContext);

		// Skip any check if sync requested from application
		if (extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false) == false) {
			if (pref.isWifiOnly() && !isWifiEnabled()) {
				AppLog.d("Wifi not enabled, skipping update check....");
				return;
			}
			long updateTime = pref.getLastUpdateTime();
			if (updateTime != -1 && (System.currentTimeMillis() - updateTime < ONE_SEC_IN_MILLIS)) {
				AppLog.d("Last update less than second, skipping...");
				return;
			}			
		}
		AppLog.v("Perform synchronization");
		
		//Broadcast progress intent
		Intent startIntent = new Intent(SYNC_PROGRESS);
		mContext.sendBroadcast(startIntent);
		
		ArrayList<String> updatedTitles = null;
		try {
			updatedTitles = doSync(pref, provider);
		} catch (RemoteException e) {
			
		}
		int size = (updatedTitles!=null) ? updatedTitles.size() : 0;
		Intent finishIntent = new Intent(SYNC_STOP);
		finishIntent.putExtra(EXTRA_UPDATES_COUNT, size);
		mContext.sendBroadcast(finishIntent);

		pref.updateLastTime(System.currentTimeMillis());
		
		if (size > 0) {
			showNotification(updatedTitles);
		}

		AppLog.d("Finish::onPerformSync()");
	
	}
	
	/**
	 * Check if device has wi-fi connection
	 * @return
	 */
	private boolean isWifiEnabled() {
		ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
	}

	/**
	 * @param provider
	 * @param updatedTitles
	 * @throws RemoteException
	 * @return list of titles that were updated
	 */
	private ArrayList<String> doSync(Preferences pref, ContentProviderClient provider) throws RemoteException {
		ArrayList<String> updatedTitles = new ArrayList<String>();

		
		MarketSession session = createAppInfoLoader(pref);
		AppIconLoader iconLoader = new AppIconLoader(session);
		AppLoader loader = new AppLoader(session, false);
		
		AppListCursor apps = loadApps(provider); 
		if (apps==null || apps.moveToFirst() == false) {
			return updatedTitles;
		}
		apps.moveToPosition(-1);
		
		boolean lastUpdatesViewed = pref.isLastUpdatesViewed();
		
		while(apps.moveToNext()) {
			AppInfo localApp = apps.getAppInfo();
			AppLog.d("Checking for updates '"+localApp.getTitle()+"' ...");
			App marketApp = loader.load(localApp.getAppId());
			if (marketApp == null) {
				AppLog.e("Cannot retrieve information about application");
				continue;
			}
			ContentValues values;
			if (marketApp.getVersionCode() > localApp.getVersionCode()) {
				AppLog.d("New version found ["+marketApp.getVersionCode()+"]");
				Bitmap icon = iconLoader.loadImageUncached(marketApp.getId());
				values = createContentValues(marketApp, icon);
				updateApp(provider, localApp.getRowId(), values);
	            updatedTitles.add(marketApp.getTitle());
	            continue;
			}
			
			AppLog.d("No update found.");
			values = new ContentValues();
			//Mark updated app as normal 
			if (localApp.getStatus() == AppInfo.STATUS_UPDATED && lastUpdatesViewed) {
				AppLog.d("Mark application as old");
				values.put(AppListTable.Columns.KEY_STATUS, AppInfo.STATUS_NORMAL );
			}
			//Refresh app icon if it wasn't fetched previously
			if (localApp.getIcon() == null) {
				AppLog.d("Fetch missing icon");
				Bitmap icon = iconLoader.loadImageUncached(localApp.getAppId());
		   	    if (icon != null) {
		   	    	byte[] iconData = BitmapUtils.flattenBitmap(icon);
		   	   	    values.put(AppListTable.Columns.KEY_ICON_CACHE, iconData);
		   	    }			
			}
			if (values.size() > 0) {
				updateApp(provider, localApp.getRowId(), values);
			}
		}
		if (lastUpdatesViewed && updatedTitles.size() > 0) {
			pref.markViewed(false);
		}
		return updatedTitles;
	}

	/**
	 * @param provider
	 * @param localApp
	 * @param values
	 * @throws RemoteException
	 */
	private void updateApp(ContentProviderClient provider, int rowId, ContentValues values) throws RemoteException {
		Uri updateUri = AppListContentProvider.CONTENT_URI.buildUpon().appendPath(String.valueOf(rowId)).build();
		provider.update(updateUri, values, null, null);
	}

	/**
	 * Create content values for new app
	 * @param app
	 * @param icon
	 * @return
	 */
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
		String deviceId = DeviceIdHelper.getDeviceId(mContext, prefs);
		final MarketSession session = helper.create(deviceId, null);

    	MarketTokenHelper tokenHelper = new MarketTokenHelper(mContext);
    	String authToken = tokenHelper.requestToken();
    	if (authToken != null) {
    		session.setAuthSubToken(authToken);
    	}
    	return session;
	}
	
	private void showNotification(ArrayList<String> updatedTitles) {
		Intent notificationIntent = new Intent(mContext, AppWatcherActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
		
		String title = renderNotificationTitle(updatedTitles);
		String text = renderNotificationText(updatedTitles);
		
		Builder builder = new NotificationCompat.Builder(mContext);	
		Notification notification = builder
			.setAutoCancel(true)
			.setSmallIcon(R.drawable.ic_stat_update)
			.setContentTitle(title)
			.setContentText(text)
			.setContentIntent(contentIntent)
			.setTicker(title)
			.getNotification();
		

		NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	/**
	 * @param updatedTitles
	 * @return
	 */
	private String renderNotificationText(ArrayList<String> updatedTitles) {
		int count = updatedTitles.size();
		if (count == 1) {
			return mContext.getString(R.string.notification_click);
		}
		if (count > 2) {
			return mContext.getString(
				R.string.notification_2_apps_more,
				updatedTitles.get(0),
				updatedTitles.get(1)						
			);
		} 
		return mContext.getString(R.string.notification_2_apps,
			updatedTitles.get(0),
			updatedTitles.get(1)						
		);				
	}

	/**
	 * @param updatedTitles
	 * @param count
	 * @return
	 */
	private String renderNotificationTitle(ArrayList<String> updatedTitles) {
		String title;
		int count = updatedTitles.size();
		if (count == 1) {
			title = mContext.getString(R.string.notification_one_updated, updatedTitles.get(0));
		} else {
			title = mContext.getString(R.string.notification_many_updates, count);
		}
		return title;
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
