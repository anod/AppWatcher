package com.anod.appwatcher.sync

import android.accounts.Account
import android.content.ContentProviderClient
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.provider.BaseColumns
import android.text.TextUtils
import android.text.format.DateUtils
import com.android.volley.VolleyError
import com.anod.appwatcher.App
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.accounts.AuthTokenBlocking
import com.anod.appwatcher.backup.gdrive.GDriveSilentSignIn
import com.anod.appwatcher.backup.gdrive.SyncConnectedWorker
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.AppChange
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata
import com.anod.appwatcher.model.schema.AppListTable
import com.anod.appwatcher.model.schema.ChangelogTable
import com.anod.appwatcher.model.schema.contentValues
import com.anod.appwatcher.utils.extractUploadDate
import finsky.api.model.DfeModel
import finsky.api.model.Document
import info.anodsplace.android.log.AppLog
import info.anodsplace.appwatcher.framework.ApplicationContext
import info.anodsplace.appwatcher.framework.InstalledApps
import info.anodsplace.playstore.BulkDetailsEndpoint
import info.anodsplace.playstore.PlayStoreEndpoint
import java.util.*

/**
 *  @author alex
 *  @date 6/3/2017
 */

class UpdateCheck(private val context: ApplicationContext): PlayStoreEndpoint.Listener {

    class UpdatedApp(
        val appId: String,
        val title: String,
        val pkg: String,
        val recentChanges: String,
        val versionCode: Int,
        val installedVersionCode: Int,
        val isNewUpdate: Boolean)

    companion object {
        private const val oneSecInMillis = 1000
        private const val bulkSize = 20
        internal const val extrasManual = "manual"

        const val syncStop = "com.anod.appwatcher.sync.start"
        const val syncProgress = "com.anod.appwatcher.sync.progress"
        const val extrasUpdatesCount = "extra_updates_count"
    }

    private val preferences = App.provide(context).prefs
    private val installedAppsProvider = InstalledApps.PackageManager(context.packageManager)

