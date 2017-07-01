package com.anod.appwatcher.sync

import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.content.ContentProviderClient
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.RemoteException
import android.provider.BaseColumns
import android.text.TextUtils
import android.text.format.DateUtils
import com.android.volley.VolleyError
import com.anod.appwatcher.App
import com.anod.appwatcher.Preferences
import com.anod.appwatcher.accounts.AuthTokenProvider
import com.anod.appwatcher.backup.GDriveSync
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.market.BulkDetailsEndpoint
import com.anod.appwatcher.market.PlayStoreEndpoint
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.utils.AppDetailsUploadDate
import com.anod.appwatcher.utils.CollectionsUtils
import com.anod.appwatcher.utils.GooglePlayServices
import com.anod.appwatcher.utils.InstalledAppsProvider
import com.google.android.finsky.api.model.Document
import info.anodsplace.android.log.AppLog
import java.io.IOException
import java.util.*

/**
 *  @author alex
 *  @date 6/3/2017
 */

class SyncAdapter(private val context: Context): PlayStoreEndpoint.Listener {

    class UpdatedApp(
            val appId: String,
            val title: String,
            val pkg: String,
            val recentChanges: String,
            val versionCode: Int,
            val installedVersionCode: Int)

    companion object {
        private const val ONE_SEC_IN_MILLIS = 1000
        private const val BULK_SIZE = 20
        internal const val SYNC_EXTRAS_MANUAL = "manual"

        const val SYNC_STOP = "com.anod.appwatcher.sync.start"
        const val SYNC_PROGRESS = "com.anod.appwatcher.sync.progress"
        const val EXTRA_UPDATES_COUNT = "extra_updates_count"
    }

    private val preferences = App.provide(context).prefs
    private val installedAppsProvider = InstalledAppsProvider.PackageManager(context.packageManager)

