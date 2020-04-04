package com.anod.appwatcher.preferences

import android.annotation.SuppressLint
import android.app.Activity
import android.app.UiModeManager.*
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.anod.appwatcher.AppWatcherActivity
import com.anod.appwatcher.Application
import com.anod.appwatcher.BuildConfig
import com.anod.appwatcher.R
import com.anod.appwatcher.backup.DbBackupManager
import com.anod.appwatcher.backup.ImportTask
import com.anod.appwatcher.backup.gdrive.GDrive
import com.anod.appwatcher.backup.gdrive.GDriveSignIn
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.preferences.Preferences.Companion.THEME_BLACK
import com.anod.appwatcher.preferences.Preferences.Companion.THEME_DEFAULT
import com.anod.appwatcher.sync.SchedulesHistoryActivity
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.userLog.UserLogActivity
import com.anod.appwatcher.utils.Theme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.jakewharton.processphoenix.ProcessPhoenix
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.DialogItems
import info.anodsplace.framework.app.DialogSingleChoice
import info.anodsplace.framework.app.SettingsActionBarActivity
import info.anodsplace.framework.content.startActivityForResultSafely
import info.anodsplace.framework.playservices.GooglePlayServices
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

@SuppressLint("Registered")
open class SettingsActivity : SettingsActionBarActivity(), GDrive.Listener, GDriveSignIn.Listener {

    override val themeRes: Int
        get() = Theme(this).theme
    override val themeColors: CustomThemeColors
        get() = Theme(this).colors

    private val syncEnabledItem: SwitchItem by lazy { SwitchItem(R.string.pref_title_drive_sync_enabled, R.string.pref_descr_drive_sync_enabled, ACTION_SYNC_ENABLE) }
    private val syncNowItem: TextItem by lazy { TextItem(R.string.pref_title_drive_sync_now, 0, ACTION_SYNC_NOW) }
    private val wifiItem: SwitchItem by lazy { SwitchItem(R.string.menu_wifi_only, 0, ACTION_WIFI_ONLY, prefs.isWifiOnly) }
    private val chargingItem: SwitchItem by lazy { SwitchItem(R.string.menu_requires_charging, 0, ACTION_REQUIRES_CHARGING, prefs.isRequiresCharging) }
    private val frequencyItem: TextItem by lazy { TextItem(R.string.pref_title_updates_frequency, 0, ACTION_UPDATE_FREQUENCY) }

    private val gDriveSignIn: GDriveSignIn by lazy { GDriveSignIn(this, this) }

    private var recreateWatchlistOnBack: Boolean = false
    private val viewModel: SettingsViewModel by viewModels()

    private val prefs: Preferences
        get() = Application.provide(this).prefs

    override fun onBackPressed() {
        super.onBackPressed()
        if (this.recreateWatchlistOnBack) {
            this.recreateWatchlist()
        }
    }

    override fun onResume() {
        super.onResume()
        syncNowItem.summary = renderDriveSyncTime()
    }

