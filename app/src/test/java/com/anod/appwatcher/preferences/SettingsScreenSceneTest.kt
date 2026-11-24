package com.anod.appwatcher.preferences

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.navigation3.runtime.NavKey
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.anod.appwatcher.R
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.diagnostics.DiagnosticAllowlist
import com.anod.appwatcher.diagnostics.DiagnosticRecorder
import com.anod.appwatcher.navigation.SceneNavKey
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.utils.POST_NOTIFICATIONS_PERMISSION
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowToast

/** Hosts [SettingsScreenScene] with the real [SettingsViewModel] and checks what the screen asks of the app around it. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35], qualifiers = "w411dp-h891dp")
@LooperMode(LooperMode.Mode.PAUSED)
class SettingsScreenSceneTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val recorderJob = SupervisorJob()
    private val notifications = TestNotificationManager()
    private val preferences = Preferences(app, notifications, appScope, DiagnosticAllowlist(emptySet()))
    private val navigatedTo = mutableListOf<NavKey>()
    private var navigatedBack = 0
    private val backupUri = Uri.parse("content://com.anod.appwatcher.test/backup.json")
    private lateinit var database: AppsDatabase

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
        database = Room.inMemoryDatabaseBuilder(app, AppsDatabase::class.java).allowMainThreadQueries().build()
        val recorder = DiagnosticRecorder(app, preferences, CoroutineScope(recorderJob + Dispatchers.IO), workInfos = { emptyList() })
        startKoin { modules(settingsTestModule(app, appScope, notifications, preferences, recorder, database)) }
    }

    @After
    fun tearDown() {
        stopKoin()
        database.close()
        runBlocking { recorderJob.cancelAndJoin() }
        appScope.cancel()
    }

    private fun show() {
        Shadows.shadowOf(app).clearNextStartedActivities()
        compose.setContent {
            SettingsScreenScene(
                prefs = preferences,
                navigateBack = { navigatedBack++ },
                navigateTo = { navigatedTo += it }
            )
        }
        compose.waitForIdle()
    }

    private fun awaitUntil(description: String, condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
        while (!condition()) {
            if (System.nanoTime() > deadline) fail("Timed out waiting for $description")
            compose.waitForIdle()
            Thread.sleep(10)
        }
    }

    private fun awaitStartedActivity(description: String): Intent {
        awaitUntil(description) { Shadows.shadowOf(app).peekNextStartedActivity() != null }
        return Shadows.shadowOf(app).nextStartedActivity
    }

    /** Answers the document picker the screen opened, the way the system does once the user has chosen [document]. */
    private fun chooseDocument(document: Uri) {
        val activity = Shadows.shadowOf(compose.activity)
        val picker = activity.peekNextStartedActivityForResult()
        assertNotNull("No document picker was opened", picker)
        activity.receiveResult(picker.intent, Activity.RESULT_OK, Intent().setData(document))
        compose.waitForIdle()
    }

    private fun awaitToast(text: String) = awaitUntil("the \"$text\" message") { ShadowToast.getTextOfLatestToast() == text }

    private fun scrollTo(title: String) {
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(title))
    }

    private fun tapRow(title: String) {
        scrollTo(title)
        compose.onNode(hasText(title)).performClick()
    }

    private fun chip(label: String) = compose.onNode(
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox) and hasText(label)
    )

    /** The background fills most of the list, so its most common pixel is the colour it is painted in. */
    private fun paintedBackground(): Int {
        val image = compose.onNode(hasScrollAction()).captureToImage()
        val pixels = IntArray(image.width * image.height)
        image.readPixels(pixels)
        return pixels.toList().groupingBy { it }.eachCount().maxBy { it.value }.key
    }

    private fun scheduledSync(): List<WorkInfo> = WorkManager.getInstance(app).getWorkInfosForUniqueWork(SyncScheduler.TAG).get()

    private fun frequencyLabels() = app.resources.getStringArray(R.array.updates_frequency)

    private fun notificationsBannerShown() =
        compose.onAllNodesWithText(app.getString(R.string.notifications_not_enabled)).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun backArrowLeavesTheScreen() {
        show()

        compose.onNodeWithContentDescription(app.getString(R.string.back)).performClick()

        awaitUntil("the screen to navigate back") { navigatedBack > 0 }
        assertEquals(1, navigatedBack)
        assertEquals(emptyList<NavKey>(), navigatedTo)
    }

    @Test
    fun refreshHistoryAndUserLogRowsOpenTheirScenes() {
        show()

        tapRow(app.getString(R.string.refresh_history))
        awaitUntil("the refresh history scene to open") { navigatedTo.size == 1 }
        tapRow(app.getString(R.string.user_log))
        awaitUntil("the user log scene to open") { navigatedTo.size == 2 }

        assertEquals(listOf<NavKey>(SceneNavKey.RefreshHistory, SceneNavKey.UserLog), navigatedTo)
        assertEquals(0, navigatedBack)
    }

    @Test
    fun openSourceLicensesRowStartsTheLicensesScreen() {
        show()

        tapRow(app.getString(R.string.pref_title_opensource))

        val started = awaitStartedActivity("the licenses screen to start")
        assertEquals(OssLicensesMenuActivity::class.java.name, started.component?.className)
        assertEquals(emptyList<NavKey>(), navigatedTo)
    }

    @Test
    fun exportingTheWatchListWritesTheChosenDocumentAndConfirmsIt() {
        runBlocking { database.insertWatchedApp("exported") }
        val output = ByteArrayOutputStream()
        Shadows.shadowOf(app.contentResolver).registerOutputStream(backupUri, output)
        show()

        tapRow(app.getString(R.string.pref_title_export))
        chooseDocument(backupUri)

        awaitToast(app.getString(R.string.export_done))
        assertTrue(output.toString().contains("exported.package"))
    }

    @Test
    fun importingABackupRestoresTheWatchListAndConfirmsIt() {
        val backup = runBlocking {
            database.insertWatchedApp("restored")
            StringWriter().also { DbJsonWriter().write(it, database) }.toString()
        }
        runBlocking { database.apps().delete() }
        Shadows.shadowOf(app.contentResolver).registerInputStream(backupUri, ByteArrayInputStream(backup.toByteArray()))
        show()

        tapRow(app.getString(R.string.pref_title_import))
        chooseDocument(backupUri)

        awaitToast(app.getString(R.string.import_done))
        assertNotNull(runBlocking { database.apps().loadApp("restored") })
    }

    @Test
    fun importingAFileThatIsNotABackupExplainsWhyAndKeepsTheWatchList() {
        runBlocking { database.insertWatchedApp("kept") }
        Shadows.shadowOf(app.contentResolver).registerInputStream(backupUri, ByteArrayInputStream("this is not a backup".toByteArray()))
        show()

        tapRow(app.getString(R.string.pref_title_import))
        chooseDocument(backupUri)

        awaitToast(app.getString(R.string.restore_deserialize_failed))
        assertNotNull(runBlocking { database.apps().loadApp("kept") })
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun pickingAThemeRepaintsTheOpenScreenWithoutRestartingTheApp() {
        val themes = app.resources.getStringArray(R.array.themes)
        val light = dynamicLightColorScheme(app).background.toArgb()
        val dark = dynamicDarkColorScheme(app).background.toArgb()
        preferences.themeIndex = LIGHT
        show()
        scrollTo(app.getString(R.string.pref_title_theme))
        assertEquals(light, paintedBackground())

        listOf(DARK to dark, BLACK to Color.Black.toArgb(), LIGHT to light, LIGHT to light).forEach { (index, background) ->
            chip(themes[index]).performClick()
            compose.waitForIdle()

            assertEquals(index, preferences.themeIndex)
            chip(themes[index]).assertIsSelected()
            assertEquals("background for ${themes[index]}", background, paintedBackground())
            assertNull("${themes[index]} started an activity", Shadows.shadowOf(app).peekNextStartedActivity())
            assertFalse("${themes[index]} closed the screen", compose.activity.isFinishing)
        }
    }

    @Test
    fun pickingAnUpdateFrequencySchedulesThePeriodicCheckAndManualCancelsIt() {
        val labels = frequencyLabels()
        show()
        scrollTo(app.getString(R.string.pref_title_updates_frequency))

        chip(labels[3]).performClick()

        awaitUntil("the periodic check to be scheduled") { scheduledSync().isNotEmpty() }
        assertEquals(10800, preferences.updatesFrequency)
        assertEquals(10800_000L, scheduledSync().single().periodicityInfo?.repeatIntervalMillis)

        chip(labels[0]).performClick()

        awaitUntil("the periodic check to be cancelled") { scheduledSync().single().state == WorkInfo.State.CANCELLED }
        assertEquals(0, preferences.updatesFrequency)
    }

    @Test
    fun allowingNotificationsFromTheBannerAsksForThePermissionAndUnlocksTheUpdateFrequency() {
        val labels = frequencyLabels()
        notifications.areNotificationsEnabled = false
        show()
        scrollTo(app.getString(R.string.pref_title_updates_frequency))
        compose.onNodeWithText(app.getString(R.string.notifications_not_enabled)).assertIsDisplayed()
        labels.forEach { chip(it).assertIsNotEnabled() }

        compose.onNodeWithText(app.getString(R.string.allow)).performClick()

        val request = awaitPermissionRequest()
        assertEquals(listOf(POST_NOTIFICATIONS_PERMISSION), request.requestedPermissions.toList())
        notifications.areNotificationsEnabled = true
        grantPermissionRequest(request)

        awaitUntil("the banner to disappear") { !notificationsBannerShown() }
        labels.forEach { chip(it).assertIsEnabled() }
        assertNull(Shadows.shadowOf(app).peekNextStartedActivity())
    }

    @Test
    fun denyingNotificationsOpensTheAppSettingsAndKeepsTheUpdateFrequencyLocked() {
        val labels = frequencyLabels()
        notifications.areNotificationsEnabled = false
        show()
        scrollTo(app.getString(R.string.pref_title_updates_frequency))
        compose.onNodeWithText(app.getString(R.string.allow)).performClick()
        val request = awaitPermissionRequest()

        denyPermissionRequest(request)

        val settings = awaitStartedActivity("the app settings to open")
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, settings.action)
        assertEquals(Uri.fromParts("package", app.packageName, null), settings.data)
        assertEquals(true, notificationsBannerShown())
        labels.forEach { chip(it).assertIsNotEnabled() }
    }

    /** Waits for the system permission dialog; Robolectric also records it as a started activity, which is consumed here. */
    private fun awaitPermissionRequest(): ShadowActivity.PermissionsRequest {
        val activity = Shadows.shadowOf(compose.activity)
        awaitUntil("the notification permission request") { activity.lastRequestedPermission != null }
        assertEquals(SYSTEM_PERMISSION_DIALOG, awaitStartedActivity("the system permission dialog").action)
        return activity.lastRequestedPermission
    }

    private fun grantPermissionRequest(request: ShadowActivity.PermissionsRequest) = answerPermissionRequest(request, PackageManager.PERMISSION_GRANTED)

    private fun denyPermissionRequest(request: ShadowActivity.PermissionsRequest) = answerPermissionRequest(request, PackageManager.PERMISSION_DENIED)

    @Suppress("DEPRECATION")
    private fun answerPermissionRequest(request: ShadowActivity.PermissionsRequest, result: Int) {
        compose.activity.onRequestPermissionsResult(request.requestCode, request.requestedPermissions, IntArray(request.requestedPermissions.size) { result })
        compose.waitForIdle()
    }

    private companion object {
        const val SYSTEM_PERMISSION_DIALOG = "android.content.pm.action.REQUEST_PERMISSIONS"
        const val LIGHT = 2
        const val DARK = 3
        const val BLACK = 4
    }
}