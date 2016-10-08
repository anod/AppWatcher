package com.anod.appwatcher.sync;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Notification;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.android.volley.VolleyError;
import com.anod.appwatcher.BuildConfig;
import com.anod.appwatcher.Preferences;
import com.anod.appwatcher.accounts.AuthTokenProvider;
import com.anod.appwatcher.backup.GDriveSync;
import com.anod.appwatcher.market.BulkDetailsEndpoint;
import com.anod.appwatcher.market.PlayStoreEndpoint;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.model.schema.AppListTable;
import com.anod.appwatcher.utils.CollectionsUtils;
import com.anod.appwatcher.utils.DocUtils;
import com.anod.appwatcher.utils.GooglePlayServices;
import com.anod.appwatcher.utils.PackageManagerUtils;
import com.google.android.finsky.api.model.Document;
import com.google.android.finsky.protos.Common;
import com.google.android.finsky.protos.DocDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import info.anodsplace.android.log.AppLog;

public class SyncAdapter implements PlayStoreEndpoint.Listener {
    private static final int ONE_SEC_IN_MILLIS = 1000;
    private static final int BULK_SIZE = 20;
    static final String SYNC_EXTRAS_MANUAL = "manual";

    private final Context mContext;
    private final PackageManagerUtils mPMUtils;
    private final Preferences mPreferences;

    public static final String SYNC_STOP = "com.anod.appwatcher.sync.start";
    public static final String SYNC_PROGRESS = "com.anod.appwatcher.sync.progress";
    public static final String EXTRA_UPDATES_COUNT = "extra_updates_count";

    public SyncAdapter(Context context) {
        mContext = context;
        mPMUtils = new PackageManagerUtils(context.getPackageManager());
        mPreferences = new Preferences(mContext);
    }

    int onPerformSync(Bundle extras, ContentProviderClient provider) {

        boolean manualSync = extras.getBoolean(SYNC_EXTRAS_MANUAL, false);
        // Skip any check if sync requested from application
        if (!manualSync) {
            if (mPreferences.isWifiOnly() && !isWifiEnabled()) {
                AppLog.d("Wifi not enabled, skipping update check....");
                return -1;
            }
            long updateTime = mPreferences.getLastUpdateTime();
            if (updateTime != -1 && (System.currentTimeMillis() - updateTime < ONE_SEC_IN_MILLIS)) {
                AppLog.d("Last update less than second, skipping...");
                return -1;
            }
        }
        if (mPreferences.getAccount() == null) {
            AppLog.d("No active account, skipping sync...");
            return -1;
        }

        AppLog.v("Perform synchronization");

        //Broadcast progress intent
        Intent startIntent = new Intent(SYNC_PROGRESS);
        mContext.sendBroadcast(startIntent);


        BulkDetailsEndpoint endpoint = createEndpoint(mPreferences);

        boolean lastUpdatesViewed = mPreferences.isLastUpdatesViewed();
        AppLog.d("Last update viewed: " + lastUpdatesViewed);

        ArrayList<UpdatedApp> updatedApps = null;
        AppListContentProviderClient appListProvider = new AppListContentProviderClient(provider);
        try {
            updatedApps = doSync(appListProvider, lastUpdatesViewed, endpoint);
        } catch (RemoteException e) {
            AppLog.e(e);
        }
        int size = (updatedApps != null) ? updatedApps.size() : 0;
        long now = System.currentTimeMillis();
        mPreferences.updateLastTime(now);

        if (!manualSync && size > 0 && lastUpdatesViewed) {
            mPreferences.markViewed(false);
        }

        notifyIfNeeded(manualSync, size, updatedApps);

        if (!manualSync) {
            if (mPreferences.isDriveSyncEnabled()) {
                AppLog.d("DriveSyncEnabled = true");
                performGDriveSync(mPreferences, now);
            } else {
                AppLog.d("DriveSyncEnabled = false, skipping...");
            }
        }

        AppLog.d("Finish::onPerformSync()");
        return size;
    }

