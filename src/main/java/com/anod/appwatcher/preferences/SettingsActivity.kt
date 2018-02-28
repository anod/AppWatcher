package com.anod.appwatcher.preferences

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDelegate
import android.text.format.DateUtils
import android.widget.Toast
import com.anod.appwatcher.*
import com.anod.appwatcher.backup.DbBackupManager
import com.anod.appwatcher.backup.ExportTask
import com.anod.appwatcher.backup.gdrive.GDrive
import com.anod.appwatcher.backup.ImportTask
import com.anod.appwatcher.backup.gdrive.GDriveSignIn
import com.anod.appwatcher.model.DbSchemaManager
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.userLog.UserLogActivity
import com.anod.appwatcher.utils.Theme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.playservices.GooglePlayServices
import info.anodsplace.framework.app.SettingsActionBarActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

open class SettingsActivity : SettingsActionBarActivity(), ExportTask.Listener, GDrive.Listener, GDriveSignIn.Listener, ImportTask.Listener {

    override val themeRes: Int
        get() =  Theme(this).theme

    private val syncEnabledItem: SwitchItem by lazy { SwitchItem(R.string.pref_title_drive_sync_enabled, R.string.pref_descr_drive_sync_enabled, ACTION_SYNC_ENABLE) }
    private val syncNowItem: TextItem by lazy { TextItem(R.string.pref_title_drive_sync_now, 0, ACTION_SYNC_NOW) }
    private val wifiItem: SwitchItem by lazy { SwitchItem(R.string.menu_wifi_only, 0, ACTION_WIFI_ONLY, prefs.isWifiOnly) }
    private val chargingItem: SwitchItem by lazy { SwitchItem(R.string.menu_requires_charging, 0, ACTION_REQUIRES_CHARGING, prefs.isRequiresCharging) }
    private val frequencyItem: TextItem by lazy { TextItem(R.string.pref_title_updates_frequency, 0, ACTION_UPDATE_FREQUENCY) }

    private val gDriveSignIn: GDriveSignIn by lazy { GDriveSignIn(this, this) }

    private var recreateWatchlistOnBack: Boolean = false

    private val prefs: Preferences
        get() = App.provide(this).prefs

    override fun onBackPressed() {
        super.onBackPressed()
        if (this.recreateWatchlistOnBack) {
            this.recreateWatchlist()
        }
    }

    override fun onExportStart() {
        AppLog.d("Exporting...")
        isProgressVisible = true
    }