    internal fun onPerformSync(extras: Bundle, provider: ContentProviderClient): Int {

        val manualSync = extras.getBoolean(SYNC_EXTRAS_MANUAL, false)
        // Skip any check if sync requested from application
        if (!manualSync) {
            if (preferences.isWifiOnly && !isWifiEnabled()) {
                AppLog.d("Wifi not enabled, skipping update check....")
                return -1
            }
            val updateTime = preferences.lastUpdateTime
            if (updateTime != -1.toLong() && System.currentTimeMillis() - updateTime < ONE_SEC_IN_MILLIS) {
                AppLog.d("Last update less than second, skipping...")
                return -1
            }
        }
        if (preferences.account == null) {
            AppLog.d("No active account, skipping sync...")
            return -1
        }

        AppLog.v("Perform synchronization")

        //Broadcast progress intent
        val startIntent = Intent(SYNC_PROGRESS)
        context.sendBroadcast(startIntent)

        val endpoint = createEndpoint(preferences)

        val lastUpdatesViewed = preferences.isLastUpdatesViewed
        AppLog.d("Last update viewed: " + lastUpdatesViewed)

        var updatedApps: List<UpdatedApp> = emptyList()
        val appListProvider = DbContentProviderClient(provider)
        try {
            updatedApps = doSync(appListProvider, lastUpdatesViewed, endpoint)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        val now = System.currentTimeMillis()
        preferences.lastUpdateTime = now

        if (!manualSync && updatedApps.isNotEmpty() && lastUpdatesViewed) {
            preferences.isLastUpdatesViewed = false
        }

        notifyIfNeeded(manualSync, updatedApps)

        if (!manualSync) {
            if (preferences.isDriveSyncEnabled) {
                AppLog.d("DriveSyncEnabled = true")
                performGDriveSync(preferences, now)
            } else {
                AppLog.d("DriveSyncEnabled = false, skipping...")
            }
        }

        AppLog.d("Finish::onPerformSync()")
        return updatedApps.size
    }

    override fun onDataChanged() {
    }

    override fun onErrorResponse(error: VolleyError) {
    }


    @Throws(RemoteException::class)
    private fun doSync(client: DbContentProviderClient, lastUpdatesViewed: Boolean, endpoint: BulkDetailsEndpoint?): ArrayList<UpdatedApp> {
        val updatedTitles = ArrayList<UpdatedApp>()

        if (endpoint == null) {
            return updatedTitles
        }

        val apps = client.queryAll(false)
        if (apps == null || !apps.moveToFirst()) {
            return updatedTitles
        }
        apps.moveToPosition(-1)

        val bulkSize = if (apps.count > BULK_SIZE) BULK_SIZE else apps.count

        val localApps = HashMap<String, AppInfo>(bulkSize)
        var i = 1
        while (apps.moveToNext()) {

            val localApp = apps.appInfo
            val docId = localApp.appId
            localApps.put(docId, localApp)

            if (localApps.size == bulkSize) {
                val docIds = localApps.keys
                AppLog.d("Sending bulk #$i... $docIds")
                val documents = requestBulkDetails(docIds, endpoint)
                if (true) {
                    updateApps(documents, localApps, client, updatedTitles, lastUpdatesViewed)
                } else {
                    AppLog.e("No documents were received.")
                }
                localApps.clear()
                i++
            }

        }
        if (localApps.size > 0) {
            val docIds = localApps.keys
            AppLog.d("Sending bulk #$i... $docIds")
            val documents = requestBulkDetails(docIds, endpoint)
            if (documents.isNotEmpty()) {
                updateApps(documents, localApps, client, updatedTitles, lastUpdatesViewed)
            } else {
                AppLog.e("No documents were received.")
            }
            localApps.clear()
        }
        apps.close()

        return updatedTitles
    }

    private fun updateApps(documents: List<Document>, localApps: Map<String, AppInfo>, client: DbContentProviderClient, updatedTitles: ArrayList<UpdatedApp>, lastUpdatesViewed: Boolean) {
        val fetched = mutableMapOf<String, Boolean>()
        val batch = mutableListOf<ContentValues>()
        for (marketApp in documents) {
            val docId = marketApp.docId
            val localApp = localApps[docId]!!
            fetched[docId] = true
            val values = updateApp(marketApp, localApp, updatedTitles, lastUpdatesViewed)
            if (values.size() > 0) {
                batch.add(values)
            }
        }

        if (batch.isNotEmpty()) {
            client.applyBatchUpdates(batch)
        }

        // Reset not fetched app statuses
        if (lastUpdatesViewed && fetched.size < localApps.size)
        {
            val statusBatch = mutableListOf<ContentValues>()
            localApps.values.forEach({
                if (fetched[it.appId] == null)
                {
                    if (it.status == AppInfoMetadata.STATUS_UPDATED) {
                        it.status = AppInfoMetadata.STATUS_NORMAL
                        AppLog.d("Mark not fetched app as old")
                        val values = ContentValues()
                        values.put(BaseColumns._ID, it.rowId)
                        values.put(AppListTable.Columns.KEY_STATUS, AppInfoMetadata.STATUS_NORMAL)
                        statusBatch.add(values)
                    }
                }
            })
            if (batch.isNotEmpty()) {
                client.applyBatchUpdates(statusBatch)
            }
        }
    }

    private fun updateApp(marketApp: Document, localApp: AppInfo, updatedTitles: ArrayList<UpdatedApp>, lastUpdatesViewed: Boolean): ContentValues {
        val appDetails = marketApp.appDetails

        if (appDetails.versionCode > localApp.versionNumber) {
            AppLog.d("New version found [" + appDetails.versionCode + "]")
            val newApp = createNewVersion(marketApp, localApp)

            val recentChanges = if (updatedTitles.size == 0) appDetails.recentChangesHtml else ""
            val installedInfo = installedAppsProvider.getInfo(appDetails.packageName)
            updatedTitles.add(UpdatedApp(localApp.appId, marketApp.title, appDetails.packageName, recentChanges!!, appDetails.versionCode, installedInfo.versionCode))
            return AppListTable.createContentValues(newApp)
        }

        AppLog.d("No update found for: " + localApp.appId)
        val values = ContentValues()
        //Mark updated app as normal
        if (localApp.status == AppInfoMetadata.STATUS_UPDATED && lastUpdatesViewed) {
            localApp.status = AppInfoMetadata.STATUS_NORMAL
            AppLog.d("Mark application as old")
            values.put(AppListTable.Columns.KEY_STATUS, AppInfoMetadata.STATUS_NORMAL)
        }
        //Refresh app icon if it wasn't fetched previously
        fillMissingData(marketApp, localApp, values)
        if (values.size() > 0) {
            values.put(BaseColumns._ID, localApp.rowId)
        }
        return values
    }

    private fun notifyIfNeeded(manualSync: Boolean, updatedApps: List<UpdatedApp>) {
        val sn = SyncNotification(context)
        if (manualSync) {
            sn.cancel()
        } else if (updatedApps.isNotEmpty()) {
            var filteredApps = updatedApps

            if (!preferences.isNotifyInstalledUpToDate) {
                filteredApps = CollectionsUtils.filter(updatedApps, object : CollectionsUtils.Predicate<UpdatedApp> {
                    override fun test(t: UpdatedApp): Boolean {
                        return t.installedVersionCode > 0 && t.versionCode <= t.installedVersionCode
                    }
                })
            }
            if (filteredApps.isNotEmpty()) {
                val notification = sn.create(filteredApps)
                sn.show(notification)
            }
        }
    }

    private fun performGDriveSync(pref: Preferences, now: Long) {
        val driveSyncTime = pref.lastDriveSyncTime
        if (driveSyncTime == -1.toLong() || now > DateUtils.DAY_IN_MILLIS + driveSyncTime) {
            AppLog.d("DriveSync perform sync")
            val driveSync = GDriveSync(context)
            try {
                driveSync.syncLocked()
                pref.lastDriveSyncTime = System.currentTimeMillis()
            } catch (e: GooglePlayServices.ResolutionException) {
                driveSync.showResolutionNotification(e.resolution)
                AppLog.e(e)
            } catch (e: Exception) {
                AppLog.e(e)
            }

        } else {
            AppLog.d("DriveSync backup is fresh")
        }
    }

    /**
     * Check if device has wi-fi connection
     */
    private fun isWifiEnabled(): Boolean {
        val activeNetwork = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (activeNetwork == null) {
            AppLog.e("No active network info")
            return false
        }
        return activeNetwork.type == ConnectivityManager.TYPE_WIFI
    }

    private fun requestBulkDetails(docIds: Set<String>, endpoint: BulkDetailsEndpoint): List<Document> {
        val listDocIds = ArrayList(docIds)
        endpoint.setDocIds(listDocIds)
        endpoint.startSync()
        return endpoint.documents
    }

    private fun createNewVersion(marketApp: Document, localApp: AppInfo): AppInfo {
        val newApp = AppInfo(marketApp)
        newApp.rowId = localApp.rowId
        newApp.status = AppInfoMetadata.STATUS_UPDATED
        return newApp
    }

    private fun createEndpoint(prefs: Preferences): BulkDetailsEndpoint? {
        val tokenHelper = AuthTokenProvider(context)
        var authToken: String? = null
        val account = prefs.account ?: return null
        try {
            authToken = tokenHelper.requestTokenBlocking(null, account)
        } catch (e: IOException) {
            AppLog.e("AuthToken IOException: " + e.message, e)
        } catch (e: AuthenticatorException) {
            AppLog.e("AuthToken AuthenticatorException: " + e.message, e)
        } catch (e: OperationCanceledException) {
            AppLog.e("AuthToken OperationCanceledException: " + e.message, e)
        }

        if (authToken == null) {
            return null
        }
        val endpoint = BulkDetailsEndpoint(context)
        endpoint.setAccount(account, authToken)
        return endpoint
    }

    private fun fillMissingData(marketApp: Document, localApp: AppInfo, values: ContentValues) {
        val refreshTime = AppDetailsUploadDate.extract(marketApp)
        values.put(AppListTable.Columns.KEY_REFRESH_TIMESTAMP, refreshTime)
        values.put(AppListTable.Columns.KEY_UPLOAD_DATE, marketApp.appDetails.uploadDate)
        if (TextUtils.isEmpty(localApp.versionName)) {
            values.put(AppListTable.Columns.KEY_VERSION_NAME, marketApp.appDetails.versionString)
        }

        if (marketApp.appDetails.appType != localApp.appType) {
            values.put(AppListTable.Columns.KEY_APP_TYPE, marketApp.appDetails.appType)
        }

        val offer = marketApp.offer
        if (offer.currencyCode != localApp.priceCur) {
            values.put(AppListTable.Columns.KEY_PRICE_CURRENCY, offer.currencyCode)
        }
        if (offer.formattedAmount != localApp.priceText) {
            values.put(AppListTable.Columns.KEY_PRICE_TEXT, offer.formattedAmount)
        }
        if (localApp.priceMicros != offer.micros.toInt()) {
            values.put(AppListTable.Columns.KEY_PRICE_MICROS, offer.micros)
        }

        val iconUrl = marketApp.iconUrl
        if (!TextUtils.isEmpty(iconUrl)) {
            values.put(AppListTable.Columns.KEY_ICON_URL, marketApp.iconUrl)
        }
    }
}