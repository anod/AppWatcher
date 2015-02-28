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
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.anod.appwatcher.AppWatcherApplication;
import com.anod.appwatcher.BuildConfig;
import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.R;
import com.anod.appwatcher.accounts.AccountHelper;
import com.anod.appwatcher.backup.GDriveSync;
import com.anod.appwatcher.market.BulkDetailsEndpoint;
import com.anod.appwatcher.market.DeviceIdHelper;
import com.anod.appwatcher.market.PlayStoreEndpoint;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.model.AppListTable;
import com.anod.appwatcher.utils.AppLog;
import com.anod.appwatcher.utils.BitmapUtils;
import com.anod.appwatcher.utils.DocUtils;
import com.anod.appwatcher.utils.GooglePlayServices;
import com.anod.appwatcher.volley.SyncImageLoader;
import com.crashlytics.android.Crashlytics;
import com.google.android.finsky.api.model.Document;
import com.google.android.finsky.protos.Common;
import com.google.android.finsky.protos.DocDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyncAdapter extends AbstractThreadedSyncAdapter implements PlayStoreEndpoint.Listener {
    private static final int ONE_SEC_IN_MILLIS = 1000;
    public static final int BULK_SIZE = 20;

    private final Context mContext;

    public static final String SYNC_STOP = "com.anod.appwatcher.sync.start";
    public static final String SYNC_PROGRESS = "com.anod.appwatcher.sync.progress";
    public static final String EXTRA_UPDATES_COUNT = "extra_updates_count";
    private BulkDetailsEndpoint mEndpoint;
    private SyncImageLoader mImageLoader;
    private int mIconSize = -1;

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


        mEndpoint = createEndpoint(pref);

		boolean lastUpdatesViewed = pref.isLastUpdatesViewed();
		AppLog.d("Last update viewed: "+lastUpdatesViewed);
		
		ArrayList<UpdatedApp> updatedApps = null;
		AppListContentProviderClient appListProvider = new AppListContentProviderClient(provider);
		try {
            updatedApps = doSync(appListProvider, lastUpdatesViewed);
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
            Notification notification = sn.create(updatedApps);
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

        mEndpoint = null;
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
            } catch (Exception e) {
                AppLog.ex(e);
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

    @Override
    public void onDataChanged() {

    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    protected static class UpdatedApp {
        String appId;
        String title;
        String pkg;
        String recentChanges;

        private UpdatedApp(String appId, String title, String pkg, String recentChanges) {
            this.appId = appId;
            this.title = title;
            this.pkg = pkg;
            this.recentChanges = recentChanges;
        }
    }
	/**
	 * @param client
	 * @throws RemoteException
	 * @return list of titles that were updated
	 */
	private ArrayList<UpdatedApp> doSync(AppListContentProviderClient client, boolean lastUpdatesViewed) throws RemoteException {
		ArrayList<UpdatedApp> updatedTitles = new ArrayList<UpdatedApp>();

		if (mEndpoint == null) {
			return updatedTitles;
		}

		AppListCursor apps = client.queryAll();
		if (apps==null || apps.moveToFirst() == false) {
			return updatedTitles;
		}
		apps.moveToPosition(-1);

        int bulkSize = apps.getCount() > BULK_SIZE ? BULK_SIZE : apps.getCount();

        HashMap<String, AppInfo> localApps = new HashMap<>(bulkSize);

        int i = 1;
		while(apps.moveToNext()) {

            AppInfo localApp = apps.getAppInfo();
            String docId = localApp.getAppId();
            localApps.put(docId, localApp);

            if (localApps.size() == bulkSize) {
                Set<String> docIds = localApps.keySet();
                AppLog.d("Sending bulk #"+i+"... "+docIds);
                List<Document> documents = requestBulkDetails(docIds);
                if (documents != null) {
                    updateApps(documents, localApps, client, updatedTitles, lastUpdatesViewed);
                } else {
                    AppLog.e("No documents were received.");
                }
                localApps.clear();
                i++;
            }

		}
        if (localApps.size() > 0) {
            Set<String> docIds = localApps.keySet();
            AppLog.d("Sending bulk #"+i+"... "+docIds);
            List<Document> documents = requestBulkDetails(docIds);
            if (documents != null) {
                updateApps(documents, localApps, client, updatedTitles, lastUpdatesViewed);
            } else {
                AppLog.e("No documents were received.");
            }
            localApps.clear();
        }
		apps.close();

		return updatedTitles;
	}

    private List<Document> requestBulkDetails(Set<String> docIds) {
        List<String> listDocIds = new ArrayList<String>(docIds);
        mEndpoint.setDocIds(listDocIds);

        mEndpoint.startSync();
        return mEndpoint.getDocuments();
    }

    private void updateApps(List<Document> documents, HashMap<String, AppInfo> localApps, AppListContentProviderClient client, ArrayList<UpdatedApp> updatedTitles, boolean lastUpdatesViewed) {
        for(Document marketApp: documents) {
            String docId = marketApp.getDocId();
            AppInfo localApp = localApps.get(docId);
            updateApp(marketApp, localApp, client, updatedTitles, lastUpdatesViewed);
        }
    }

    private void updateApp(Document marketApp, AppInfo localApp, AppListContentProviderClient client, ArrayList<UpdatedApp> updatedTitles, boolean lastUpdatesViewed) {
        Set<String> debugPkgs = null;
        if (BuildConfig.DEBUG) {
            debugPkgs = new HashSet<String>();
            //   debugPkgs.add("com.adobe.reader");
            //   debugPkgs.add("com.aide.ui");
               debugPkgs.add("com.anod.car.home.free");
            //   debugPkgs.add("com.anod.car.home.pro");
            //   debugPkgs.add("com.ibolt.carhome");
        }
        DocDetails.AppDetails appDetails = marketApp.getAppDetails();
        if (appDetails.versionCode > localApp.getVersionCode() || (debugPkgs!=null && debugPkgs.contains(localApp.getPackageName()))) {
            AppLog.d("New version found ["+appDetails.versionCode+"]");
            Bitmap icon = loadIcon(marketApp);
            AppInfo newApp = createNewVersion(marketApp, localApp, icon);
            client.update(newApp);
            String recentChanges = (updatedTitles.size() == 0) ? appDetails.recentChangesHtml : null;
            updatedTitles.add(new UpdatedApp(localApp.getAppId(),marketApp.getTitle(),appDetails.packageName, recentChanges));
            return;
        }

        AppLog.d("No update found for: "+localApp.getAppId());
        ContentValues values = new ContentValues();
        //Mark updated app as normal
        if (localApp.getStatus() == AppInfo.STATUS_UPDATED && lastUpdatesViewed) {
            localApp.setStatus(AppInfo.STATUS_NORMAL);
            AppLog.d("Mark application as old");
            values.put(AppListTable.Columns.KEY_STATUS, AppInfo.STATUS_NORMAL );
        }
        //Refresh app icon if it wasn't fetched previously
        fillMissingData(marketApp, localApp, values);

        if (values.size() > 0) {
            AppLog.d("ContentValues: "+values.toString());
            client.update(localApp.getRowId(), values);
        }
    }

    private void fillMissingData(Document marketApp, AppInfo localApp, ContentValues values) {
        if (localApp.getIcon() == null) {
            AppLog.d("Fetch missing icon");
            Bitmap icon = loadIcon(marketApp);
            if (icon != null) {
                byte[] iconData = BitmapUtils.flattenBitmap(icon);
                values.put(AppListTable.Columns.KEY_ICON_CACHE, iconData);
            }
        }
        if (TextUtils.isEmpty(localApp.getUploadDate())) {
            values.put(AppListTable.Columns.KEY_UPLOAD_DATE, marketApp.getAppDetails().uploadDate);
        }
        if (TextUtils.isEmpty(localApp.getVersionName())) {
            values.put(AppListTable.Columns.KEY_VERSION_NAME, marketApp.getAppDetails().versionString);
        }

        Common.Offer offer = DocUtils.getOffer(marketApp);

        if (!offer.currencyCode.equals(localApp.getPriceCur())) {
            values.put(AppListTable.Columns.KEY_PRICE_CURRENCY, offer.currencyCode);
        }
        if (!offer.formattedAmount.equals(localApp.getPriceText())) {
            values.put(AppListTable.Columns.KEY_PRICE_TEXT, offer.formattedAmount);
        }
        if (localApp.getPriceMicros() != offer.micros) {
            values.put(AppListTable.Columns.KEY_PRICE_MICROS, offer.micros);
        }
    }

    private Bitmap loadIcon(Document marketApp) {
        String imageUrl = DocUtils.getIconUrl(marketApp);
        if (imageUrl == null) {
            return null;
        }
        if (mIconSize == -1) {
            mIconSize = mContext.getResources().getDimensionPixelSize(R.dimen.icon_size);
        }

        ImageLoader.ImageContainer container = getImageLoader().get(imageUrl, null, mIconSize, mIconSize);
        return container.getBitmap();
    }

    private SyncImageLoader getImageLoader() {
        if (mImageLoader == null) {
            // No cache implementation
            mImageLoader = new SyncImageLoader(AppWatcherApplication.provide(mContext).requestQueue());
        }
        return mImageLoader;
    }

    private AppInfo createNewVersion(Document marketApp, AppInfo localApp, Bitmap newIcon)  {

		AppInfo newApp = new AppInfo(marketApp, newIcon);
		newApp.setRowId(localApp.getRowId());
		newApp.setStatus(AppInfo.STATUS_UPDATED);

		return newApp;
	}


	private BulkDetailsEndpoint createEndpoint(Preferences prefs) {
		AccountHelper tokenHelper = new AccountHelper(mContext);
		String authToken = null;
        Account account = prefs.getAccount();
		try {
			authToken = tokenHelper.requestTokenBlocking(null, account);
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
		String deviceId = DeviceIdHelper.getDeviceId(mContext, prefs);
        BulkDetailsEndpoint endpoint = new BulkDetailsEndpoint(null, mContext);
        endpoint.setAccount(account,authToken);
        return endpoint;
	}
	


}