    override fun onExportFinish(code: Int) {
        AppLog.d("Code: " + code)
        isProgressVisible = false
        val r = resources
        if (code == DbBackupManager.RESULT_OK) {
            Toast.makeText(this, r.getString(R.string.export_done), Toast.LENGTH_SHORT).show()
            return
        }
        when (code) {
            DbBackupManager.ERROR_STORAGE_NOT_AVAILABLE -> Toast.makeText(this, r.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show()
            DbBackupManager.ERROR_FILE_WRITE -> Toast.makeText(this, r.getString(R.string.failed_to_write_file), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        syncNowItem!!.summary = renderDriveSyncTime()
    }

    override fun createPreferenceItems(): List<Preference> {
        val preferences = mutableListOf<Preference>()

        preferences.add(Category(R.string.category_updates))

        val useAutoSync = prefs.useAutoSync
        val currentIndex = resources.getIntArray(R.array.updates_frequency_values).indexOf(prefs.updatesFrequency)
        val frequencyTitles = resources.getStringArray(R.array.updates_frequency)
        frequencyItem.summary = if (currentIndex == -1) "Every ${prefs.updatesFrequency} minutes" else frequencyTitles[currentIndex]
        preferences.add(frequencyItem)

        preferences.add(wifiItem)
        wifiItem.enabled = useAutoSync

        preferences.add(chargingItem)
        chargingItem.enabled = useAutoSync

        preferences.add(Category(R.string.settings_notifications))
        preferences.add(SwitchItem(R.string.uptodate_title, R.string.uptodate_summary, ACTION_NOTIFY_UPTODATE, prefs.isNotifyInstalledUpToDate))

        preferences.add(Category(R.string.pref_header_drive_sync))

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
        preferences.add(syncEnabledItem)
        preferences.add(syncNowItem)
        if (BuildConfig.DEBUG) {
            preferences.add(TextItem(R.string.pref_gdrive_upload, 0, ACTION_GDRIVE_UPLOAD))
        }

        preferences.add(Category(R.string.pref_header_backup))
        preferences.add(TextItem(R.string.pref_title_export, R.string.pref_descr_export, ACTION_EXPORT))
        preferences.add(TextItem(R.string.pref_title_import, R.string.pref_descr_import, ACTION_IMPORT))

        preferences.add(Category(R.string.pref_header_interface))
        preferences.add(TextItem(R.string.pref_title_theme, R.string.pref_descr_theme, ACTION_THEME))
        preferences.add(TextItem(R.string.pref_title_dark_theme, R.string.pref_descr_dark_theme, ACTION_DARK_THEME))
        preferences.add(SwitchItem(R.string.pref_show_recent_title, R.string.pref_show_recent_descr, ACTION_SHOW_RECENT, prefs.showRecent))
        preferences.add(SwitchItem(R.string.pref_show_ondevice_title, R.string.pref_show_ondevice_descr, ACTION_SHOW_ONDEVICE, prefs.showOnDevice))

        preferences.add(Category(R.string.pref_header_about))

        val aboutItem = TextItem(R.string.pref_title_about, 0, ACTION_ABOUT)
        aboutItem.summary = appVersion
        preferences.add(aboutItem)
        preferences.add(TextItem(R.string.pref_title_opensource, R.string.pref_descr_opensource, ACTION_LICENSES))

        preferences.add(TextItem(R.string.user_log, 0, ACTION_USER_LOG))

        if (BuildConfig.DEBUG) {
            preferences.add(TextItem(R.string.pref_export_db, 0, ACTION_EXPORT_DB))
        }
        return preferences
    }

    private fun renderDriveSyncTime(): String {
        val time = prefs.lastDriveSyncTime
        if (time == (-1).toLong()) {
            return getString(R.string.pref_descr_drive_sync_now, getString(R.string.never))
        } else {
            return getString(R.string.pref_descr_drive_sync_now,
                    DateUtils.getRelativeDateTimeString(this, time, 0, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_BACKUP_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                ImportTask(this, this).execute(data!!.data)
            }
        } else if (requestCode == REQUEST_BACKUP_DEST) {
            if (resultCode == Activity.RESULT_OK) {
                ExportTask(this, this).execute(data!!.data)
            }
        } else {
            gDriveSignIn.onActivityResult(requestCode, resultCode, data)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPreferenceItemClick(action: Int, pref: Item) {
        when (action) {
            ACTION_EXPORT -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                val uri = Uri.parse(DbBackupManager.defaultBackupDir.absolutePath)
                intent.setDataAndType(uri, "application/json")
                intent.putExtra(Intent.EXTRA_TITLE, "appwatcher-" + DbBackupManager.generateFileName())
                startActivityForResult(intent, REQUEST_BACKUP_DEST)
            } else {
                val backupFile = DbBackupManager.generateBackupFile()
                ExportTask(this, this).execute(Uri.fromFile(backupFile))
            }
            ACTION_IMPORT -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                val mimeTypes = arrayOf("application/json", "text/plain", "*/*")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                startActivityForResult(intent, REQUEST_BACKUP_FILE)
            } else {
                startActivity(Intent(this, ListExportActivity::class.java))
            }
            ACTION_LICENSES -> startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            ACTION_SYNC_ENABLE -> {
                syncNowItem.enabled = false // disable temporary sync now
                if (syncEnabledItem.checked) {
                    isProgressVisible = true
                    gDriveSignIn.signIn()
                }

            }
            ACTION_SYNC_NOW -> if (syncNowItem!!.enabled) {
                syncNowItem!!.enabled = false
                val googleAccount = GoogleSignIn.getLastSignedInAccount(this)
                if (googleAccount != null) {
                    GDrive(this, googleAccount, this).sync()
                } else {
                    onGDriveLoginError(0)
                }
            }
            ACTION_GDRIVE_UPLOAD -> {
                val googleAccount = GoogleSignIn.getLastSignedInAccount(this)
                if (googleAccount != null) {
                    GDrive(this, googleAccount).upload()
                }
            }
            ACTION_UPDATE_FREQUENCY -> {
                val values = resources.getIntArray(R.array.updates_frequency_values)
                val dialog = AlertDialog.Builder(this)
                        .setTitle(R.string.pref_title_updates_frequency)
                        .setSingleChoiceItems(R.array.updates_frequency, values.indexOf(prefs.updatesFrequency)) { d, which ->
                            prefs.updatesFrequency = values[which]
                            val useAutoSync = prefs.useAutoSync
                            if (useAutoSync) {
                                SyncScheduler.schedule(this, prefs.isRequiresCharging, prefs.isWifiOnly, prefs.updatesFrequency)
                            } else {
                                SyncScheduler.cancel(this)
                            }
                            frequencyItem.summary = resources.getStringArray(R.array.updates_frequency)[which]
                            wifiItem.enabled = useAutoSync
                            chargingItem.enabled = useAutoSync
                            d.dismiss()
                            notifyDataSetChanged()
                        }.create()
                dialog.show()
            }
            ACTION_WIFI_ONLY -> {
                val useWifiOnly = (pref as CheckboxItem).checked
                prefs.isWifiOnly = useWifiOnly
                SyncScheduler.schedule(this, prefs.isRequiresCharging, useWifiOnly, prefs.updatesFrequency)
            }
            ACTION_REQUIRES_CHARGING -> {
                val requiresCharging = (pref as CheckboxItem).checked
                prefs.isRequiresCharging = requiresCharging
                SyncScheduler.schedule(this, requiresCharging, prefs.isWifiOnly, prefs.updatesFrequency)
            }
            ACTION_NOTIFY_UPTODATE -> {
                val notify = (pref as CheckboxItem).checked
                prefs.isNotifyInstalledUpToDate = notify
            }
            ACTION_THEME -> {
                val dialog = AlertDialog.Builder(this)
                        .setTitle(R.string.pref_title_theme)
                        .setItems(R.array.themes) { _, which ->
                            if (prefs.nightMode != which) {
                                prefs.nightMode = which
                                AppCompatDelegate.setDefaultNightMode(which)
                                this@SettingsActivity.recreate()
                                this.recreateWatchlist()
                            }
                        }.create()
                dialog.show()
            }
            ACTION_DARK_THEME -> {
                val dialog = AlertDialog.Builder(this)
                        .setTitle(R.string.pref_title_dark_theme)
                        .setItems(R.array.dark_themes) { _, which ->
                            if (prefs.theme != which) {
                                prefs.theme = which
                                this@SettingsActivity.setResult(android.app.Activity.RESULT_OK, Intent().putExtra("recreateWatchlistOnBack", true))
                                this.recreateWatchlist()
                            }
                        }.create()
                dialog.show()
            }
            ACTION_EXPORT_DB -> try {
                exportDb()
            } catch (e: IOException) {
                AppLog.e(e)
            }
            ACTION_SHOW_RECENT -> {
                val showRecent = (pref as CheckboxItem).checked
                if (!this.recreateWatchlistOnBack) {
                    this.recreateWatchlistOnBack = prefs.showRecent != showRecent
                }
                prefs.showRecent = showRecent
            }
            ACTION_SHOW_ONDEVICE -> {
                val showOnDevice = (pref as CheckboxItem).checked
                if (!this.recreateWatchlistOnBack) {
                    this.recreateWatchlistOnBack = prefs.showOnDevice != showOnDevice
                }
                prefs.showOnDevice = showOnDevice
            }
            ACTION_USER_LOG -> {
                startActivity(Intent(this, UserLogActivity::class.java))
            }
        }
        notifyDataSetChanged()
    }

    private fun recreateWatchlist() {
        val i = Intent(this@SettingsActivity, AppWatcherActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
    }

    @Throws(IOException::class)
    private fun exportDb() {
        val sd = Environment.getExternalStorageDirectory()
        val dbPath: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            dbPath = filesDir.absolutePath.replace("files", "databases") + File.separator
        } else {
            dbPath = filesDir.path + packageName + "/databases/"
        }
        val currentDBPath = DbSchemaManager.dbName
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
        syncNowItem!!.enabled = true
        prefs.isDriveSyncEnabled = true
        App.provide(this).uploadServiceContentObserver
        notifyDataSetChanged()
        isProgressVisible = false

        Toast.makeText(this, R.string.gdrive_connected, Toast.LENGTH_SHORT).show()
    }

    override fun onGDriveLoginError(errorCode: Int) {
        isProgressVisible = false
        syncNowItem!!.enabled = syncEnabledItem.checked
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

    override fun onImportFinish(code: Int) {
        ImportTask.showImportFinishToast(this, code)
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
        private const val ACTION_GDRIVE_UPLOAD = 13
        private const val ACTION_DARK_THEME = 14
        private const val ACTION_SHOW_RECENT = 15
        private const val ACTION_SHOW_ONDEVICE = 16
        private const val ACTION_USER_LOG = 17
    }
}