    private void notifyIfNeeded(boolean manualSync, int size, List<UpdatedApp> updatedApps) {
        SyncNotification sn = new SyncNotification(mContext);
        if (manualSync) {
            sn.cancel();
        } else if (size > 0) {
            if (!mPreferences.isNotifyInstalledUpToDate())
            {
                updatedApps = CollectionsUtils.filter(updatedApps, new CollectionsUtils.Predicate<UpdatedApp>() {
                    @Override
                    public boolean test(UpdatedApp updatedApp) {
                        return updatedApp.isInstalledUpToDate;
                    }
                });
            }
            size = updatedApps.size();
            if (size > 0) {
                Notification notification = sn.create(updatedApps);
                sn.show(notification);
            }
        }
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
                if (e.getResolution() != null) {
                    driveSync.showResolutionNotification(e.getResolution());
                }
                AppLog.e(e);
            } catch (Exception e) {
                AppLog.e(e);
            }
        } else {
            AppLog.d("DriveSync backup is fresh");
        }
    }

    /**
     * Check if device has wi-fi connection
     */
    private boolean isWifiEnabled() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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

    static class UpdatedApp {
        final String appId;
        final String title;
        final String pkg;
        final String recentChanges;
        final boolean isInstalledUpToDate;

        private UpdatedApp(String appId, String title, String pkg, String recentChanges, boolean isInstalledUpToDate) {
            this.appId = appId;
            this.title = title;
            this.pkg = pkg;
            this.recentChanges = recentChanges;
            this.isInstalledUpToDate = isInstalledUpToDate;
        }
    }

    /**
     * @return list of titles that were updated
     * @throws RemoteException
     */
    private ArrayList<UpdatedApp> doSync(AppListContentProviderClient client, boolean lastUpdatesViewed, BulkDetailsEndpoint endpoint) throws RemoteException {
        ArrayList<UpdatedApp> updatedTitles = new ArrayList<UpdatedApp>();

        if (endpoint == null) {
            return updatedTitles;
        }

        AppListCursor apps = client.queryAll(false);
        if (apps == null || !apps.moveToFirst()) {
            return updatedTitles;
        }
        apps.moveToPosition(-1);

        int bulkSize = apps.getCount() > BULK_SIZE ? BULK_SIZE : apps.getCount();

        HashMap<String, AppInfo> localApps = new HashMap<>(bulkSize);



        int i = 1;
        while (apps.moveToNext()) {

            AppInfo localApp = apps.getAppInfo();
            String docId = localApp.getAppId();
            localApps.put(docId, localApp);

            if (localApps.size() == bulkSize) {
                Set<String> docIds = localApps.keySet();
                AppLog.d("Sending bulk #" + i + "... " + docIds);
                List<Document> documents = requestBulkDetails(docIds, endpoint);
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
            AppLog.d("Sending bulk #" + i + "... " + docIds);
            List<Document> documents = requestBulkDetails(docIds, endpoint);
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

    private List<Document> requestBulkDetails(Set<String> docIds, BulkDetailsEndpoint endpoint) {
        List<String> listDocIds = new ArrayList<String>(docIds);
        endpoint.setDocIds(listDocIds);
        endpoint.startSync();
        return endpoint.getDocuments();
    }

    private void updateApps(List<Document> documents, HashMap<String, AppInfo> localApps, AppListContentProviderClient client, ArrayList<UpdatedApp> updatedTitles, boolean lastUpdatesViewed) {
        for (Document marketApp : documents) {
            String docId = marketApp.getDocId();
            AppInfo localApp = localApps.get(docId);
            updateApp(marketApp, localApp, client, updatedTitles, lastUpdatesViewed);
        }
    }

    private void updateApp(Document marketApp, AppInfo localApp, AppListContentProviderClient client, ArrayList<UpdatedApp> updatedTitles, boolean lastUpdatesViewed) {
        DocDetails.AppDetails appDetails = marketApp.getAppDetails();

        if (appDetails.versionCode > localApp.versionNumber) {
            AppLog.d("New version found [" + appDetails.versionCode + "]");
            AppInfo newApp = createNewVersion(marketApp, localApp);
            client.update(newApp);
            String recentChanges = (updatedTitles.size() == 0) ? appDetails.recentChangesHtml : null;

            boolean isInstalledUpToDate = false;
            PackageManagerUtils.InstalledInfo installedInfo = mPMUtils.getInstalledInfo(appDetails.packageName);
            if (installedInfo != null)
            {
                isInstalledUpToDate = appDetails.versionCode == installedInfo.versionCode;
            }

            updatedTitles.add(new UpdatedApp(localApp.getAppId(), marketApp.getTitle(), appDetails.packageName, recentChanges, isInstalledUpToDate));
            return;
        }

        AppLog.d("No update found for: " + localApp.getAppId());
        ContentValues values = new ContentValues();
        //Mark updated app as normal
        if (localApp.getStatus() == AppInfo.STATUS_UPDATED && lastUpdatesViewed) {
            localApp.setStatus(AppInfo.STATUS_NORMAL);
            AppLog.d("Mark application as old");
            values.put(AppListTable.Columns.KEY_STATUS, AppInfo.STATUS_NORMAL);
        }
        //Refresh app icon if it wasn't fetched previously
        fillMissingData(marketApp, localApp, values);

        if (values.size() > 0) {
            AppLog.d("ContentValues: " + values.toString());
            client.update(localApp.getRowId(), values);
        }
    }

    private void fillMissingData(Document marketApp, AppInfo localApp, ContentValues values) {
        long refreshTime = DocUtils.extractDate(marketApp);
        values.put(AppListTable.Columns.KEY_REFRESH_TIMESTAMP, refreshTime);
        values.put(AppListTable.Columns.KEY_UPLOAD_DATE, marketApp.getAppDetails().uploadDate);
        if (TextUtils.isEmpty(localApp.versionName)) {
            values.put(AppListTable.Columns.KEY_VERSION_NAME, marketApp.getAppDetails().versionString);
        }

        Common.Offer offer = DocUtils.getOffer(marketApp);

        if (!offer.currencyCode.equals(localApp.priceCur)) {
            values.put(AppListTable.Columns.KEY_PRICE_CURRENCY, offer.currencyCode);
        }
        if (!offer.formattedAmount.equals(localApp.priceText)) {
            values.put(AppListTable.Columns.KEY_PRICE_TEXT, offer.formattedAmount);
        }
        if (localApp.priceMicros != offer.micros) {
            values.put(AppListTable.Columns.KEY_PRICE_MICROS, offer.micros);
        }
        String iconUrl = DocUtils.getIconUrl(marketApp);
        if (!TextUtils.isEmpty(iconUrl))
        {
            values.put(AppListTable.Columns.KEY_ICON_URL, DocUtils.getIconUrl(marketApp));
        }
    }


    private AppInfo createNewVersion(Document marketApp, AppInfo localApp) {
        AppInfo newApp = new AppInfo(marketApp);
        newApp.setRowId(localApp.getRowId());
        newApp.setStatus(AppInfo.STATUS_UPDATED);
        return newApp;
    }


    private BulkDetailsEndpoint createEndpoint(Preferences prefs) {
        AuthTokenProvider tokenHelper = new AuthTokenProvider(mContext);
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
        BulkDetailsEndpoint endpoint = new BulkDetailsEndpoint(mContext);
        endpoint.setAccount(account, authToken);
        return endpoint;
    }


}