    fun perform(extras: Bundle, provider: ContentProviderClient): Int {

        val manualSync = extras.getBoolean(extrasManual, false)
        // Skip any check if sync requested from application
        if (!manualSync) {
            if (preferences.isWifiOnly && !App.with(context).isWifiEnabled) {
                AppLog.d("Wifi not enabled, skipping update check....")
                return -1
            }
            val updateTime = preferences.lastUpdateTime
            if (updateTime != (-1).toLong() && System.currentTimeMillis() - updateTime < oneSecInMillis) {
                AppLog.d("Last update less than second, skipping...")
                return -1
            }
        }
        val account = preferences.account
        if (account == null) {
            AppLog.d("No active account, skipping sync...")
            return -1
        }

        AppLog.v("Perform synchronization")

        val authToken = requestAuthToken(account)
        if (authToken == null) {
            AppLog.e("Cannot receive token")
            return -1
        }

        //Broadcast progress intent
        val startIntent = Intent(syncProgress)
        context.sendBroadcast(startIntent)

        val lastUpdatesViewed = preferences.isLastUpdatesViewed
        AppLog.d("Last update viewed: " + lastUpdatesViewed)

        var updatedApps: List<UpdatedApp> = emptyList()
        val appListProvider = DbContentProviderClient(provider)
        try {
            updatedApps = doSync(appListProvider, lastUpdatesViewed, authToken, account)
        } catch (e: RemoteException) {
            AppLog.e(e)
        }

        val now = System.currentTimeMillis()
        preferences.lastUpdateTime = now

        if (!manualSync && (updatedApps.firstOrNull { it.isNewUpdate } != null)) {
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

        AppLog.d("Finish::perform()")
        return updatedApps.size
    }

    override fun onDataChanged(data: DfeModel) {
    }

    override fun onErrorResponse(error: VolleyError) {
    }

    @Throws(RemoteException::class)
    private fun doSync(client: DbContentProviderClient, lastUpdatesViewed: Boolean, authToken: String, account: Account): List<UpdatedApp> {

        val apps = client.queryAll(false)
        if (!apps.moveToFirst()) {
            return listOf()
        }
        apps.moveToPosition(-1)

        val bulkSize = if (apps.count > bulkSize) bulkSize else apps.count

        val localApps = HashMap<String, AppInfo>(bulkSize)
        var i = 1
        val updatedTitles = mutableListOf<UpdatedApp>()
        while (apps.moveToNext()) {

            val localApp = apps.appInfo
            val docId = localApp.appId
            localApps.put(docId, localApp)

            if (localApps.size == bulkSize) {
                val docIds = localApps.keys.toList()
                val endpoint = createEndpoint(docIds, authToken, account)
                AppLog.d("Sending bulk #$i... $docIds")
                endpoint.startSync()
                updateApps(endpoint.documents, localApps, client, updatedTitles, lastUpdatesViewed)
                localApps.clear()
                i++
            }

        }
        if (localApps.size > 0) {
            val docIds = localApps.keys.toList()
            val endpoint = createEndpoint(docIds, authToken, account)
            AppLog.d("Sending bulk #$i... $docIds")
            endpoint.startSync()
            updateApps(endpoint.documents, localApps, client, updatedTitles, lastUpdatesViewed)
            localApps.clear()
        }
        apps.close()

        return updatedTitles
    }

    private fun updateApps(documents: List<Document>, localApps: Map<String, AppInfo>, client: DbContentProviderClient, updatedTitles: MutableList<UpdatedApp>, lastUpdatesViewed: Boolean) {
        val fetched = mutableMapOf<String, Boolean>()
        val batch = mutableListOf<ContentValues>()
        val changelog = mutableListOf<ContentValues>()
        for (marketApp in documents) {
            val docId = marketApp.docId
            localApps[docId]?.let {
                fetched[docId] = true
                val values = updateApp(marketApp, it, updatedTitles, lastUpdatesViewed)
                if (values.size() > 0) {
                    batch.add(values)
                }
                val recentChanges = marketApp.appDetails.recentChangesHtml ?: ""
                if (recentChanges.isNotBlank()) {
                    changelog.add(AppChange(docId, marketApp.appDetails.versionCode, marketApp.appDetails.versionString, recentChanges).contentValues)
                }
            }
        }

        if (batch.isNotEmpty()) {
            client.applyBatchUpdates(batch) {
                val rowId = it.getAsString(BaseColumns._ID)
                DbContentProvider.appsUri.buildUpon().appendPath(rowId).build()
            }
        }

        if (changelog.isNotEmpty()) {
            client.applyBatchInsert(changelog) {
                DbContentProvider.changelogUri
                        .buildUpon()
                        .appendPath("apps")
                        .appendPath(it.getAsString(ChangelogTable.Columns.appId))
                        .appendPath("v")
                        .appendPath(it.getAsString(ChangelogTable.Columns.versionCode))
                        .build()
            }
        }

        // Reset not fetched app statuses
        if (lastUpdatesViewed && fetched.size < localApps.size) {
            val statusBatch = mutableListOf<ContentValues>()
            localApps.values.forEach({
                if (fetched[it.appId] == null) {
                    if (it.status == AppInfoMetadata.STATUS_UPDATED) {
                        it.status = AppInfoMetadata.STATUS_NORMAL
                        AppLog.d("Set not fetched app as viewed")
                        val values = ContentValues()
                        values.put(BaseColumns._ID, it.rowId)
                        values.put(AppListTable.Columns.status, AppInfoMetadata.STATUS_NORMAL)
                        statusBatch.add(values)
                    }
                }
            })
            if (statusBatch.isNotEmpty()) {
                client.applyBatchUpdates(statusBatch) {
                    val rowId = it.getAsString(BaseColumns._ID)
                    DbContentProvider.appsUri.buildUpon().appendPath(rowId).build()
                }
            }
        }
    }

    private fun updateApp(marketApp: Document, localApp: AppInfo, updatedTitles: MutableList<UpdatedApp>, lastUpdatesViewed: Boolean): ContentValues {
        val appDetails = marketApp.appDetails

        if (appDetails.versionCode > localApp.versionNumber) {
            AppLog.d("New version found [" + appDetails.versionCode + "]")
            val newApp = AppInfo(localApp.rowId, AppInfoMetadata.STATUS_UPDATED, marketApp)
            val installedInfo = installedAppsProvider.packageInfo(appDetails.packageName)
            val recentChanges = if (updatedTitles.size == 0) appDetails.recentChangesHtml ?: "" else ""
            updatedTitles.add(
                UpdatedApp(
                    localApp.appId,
                    marketApp.title,
                    appDetails.packageName,
                    recentChanges,
                    appDetails.versionCode,
                    installedInfo.versionCode,
                    true
                )
            )
            return newApp.contentValues
        }

        AppLog.d("No update found for: " + localApp.appId)
        val values = ContentValues()

        //Mark updated app as normal
        if (localApp.status == AppInfoMetadata.STATUS_UPDATED && lastUpdatesViewed) {
            AppLog.d("Set application update as viewed")
            localApp.status = AppInfoMetadata.STATUS_NORMAL
            values.put(AppListTable.Columns.status, AppInfoMetadata.STATUS_NORMAL)
        } else if (localApp.status == AppInfoMetadata.STATUS_UPDATED) {
            // App was previously updated
            val installedInfo = installedAppsProvider.packageInfo(appDetails.packageName)
            val recentChanges = if (updatedTitles.size == 0) appDetails.recentChangesHtml ?: "" else ""
            updatedTitles.add(
                UpdatedApp(
                    localApp.appId,
                    marketApp.title,
                    appDetails.packageName,
                    recentChanges,
                    appDetails.versionCode,
                    installedInfo.versionCode,
                    false
                )
            )
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
        } else if (updatedApps.firstOrNull { it.isNewUpdate } != null) {
            var filteredApps = updatedApps

            if (!preferences.isNotifyInstalledUpToDate) {
                filteredApps = updatedApps.filter {
                    it.installedVersionCode > 0 && it.versionCode <= it.installedVersionCode
                }
            }
            if (filteredApps.isNotEmpty()) {
                sn.show(filteredApps)
            }
        }
    }

    private fun performGDriveSync(pref: Preferences, now: Long) {
        val driveSyncTime = pref.lastDriveSyncTime
        if (driveSyncTime == (-1).toLong() || now > DateUtils.DAY_IN_MILLIS + driveSyncTime) {
            AppLog.d("DriveSync perform sync")
            val signIn = GDriveSilentSignIn(context)

            try {
                val googleAccount = signIn.signInLocked()
                val worker = SyncConnectedWorker(context, googleAccount)
                worker.doSyncInBackground()
                pref.lastDriveSyncTime = System.currentTimeMillis()
            } catch (e: Exception) {
                AppLog.e("Perform Google Drive sync exception: ${e.message ?: "'empty message'"}", e)
            }
        } else {
            AppLog.d("DriveSync backup is fresh")
        }
    }

    private fun requestAuthToken(account: Account): String? {
        val tokenHelper = AuthTokenBlocking(context)
        var authToken: String? = null
        try {
            authToken = tokenHelper.request(null, account)
        } catch (e: Throwable) {
            AppLog.e("AuthTokenBlocking request exception: " + e.message, e)
        }

        return authToken
    }

    private fun createEndpoint(docIds: List<String>, authToken: String, account: Account): BulkDetailsEndpoint {
        val endpoint = BulkDetailsEndpoint(context.actual, App.provide(context).requestQueue, App.provide(context).deviceInfo, account, docIds)
        endpoint.authToken = authToken
        endpoint.listener = this
        return endpoint
    }

    private fun fillMissingData(marketApp: Document, localApp: AppInfo, values: ContentValues) {
        val refreshTime = marketApp.extractUploadDate()
        values.put(AppListTable.Columns.refreshTimestamp, refreshTime)
        values.put(AppListTable.Columns.uploadDate, marketApp.appDetails.uploadDate)
        if (TextUtils.isEmpty(localApp.versionName)) {
            values.put(AppListTable.Columns.versionName, marketApp.appDetails.versionString)
        }

        if (marketApp.appDetails.appType != localApp.appType) {
            values.put(AppListTable.Columns.appType, marketApp.appDetails.appType)
        }

        val offer = marketApp.offer
        if (offer.currencyCode != localApp.priceCur) {
            values.put(AppListTable.Columns.priceCurrency, offer.currencyCode)
        }
        if (offer.formattedAmount != localApp.priceText) {
            values.put(AppListTable.Columns.priceText, offer.formattedAmount)
        }
        if (localApp.priceMicros != offer.micros.toInt()) {
            values.put(AppListTable.Columns.priceMicros, offer.micros)
        }

        val iconUrl = marketApp.iconUrl
        if (!TextUtils.isEmpty(iconUrl)) {
            values.put(AppListTable.Columns.iconUrl, marketApp.iconUrl)
        }
    }
}