    override fun createPreferenceItems(): List<Preference> {

        val useAutoSync = prefs.useAutoSync
        val currentIndex = resources.getIntArray(R.array.updates_frequency_values).indexOf(prefs.updatesFrequency)
        val frequencyTitles = resources.getStringArray(R.array.updates_frequency)
        frequencyItem.summary = if (currentIndex == -1) "Every ${prefs.updatesFrequency} minutes" else frequencyTitles[currentIndex]

        wifiItem.enabled = useAutoSync
        chargingItem.enabled = useAutoSync

        val gps = GooglePlayServices(this)
        if (!gps.isSupported) {
            syncEnabledItem.checked = false
            syncEnabledItem.enabled = false
            syncNowItem.enabled = false
            syncEnabledItem.summaryRes = 0
            syncEnabledItem.summary = gps.availabilityMessage
        } else {
            syncEnabledItem.checked = prefs.isDriveSyncEnabled
            syncNowItem.enabled = syncEnabledItem.checked
            syncNowItem.summary = renderDriveSyncTime()
        }

        val aboutItem = TextItem(R.string.pref_title_about, 0, ACTION_ABOUT)
        aboutItem.summary = appVersion

        val preferences = mutableListOf(
                Category(R.string.category_updates),
                frequencyItem,
                wifiItem,
                chargingItem,

                Category(R.string.settings_notifications),
                SwitchItem(R.string.uptodate_title, R.string.uptodate_summary, ACTION_NOTIFY_UPTODATE, prefs.isNotifyInstalledUpToDate),
                SwitchItem(R.string.pref_notify_installed, R.string.pref_notify_installed_summary, ACTION_NOTIFY_INSTALLED, prefs.isNotifyInstalled),
                SwitchItem(R.string.pref_notify_no_changes, R.string.pref_notify_no_changes_summary, ACTION_NOTIFY_NO_CHANGES, prefs.isNotifyNoChanges),

                Category(R.string.pref_header_drive_sync),
                syncEnabledItem,
                syncNowItem,

                Category(R.string.pref_header_backup),
                TextItem(R.string.pref_title_export, R.string.pref_descr_export, ACTION_EXPORT),
                TextItem(R.string.pref_title_import, R.string.pref_descr_import, ACTION_IMPORT),

                Category(R.string.pref_header_interface),
                TextItem(R.string.pref_title_theme, R.string.pref_descr_theme, ACTION_THEME),
                SwitchItem(R.string.pref_show_recent_title, R.string.pref_show_recent_descr, ACTION_SHOW_RECENT, prefs.showRecent),
                SwitchItem(R.string.pref_show_ondevice_title, R.string.pref_show_ondevice_descr, ACTION_SHOW_ONDEVICE, prefs.showOnDevice),
                SwitchItem(R.string.pref_show_recently_updated_title, R.string.pref_show_recently_updated_descr, ACTION_SHOW_RECENTLY_UPDATED, prefs.showRecentlyUpdated),
                TextItem(R.string.pref_default_filter, R.string.pref_default_filter_summary, ACTION_DEFAULT_FILTER),
                SwitchItem(R.string.pref_pull_to_refresh, 0, ACTION_ENABLE_PULL_TO_REFRESH, prefs.enablePullToRefresh),
                TextItem(R.string.adaptive_icon_style, R.string.adaptive_icon_style_summary, ACTION_ICON_STYLE),

                Category(R.string.pref_privacy),
                SwitchItem(R.string.crash_reports_title, R.string.crash_reports_descr, ACTION_CRASH_REPORTS, prefs.collectCrashReports),

                Category(R.string.pref_header_about),
                aboutItem,
                TextItem(R.string.pref_title_opensource, R.string.pref_descr_opensource, ACTION_LICENSES),
                TextItem(R.string.user_log, 0, ACTION_USER_LOG),
                TextItem(R.string.refresh_history, 0, ACTION_USER_CHECK_HISTORY)
        )

        if (BuildConfig.DEBUG) {
            preferences.add(TextItem(R.string.pref_export_db, 0, ACTION_EXPORT_DB))
        }
        return preferences
    }

