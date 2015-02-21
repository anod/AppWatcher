package com.anod.appwatcher.sync;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Notification;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.format.DateUtils;

import com.anod.appwatcher.BuildConfig;
import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.accounts.AccountHelper;
import com.anod.appwatcher.backup.GDriveSync;
import com.anod.appwatcher.market.AppIconLoader;
import com.anod.appwatcher.market.AppLoader;
import com.anod.appwatcher.market.DeviceIdHelper;
import com.anod.appwatcher.market.MarketSessionHelper;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.model.AppListTable;
import com.anod.appwatcher.utils.AppLog;
import com.anod.appwatcher.utils.BitmapUtils;
import com.anod.appwatcher.utils.GooglePlayServices;
import com.crashlytics.android.Crashlytics;
import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.model.Market.App;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final int ONE_SEC_IN_MILLIS = 1000;

	private final Context mContext;

    public static final String SYNC_STOP = "com.anod.appwatcher.sync.start";
    public static final String SYNC_PROGRESS = "com.anod.appwatcher.sync.progress";
    public static final String EXTRA_UPDATES_COUNT = "extra_updates_count";
    private MarketSession mMarketSession;
    private AppLoader mLoader;

    public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
        mContext = context;
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

		Preferences pref = new Preferences(mContext);

		boolean manualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
		// Skip any check if sync requested from application
		if (manualSync == false) {
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
		if (pref.getAccount() == null) {
			AppLog.d("No active account, skipping sync...");
			return;
		}
		AppLog.v("Perform synchronization");
		
		//Broadcast progress intent
		Intent startIntent = new Intent(SYNC_PROGRESS);
		mContext.sendBroadcast(startIntent);

        mMarketSession = createMarketSession(pref);
        mLoader = new AppLoader(mMarketSession);

		boolean lastUpdatesViewed = pref.isLastUpdatesViewed();
		AppLog.d("Last update viewed: "+lastUpdatesViewed);
		
		ArrayList<UpdatedApp> updatedApps = null;
		AppListContentProviderClient appListProvider = new AppListContentProviderClient(provider);
		try {
            updatedApps = doSync(pref, appListProvider, lastUpdatesViewed);
		} catch (RemoteException e) {
            AppLog.e("doSync exception", e);
            Crashlytics.logException(e);
		}
		int size = (updatedApps!=null) ? updatedApps.size() : 0;
		Intent finishIntent = new Intent(SYNC_STOP);
		finishIntent.putExtra(EXTRA_UPDATES_COUNT, size);
		mContext.sendBroadcast(finishIntent);

        long now = System.currentTimeMillis();
		pref.updateLastTime(now);
				
		if (size > 0) {
            SyncNotification sn = new SyncNotification(mContext);
            Notification notification = sn.create(updatedApps, mLoader);
            sn.show(notification);
			if (!manualSync && lastUpdatesViewed) {
				pref.markViewed(false);
			}
		}

        if (manualSync == false) {
            if (pref.isDriveSyncEnabled()) {
                AppLog.d("DriveSyncEnabled = true");
                performGDriveSync(pref, now);
            } else {
                AppLog.d("DriveSyncEnabled = false, skipping...");
            }
        }
        mLoader = null;
        mMarketSession = null;
		AppLog.d("Finish::onPerformSync()");
	
	}

    private void performGDriveSync(Preferences pref, long now) {
        long driveSyncTime = pref.getLastDriveSyncTime();
        if (driveSyncTime == -1 || now > (DateUtils.DAY_IN_MILLIS + driveSyncTime)) {
            AppLog.d("DriveSync perform sync");
            GDriveSync driveSync = new GDriveSync(mContext);
            try {
                driveSync.syncLocked();
                pref.saveDriveSyncTime(System.currentTimeMillis());
            } catch (GooglePlayServices.ResolutionException e) {
                if (e.getResolution() != null){
                    driveSync.showResolutionNotification(e.getResolution());
                }
                AppLog.ex(e);
                Crashlytics.logException(e);
            } catch (Exception e) {
                AppLog.ex(e);
                Crashlytics.logException(e);
            }
        } else {
            AppLog.d("DriveSync backup is fresh");
        }
    }

    /**
	 * Check if device has wi-fi connection
	 * @return
	 */
	private boolean isWifiEnabled() {
		ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork == null) {
			AppLog.e("No active network info");
			return false;
		}
		return (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
	}

    protected static class UpdatedApp {
        String appId;
        String title;
        String pkg;

        private UpdatedApp(String appId, String title, String pkg) {
            this.appId = appId;
            this.title = title;
            this.pkg = pkg;
        }
    }
	/**
	 * @param pref
	 * @param client
	 * @throws RemoteException
	 * @return list of titles that were updated
	 */
	private ArrayList<UpdatedApp> doSync(Preferences pref, AppListContentProviderClient client, boolean lastUpdatesViewed) throws RemoteException {
		ArrayList<UpdatedApp> updatedTitles = new ArrayList<UpdatedApp>();

		if (mMarketSession == null) {
			return updatedTitles;
		}
        AppIconLoader iconLoader = new AppIconLoader(mMarketSession);

		AppListCursor apps = client.queryAll();
		if (apps==null || apps.moveToFirst() == false) {
			return updatedTitles;
		}
		apps.moveToPosition(-1);


        Set<String> debugPkgs = null;
        if (BuildConfig.DEBUG) {
            debugPkgs = new HashSet<String>();
         //   debugPkgs.add("com.adobe.reader");
         //   debugPkgs.add("com.aide.ui");
         //   debugPkgs.add("com.anod.car.home.free");
         //   debugPkgs.add("com.anod.car.home.pro");
         //   debugPkgs.add("com.ibolt.carhome");
        }

		// TODO: implement multiple calls
		while(apps.moveToNext()) {
			App marketApp = null;
			AppInfo localApp = apps.getAppInfo();
			AppLog.d("Checking for updates '"+localApp.getTitle()+"' ...");
			try {
				marketApp = mLoader.loadOne(localApp.getAppId());
			} catch (Exception e) {
				AppLog.e("Cannot retrieve information for "+localApp.getTitle() + ", id:"+localApp.getAppId(), e);
				continue;
			}
			if (marketApp == null) {
				AppLog.e("Cannot retrieve information for "+localApp.getTitle() + ", id:"+localApp.getAppId());
				continue;
			}

			if (marketApp.getVersionCode() > localApp.getVersionCode() || (debugPkgs!=null && debugPkgs.contains(localApp.getPackageName()))) {
				AppLog.d("New version found ["+marketApp.getVersionCode()+"]");
				Bitmap icon = iconLoader.loadImageUncached(marketApp.getId());
				AppInfo newApp = createNewVersion(marketApp, localApp, icon);
				client.update(newApp);
				updatedTitles.add(new UpdatedApp(localApp.getAppId(),marketApp.getTitle(),marketApp.getPackageName()));
				continue;
			}
			
			AppLog.d("No update found.");
			ContentValues values = new ContentValues();
			//Mark updated app as normal 
			if (localApp.getStatus() == AppInfo.STATUS_UPDATED && lastUpdatesViewed) {
				localApp.setStatus(AppInfo.STATUS_NORMAL);
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
			if (!marketApp.getPriceCurrency().equals(localApp.getPriceCur())) {
				values.put(AppListTable.Columns.KEY_PRICE_CURRENCY, marketApp.getPriceCurrency());
			}
			if (!marketApp.getPrice().equals(localApp.getPriceText())) {
				values.put(AppListTable.Columns.KEY_PRICE_TEXT, marketApp.getPrice());
			}
			if (localApp.getPriceMicros() != marketApp.getPriceMicros()) {
				values.put(AppListTable.Columns.KEY_PRICE_MICROS, marketApp.getPriceMicros());
			}

			AppLog.d("ContentValues: "+values.toString());

			if (values.size() > 0) {
				client.update(localApp.getRowId(), values);
			}
		}
		apps.close();

		return updatedTitles;
	}

	private AppInfo createNewVersion(App marketApp, AppInfo localApp, Bitmap newIcon)  {

        // Gets the current system time in milliseconds
        Long now = Long.valueOf(System.currentTimeMillis());

		AppInfo newApp = new AppInfo(marketApp, newIcon);
		newApp.setRowId(localApp.getRowId());
		newApp.setStatus(AppInfo.STATUS_UPDATED);
		newApp.setUpdateTime(now);

		return newApp;
	}


	private MarketSession createMarketSession(Preferences prefs) {
		AccountHelper tokenHelper = new AccountHelper(mContext);
		String authToken = null;
		try {
			authToken = tokenHelper.requestTokenBlocking(null, prefs.getAccount());
		} catch (IOException e) {
			AppLog.e("AuthToken IOException: " + e.getMessage(), e);
		} catch (AuthenticatorException e) {
			AppLog.e("AuthToken AuthenticatorException: " + e.getMessage(), e);
		} catch (OperationCanceledException e) {
			AppLog.e("AuthToken OperationCanceledException: " + e.getMessage(), e);
		}
		if (authToken == null) {
			return null;
		}
		MarketSessionHelper helper = new MarketSessionHelper(mContext);
		String deviceId = DeviceIdHelper.getDeviceId(mContext, prefs);
		final MarketSession session = helper.create(deviceId, null);
   		session.setAuthSubToken(authToken);
    	return session;
	}
	


}
