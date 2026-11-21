package com.anod.appwatcher.sync

import android.app.Application
import android.app.Notification
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.ChangelogTable
import com.anod.appwatcher.database.contentValues
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import finsky.protos.AppDetails
import finsky.protos.Details
import finsky.protos.DocDetails
import finsky.protos.DocV2
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * The notification settings decide which of the updates found by a scheduled refresh are announced.
 * A manual refresh reports its updates in the app instead and never posts a notification.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class UpdateCheckNotificationSettingsTest {
    private val manualSync = Data.Builder().putBoolean(UpdateCheck.EXTRAS_MANUAL, true).build()
    private val remoteApps = mutableMapOf<String, RemoteApp>()
    private lateinit var harness: UpdateCheckHarness
    private val notifications get() = harness.notifications
    private val preferences get() = harness.preferences
    private val database get() = harness.database
    private val updateCheck get() = harness.updateCheck

    @Before
    fun setUp() {
        harness = UpdateCheckHarness(ApplicationProvider.getApplicationContext())
        harness.dfeApi.bulkDetailsResponse = { docIds, forUpdateCheck ->
            val response = Details.BulkDetailsResponse.newBuilder()
            for (docId in docIds) {
                val remote = remoteApps[docId.packageName] ?: continue
                response.addEntry(
                    Details.BulkDetailsEntry.newBuilder()
                        .setDoc(remote.toDocument(docId.packageName, full = !forUpdateCheck))
                        .build()
                )
            }
            response.build()
        }
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun scheduledSyncAnnouncesAnUpdateToAnAppThatIsNotInstalled() = runBlocking {
        watch("Alpha")

        assertEquals(1, scheduledSync())

        assertEquals(1, lastSchedule().notified)
        assertEquals(listOf(SyncNotification.SYNC_NOTIFICATION_ID to "New version of Alpha"), announcements)
    }

    @Test
    fun scheduledSyncAnnouncesAnUpdateToAnInstalledAppByDefault() = runBlocking {
        watch("Alpha", installed = 1)

        assertEquals(1, scheduledSync())

        assertEquals(1, lastSchedule().notified)
        assertEquals(listOf(SyncNotification.SYNC_NOTIFICATION_ID to "New version of Alpha"), announcements)
    }

    @Test
    fun scheduledSyncAnnouncesSeveralUpdatesInOneNotification() = runBlocking {
        watch("Zeta")
        watch("Alpha", installed = 1)

        assertEquals(2, scheduledSync())

        assertEquals(2, lastSchedule().notified)
        assertEquals(listOf(SyncNotification.SYNC_NOTIFICATION_ID to "New versions for 2 apps"), announcements)
        assertEquals("Alpha, Zeta", notifications.posted.single().second.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
    }

    @Test
    fun installedNotificationsOffSuppressesAnUpdateToAnInstalledApp() = runBlocking {
        preferences.isNotifyInstalled = false
        watch("Alpha", installed = 1)

        assertEquals(1, scheduledSync())

        val schedule = lastSchedule()
        assertEquals(1, schedule.found)
        assertEquals(0, schedule.notified)
        assertTrue(notifications.posted.isEmpty())
    }

    @Test
    fun installedNotificationsOffStillAnnouncesAnUpdateToAnAppThatIsNotInstalled() = runBlocking {
        preferences.isNotifyInstalled = false
        watch("Alpha", installed = 1)
        watch("Beta")

        assertEquals(2, scheduledSync())

        assertEquals(1, lastSchedule().notified)
        assertEquals(listOf(SyncNotification.SYNC_NOTIFICATION_ID to "New version of Beta"), announcements)
    }

    @Test
    fun noChangesNotificationsOffSuppressesAnUpdateWhoseChangelogIsUnchanged() = runBlocking {
        preferences.isNotifyNoChanges = false
        watch("Alpha", cachedChanges = "Bug fixes and performance improvements", remoteChanges = "Bug fixes and performance improvements.")

        assertEquals(1, scheduledSync())

        assertEquals(0, lastSchedule().notified)
        assertTrue(notifications.posted.isEmpty())
    }

    @Test
    fun noChangesNotificationsOffSuppressesAnUpdateWithoutAnyChangelog() = runBlocking {
        preferences.isNotifyNoChanges = false
        watch("Alpha", remoteChanges = "")

        assertEquals(1, scheduledSync())

        assertEquals(0, lastSchedule().notified)
        assertTrue(notifications.posted.isEmpty())
    }

    @Test
    fun noChangesNotificationsOffStillAnnouncesAnUpdateWhoseChangelogDiffers() = runBlocking {
        preferences.isNotifyNoChanges = false
        watch("Alpha", cachedChanges = "Bug fixes", remoteChanges = "A new dark theme")

        assertEquals(1, scheduledSync())

        assertEquals(1, lastSchedule().notified)
        assertEquals(listOf(SyncNotification.SYNC_NOTIFICATION_ID to "New version of Alpha"), announcements)
    }

    @Test
    fun noChangesNotificationsOnAnnouncesAnUpdateWhoseChangelogIsUnchanged() = runBlocking {
        watch("Alpha", cachedChanges = "Bug fixes", remoteChanges = "Bug fixes")

        assertEquals(1, scheduledSync())

        assertEquals(1, lastSchedule().notified)
        assertEquals(listOf(SyncNotification.SYNC_NOTIFICATION_ID to "New version of Alpha"), announcements)
    }

    @Test
    fun installedAndNoChangesFiltersApplyTogether() = runBlocking {
        preferences.isNotifyInstalled = false
        preferences.isNotifyNoChanges = false
        watch("Alpha", installed = 1, remoteChanges = "Fresh notes")
        watch("Beta", cachedChanges = "Same notes", remoteChanges = "Same notes")
        watch("Gamma", cachedChanges = "Old notes", remoteChanges = "Fresh notes")

        assertEquals(3, scheduledSync())

        assertEquals(1, lastSchedule().notified)
        assertEquals(listOf(SyncNotification.SYNC_NOTIFICATION_ID to "New version of Gamma"), announcements)
    }

    /**
     * An installed app is never reported once the device has caught up with the store, so the
     * "Updated notifications" setting has nothing left to filter in a sync.
     */
    @Test
    fun appsAlreadyUpdatedOnTheDeviceAreNeverAnnouncedWhateverUpdatedNotificationsIs() = runBlocking {
        for ((index, updatedNotifications) in listOf(true, false).withIndex()) {
            preferences.isNotifyInstalledUpToDate = updatedNotifications
            val name = "App$index"
            watch(name, installed = 2)

            assertEquals(0, scheduledSync())

            assertEquals(0, lastSchedule().found)
            assertTrue(notifications.posted.isEmpty())
            assertEquals(2, database.apps().loadApp("com.example.${name.lowercase()}")!!.versionNumber)
        }
    }

    @Test
    fun scheduledSyncWithoutUpdatesPostsNothing() = runBlocking {
        watch("Alpha", cached = 2, remote = 2)

        assertEquals(0, scheduledSync())

        assertEquals(0, lastSchedule().notified)
        assertTrue(notifications.posted.isEmpty())
    }

    @Test
    fun manualSyncReportsUpdatesWithoutPostingANotification() = runBlocking {
        watch("Alpha")

        assertEquals(1, updateCheck.perform(manualSync))

        assertTrue(notifications.posted.isEmpty())
        assertTrue(SyncNotification.SYNC_NOTIFICATION_ID in notifications.cancelled)
        assertEquals(1, lastSchedule().found)
        assertEquals(0, lastSchedule().notified)
    }

    private val announcements: List<Pair<Int, String?>>
        get() = notifications.posted.map { (id, notification) ->
            id to notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        }

    private suspend fun scheduledSync(): Int {
        preferences.lastUpdateTime = -1L
        return updateCheck.perform(Data.EMPTY)
    }

    private suspend fun lastSchedule() = database.schedules().load().first().maxBy { it.id }

    /** Adds a watched app cached at [cached] whose Play Store listing is at [remote]. */
    private suspend fun watch(
        name: String,
        cached: Int = 1,
        remote: Int = 2,
        installed: Int? = null,
        cachedChanges: String? = null,
        remoteChanges: String = "New features"
    ) {
        val packageName = "com.example.${name.lowercase()}"
        val app = App.fromLocalPackage(0, packageName, 0, cached, cached.toString(), name, null)
            .copy(status = App.STATUS_NORMAL)
        AppListTable.Queries.insert(app, database)
        if (cachedChanges != null) {
            val changelog = AppChange(packageName, cached, cached.toString(), cachedChanges, "Jan 1, 2026", false)
            database.openHelper.writableDatabase.insert(ChangelogTable.TABLE, SQLiteDatabase.CONFLICT_REPLACE, changelog.contentValues)
        }
        if (installed != null) {
            install(packageName, installed)
        }
        remoteApps[packageName] = RemoteApp(name, remote, remoteChanges)
    }

    @Suppress("DEPRECATION")
    private fun install(packageName: String, versionCode: Int) {
        shadowOf(harness.context.packageManager).installPackage(
            PackageInfo().apply {
                this.packageName = packageName
                this.versionCode = versionCode
                applicationInfo = ApplicationInfo().apply {
                    this.packageName = packageName
                    enabled = true
                }
            }
        )
    }

    /** The update check (`?au=1`) answers with the version only, the plain request with the full listing. */
    private class RemoteApp(val title: String, val versionCode: Int, val recentChanges: String) {
        fun toDocument(packageName: String, full: Boolean): DocV2 {
            val appDetails = AppDetails.newBuilder()
                .setPackageName(packageName)
                .setVersionCode(versionCode)
            val document = DocV2.newBuilder().setDocid(packageName)
            if (full) {
                appDetails
                    .setVersionString(versionCode.toString())
                    .setUploadDate("Jan 2, 2026")
                    .setRecentChangesHtml(recentChanges)
                document.setTitle(title)
            }
            return document.setDetails(DocDetails.newBuilder().setAppDetails(appDetails)).build()
        }
    }
}