    private fun renderDriveSyncTime(): String {
        val time = prefs.lastDriveSyncTime
        return if (time == (-1).toLong()) {
            getString(R.string.pref_descr_drive_sync_now, getString(R.string.never))
        } else {
            getString(R.string.pref_descr_drive_sync_now,
                    DateUtils.getRelativeDateTimeString(this, time, 0, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_BACKUP_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = data!!.data ?: return
                viewModel.import(uri).observe(this) {
                    when (it) {
                        -1 -> {
                            AppLog.d("Importing...")
                            isProgressVisible = true
                        }
                        else -> {
                            AppLog.d("Import finished with code: $it")
                            isProgressVisible = false
                            ImportTask.showImportFinishToast(this, it)
                        }
                    }
                }

            }
        } else if (requestCode == REQUEST_BACKUP_DEST) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = data!!.data ?: return
                viewModel.export(uri).observe(this) {
                    when (it) {
                        -1 -> {
                            AppLog.d("Exporting...")
                            isProgressVisible = true
                        }
                        else -> {
                            AppLog.d("Export finished with code: $it")
                            isProgressVisible = false
                            when (it) {
                                DbBackupManager.RESULT_OK -> Toast.makeText(this, resources.getString(R.string.export_done), Toast.LENGTH_SHORT).show()
                                DbBackupManager.ERROR_STORAGE_NOT_AVAILABLE -> Toast.makeText(this, resources.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show()
                                DbBackupManager.ERROR_FILE_WRITE -> Toast.makeText(this, resources.getString(R.string.failed_to_write_file), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        } else {
            gDriveSignIn.onActivityResult(requestCode, resultCode, data)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPreferenceItemClick(action: Int, pref: Item) {
        when (action) {
            ACTION_EXPORT -> {
                try {
                    startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        setDataAndType(Uri.parse(DbBackupManager.defaultBackupDir.absolutePath), "application/json")
                        putExtra(Intent.EXTRA_TITLE, "appwatcher-" + DbBackupManager.generateFileName())
                    }, REQUEST_BACKUP_DEST)
                } catch (e: Exception) {
                    AppLog.e(e)
                    Toast.makeText(this, "Cannot start activity: $intent", Toast.LENGTH_SHORT).show()
                }
            }
            ACTION_IMPORT -> startActivityForResultSafely(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/plain", "*/*"))
            }, REQUEST_BACKUP_FILE)
            ACTION_LICENSES -> startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            ACTION_SYNC_ENABLE -> {
                syncNowItem.enabled = false // disable temporary sync now
                if (syncEnabledItem.checked) {
                    isProgressVisible = true
                    gDriveSignIn.signIn()
                }
            }
            ACTION_SYNC_NOW -> if (syncNowItem.enabled) {
                syncNowItem.enabled = false
                val googleAccount = GoogleSignIn.getLastSignedInAccount(this)
                if (googleAccount != null) {
                    GDrive(this, googleAccount, this).sync()
                } else {
                    onGDriveLoginError(0)
                }
            }
            ACTION_UPDATE_FREQUENCY -> {
                val values = resources.getIntArray(R.array.updates_frequency_values)
                DialogSingleChoice(this, R.style.AlertDialog,
                        R.string.pref_title_updates_frequency,
                        R.array.updates_frequency,
                        values.indexOf(prefs.updatesFrequency)) { dialog, which ->
                    prefs.updatesFrequency = values[which]
                    val useAutoSync = prefs.useAutoSync
                    if (useAutoSync) {
                        SyncScheduler(this)
                                .schedule(prefs.isRequiresCharging, prefs.isWifiOnly, prefs.updatesFrequency.toLong(), true)
                                .observe(this, Observer { })
                    } else {
                        SyncScheduler(this)
                                .cancel()
                                .observe(this, Observer { })
                    }
                    frequencyItem.summary = resources.getStringArray(R.array.updates_frequency)[which]
                    wifiItem.enabled = useAutoSync
                    chargingItem.enabled = useAutoSync
                    dialog.dismiss()
                    notifyDataSetChanged()
                }.show()
            }
            ACTION_WIFI_ONLY -> {
                val useWifiOnly = (pref as ToggleItem).checked
                prefs.isWifiOnly = useWifiOnly
                SyncScheduler(this)
                        .schedule(prefs.isRequiresCharging, useWifiOnly, prefs.updatesFrequency.toLong(), true)
                        .observe(this, Observer { })
            }
            ACTION_REQUIRES_CHARGING -> {
                val requiresCharging = (pref as ToggleItem).checked
                prefs.isRequiresCharging = requiresCharging
                SyncScheduler(this)
                        .schedule(requiresCharging, prefs.isWifiOnly, prefs.updatesFrequency.toLong(), true)
                        .observe(this, Observer { })
            }
            ACTION_NOTIFY_UPTODATE -> prefs.isNotifyInstalledUpToDate = (pref as ToggleItem).checked
            ACTION_THEME -> {
                val nightModes = arrayOf(
                        MODE_NIGHT_AUTO, MODE_NIGHT_AUTO, MODE_NIGHT_NO, MODE_NIGHT_YES, MODE_NIGHT_YES
                )
                val themes = arrayOf(
                        THEME_DEFAULT, THEME_BLACK, THEME_DEFAULT, THEME_DEFAULT, THEME_BLACK
                )
                DialogItems(this, R.style.AlertDialog, R.string.pref_title_theme, R.array.themes) { _, which ->
                    var recreate = false
                    if (prefs.theme != themes[which]) {
                        prefs.theme = themes[which]
                        recreate = true
                    }
                    if (prefs.nightMode != nightModes[which]) {
                        prefs.nightMode = nightModes[which]
                        recreate = true
                    }
                    if (recreate) {
                        AppCompatDelegate.setDefaultNightMode(nightModes[which])
                        this@SettingsActivity.setResult(Activity.RESULT_OK, Intent().putExtra("recreateWatchlistOnBack", true))
                        this@SettingsActivity.recreate()
                        this.recreateWatchlist()
                    }
                }.show()
            }
            ACTION_EXPORT_DB -> try {
                exportDb()
            } catch (e: IOException) {
                AppLog.e(e)
            }
            ACTION_SHOW_RECENT -> prefs.showRecent = this.applyToggle(pref, prefs.showRecent)
            ACTION_SHOW_ONDEVICE -> prefs.showOnDevice = this.applyToggle(pref, prefs.showOnDevice)
            ACTION_SHOW_RECENTLY_UPDATED -> prefs.showRecentlyUpdated = this.applyToggle(pref, prefs.showRecentlyUpdated)
            ACTION_USER_LOG -> {
                startActivity(Intent(this, UserLogActivity::class.java))
            }
            ACTION_USER_CHECK_HISTORY -> {
                startActivity(Intent(this, SchedulesHistoryActivity::class.java))
            }
            ACTION_DEFAULT_FILTER -> {
                DialogSingleChoice(this, R.style.AlertDialog, R.string.pref_default_filter, R.array.filter_titles, prefs.defaultMainFilterId) { _, which ->
                    prefs.defaultMainFilterId = which
                }.show()
            }
            ACTION_ENABLE_PULL_TO_REFRESH -> prefs.enablePullToRefresh = this.applyToggle(pref, prefs.enablePullToRefresh)
            ACTION_NOTIFY_INSTALLED -> prefs.isNotifyInstalled = (pref as ToggleItem).checked
            ACTION_NOTIFY_NO_CHANGES -> prefs.isNotifyNoChanges = (pref as ToggleItem).checked
            ACTION_CRASH_REPORTS -> {
                prefs.collectCrashReports = (pref as ToggleItem).checked
                ProcessPhoenix.triggerRebirth(this, Intent(this, AppWatcherActivity::class.java))
            }
            ACTION_ICON_STYLE -> {
                val values = resources.getStringArray(R.array.adaptive_icon_style_paths_values)
                DialogSingleChoice(this, R.style.AlertDialog,
                        R.string.adaptive_icon_style,
                        R.array.adaptive_icon_style_names,
                        values.indexOf(prefs.iconShape)) { dialog, which ->
                    prefs.iconShape = values[which]
                    Application.provide(this).iconLoader.setIconShape(values[which] ?: "")
                    this.recreateWatchlist()
                    dialog.dismiss()
                }.show()
            }
        }
        notifyDataSetChanged()
    }

    private fun applyToggle(pref: Item, oldValue: Boolean): Boolean {
        val newValue = (pref as ToggleItem).checked
        if (!this.recreateWatchlistOnBack) {
            this.recreateWatchlistOnBack = oldValue != newValue
        }
        return newValue
    }

    private fun recreateWatchlist() {
        val i = Intent(this@SettingsActivity, AppWatcherActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
    }

    @Throws(IOException::class)
    private fun exportDb() {
        val sd = Environment.getExternalStorageDirectory()
        val dbPath = filesDir.absolutePath.replace("files", "databases") + File.separator
        val currentDBPath = AppsDatabase.dbName
        val backupDBPath = "appwatcher.db"
        val currentDB = File(dbPath, currentDBPath)
        val backupDB = File(sd, backupDBPath)

        if (currentDB.exists()) {
            val src = FileInputStream(currentDB).channel
            val dst = FileOutputStream(backupDB).channel
            dst.transferFrom(src, 0, src.size())
            src.close()
            dst.close()
        }
    }

    private val appVersion: String
        get() = String.format(Locale.US, "%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

    override fun onGDriveLoginSuccess(googleSignInAccount: GoogleSignInAccount) {
        syncEnabledItem.checked = true
        syncEnabledItem.enabled = true
        syncNowItem.enabled = true
        prefs.isDriveSyncEnabled = true
        Application.provide(this).uploadServiceContentObserver
        notifyDataSetChanged()
        isProgressVisible = false

        Toast.makeText(this, R.string.gdrive_connected, Toast.LENGTH_SHORT).show()
    }

    override fun onGDriveLoginError(errorCode: Int) {
        isProgressVisible = false
        syncNowItem.enabled = syncEnabledItem.checked
        notifyDataSetChanged()
        Toast.makeText(this, "Drive login error $errorCode", Toast.LENGTH_SHORT).show()
    }

    override fun onGDriveSyncProgress() {

    }

    override fun onGDriveSyncStart() {
        isProgressVisible = true
        Toast.makeText(this, R.string.sync_start, Toast.LENGTH_SHORT).show()
    }

    override fun onGDriveSyncFinish() {
        isProgressVisible = false
        prefs.lastDriveSyncTime = System.currentTimeMillis()
        syncNowItem.summary = getString(R.string.pref_descr_drive_sync_now, getString(R.string.now))
        syncNowItem.enabled = syncEnabledItem.checked
        notifyDataSetChanged()
        Toast.makeText(this, R.string.sync_finish, Toast.LENGTH_SHORT).show()
    }

    override fun onGDriveError() {
        isProgressVisible = false
        syncNowItem.enabled = syncEnabledItem.checked
        notifyDataSetChanged()
        Toast.makeText(this, R.string.sync_error, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_BACKUP_DEST = 1
        private const val REQUEST_BACKUP_FILE = 2

        private const val ACTION_EXPORT = 3
        private const val ACTION_IMPORT = 4
        private const val ACTION_LICENSES = 6
        private const val ACTION_ABOUT = 5
        private const val ACTION_SYNC_ENABLE = 1
        private const val ACTION_SYNC_NOW = 2
        private const val ACTION_UPDATE_FREQUENCY = 7
        private const val ACTION_WIFI_ONLY = 8
        private const val ACTION_REQUIRES_CHARGING = 9
        private const val ACTION_NOTIFY_UPTODATE = 10
        private const val ACTION_THEME = 11
        private const val ACTION_EXPORT_DB = 12
        private const val ACTION_SHOW_RECENT = 15
        private const val ACTION_SHOW_ONDEVICE = 16
        private const val ACTION_USER_LOG = 17
        private const val ACTION_SHOW_RECENTLY_UPDATED = 18
        private const val ACTION_DEFAULT_FILTER = 19
        private const val ACTION_NOTIFY_INSTALLED = 20
        private const val ACTION_ENABLE_PULL_TO_REFRESH = 21
        private const val ACTION_CRASH_REPORTS = 22
        private const val ACTION_ICON_STYLE = 23
        private const val ACTION_NOTIFY_NO_CHANGES = 24
        private const val ACTION_USER_CHECK_HISTORY = 25
    }
}