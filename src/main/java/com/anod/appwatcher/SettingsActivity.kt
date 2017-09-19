package com.anod.appwatcher

import android.accounts.Account
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDelegate
import android.text.format.DateUtils
import android.widget.Toast
import com.anod.appwatcher.accounts.AccountChooser
import com.anod.appwatcher.backup.DbBackupManager
import com.anod.appwatcher.backup.ExportTask
import com.anod.appwatcher.backup.GDriveSync
import com.anod.appwatcher.backup.ImportTask
import com.anod.appwatcher.model.DbSchemaManager
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.ui.SettingsActionBarActivity
import de.psdev.licensesdialog.LicensesDialog
import info.anodsplace.android.log.AppLog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class SettingsActivity : SettingsActionBarActivity(), ExportTask.Listener, GDriveSync.Listener, AccountChooser.OnAccountSelectionListener, ImportTask.Listener {

    private var gDriveSync: GDriveSync? = null
    private var syncEnabledItem: SettingsActionBarActivity.CheckboxItem? = null
    private var syncNowItem: SettingsActionBarActivity.Item? = null
    private var accountChooser: AccountChooser? = null
    private var wifiItem: SettingsActionBarActivity.CheckboxItem? = null
    private var chargingItem: SettingsActionBarActivity.CheckboxItem? = null
    private lateinit var prefs: Preferences

    override fun onExportStart() {
        AppLog.d("Exporting...")
        setProgressVisibility(true)
    }

    override fun onExportFinish(code: Int) {
        AppLog.d("Code: " + code)
        setProgressVisibility(false)
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

    override fun init() {
        gDriveSync = GDriveSync(this)
        prefs = App.provide(this).prefs
        accountChooser = AccountChooser(this, prefs, this)
        accountChooser!!.init()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        accountChooser!!.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    override fun onPause() {
        super.onPause()
        gDriveSync!!.listener = null
    }

    override fun onResume() {
        super.onResume()

        syncNowItem!!.summary = renderDriveSyncTime()
        gDriveSync!!.listener = this
    }

    override fun initPreferenceItems(): List<SettingsActionBarActivity.Preference> {
        val preferences = mutableListOf<SettingsActionBarActivity.Preference>()

        preferences.add(SettingsActionBarActivity.Category(R.string.category_updates))

        val useAutoSync = prefs.useAutoSync
        preferences.add(SettingsActionBarActivity.CheckboxItem(R.string.menu_auto_update, 0, ACTION_AUTO_UPDATE, useAutoSync))

        wifiItem = SettingsActionBarActivity.CheckboxItem(R.string.menu_wifi_only, 0, ACTION_WIFI_ONLY, prefs.isWifiOnly)
        preferences.add(wifiItem!!)
        wifiItem!!.enabled = useAutoSync

        chargingItem = SettingsActionBarActivity.CheckboxItem(R.string.menu_requires_charging, 0, ACTION_REQUIRES_CHARGING, prefs.isRequiresCharging)
        preferences.add(chargingItem!!)
        chargingItem!!.enabled = useAutoSync

        preferences.add(SettingsActionBarActivity.Category(R.string.settings_notifications))
        preferences.add(SettingsActionBarActivity.CheckboxItem(R.string.uptodate_title, R.string.uptodate_summary, ACTION_NOTIFY_UPTODATE, prefs.isNotifyInstalledUpToDate))

        preferences.add(SettingsActionBarActivity.Category(R.string.pref_header_drive_sync))

        syncEnabledItem = SettingsActionBarActivity.CheckboxItem(R.string.pref_title_drive_sync_enabled, R.string.pref_descr_drive_sync_enabled, ACTION_SYNC_ENABLE)
        syncNowItem = SettingsActionBarActivity.Item(R.string.pref_title_drive_sync_now, 0, ACTION_SYNC_NOW)
        if (!gDriveSync!!.isSupported) {
            syncEnabledItem!!.checked = false
            syncEnabledItem!!.enabled = false
            syncNowItem!!.enabled = false
            syncEnabledItem!!.summaryRes = 0
            syncEnabledItem!!.summary = gDriveSync!!.playServiceStatusText
        } else {
            syncEnabledItem!!.checked = prefs.isDriveSyncEnabled
            syncNowItem!!.enabled = syncEnabledItem!!.checked
            syncNowItem!!.summary = renderDriveSyncTime()
        }
        preferences.add(syncEnabledItem!!)
        preferences.add(syncNowItem!!)
        if (BuildConfig.DEBUG) {
            preferences.add(SettingsActionBarActivity.Item(R.string.pref_gdrive_upload, 0, ACTION_GDRIVE_UPLOAD))
        }

        preferences.add(SettingsActionBarActivity.Category(R.string.pref_header_backup))
        preferences.add(SettingsActionBarActivity.Item(R.string.pref_title_export, R.string.pref_descr_export, ACTION_EXPORT))
        preferences.add(SettingsActionBarActivity.Item(R.string.pref_title_import, R.string.pref_descr_import, ACTION_IMPORT))

        preferences.add(SettingsActionBarActivity.Category(R.string.pref_header_interface))
        preferences.add(SettingsActionBarActivity.Item(R.string.pref_title_theme, R.string.pref_descr_theme, ACTION_THEME))

        preferences.add(SettingsActionBarActivity.Category(R.string.pref_header_about))

        val aboutItem = SettingsActionBarActivity.Item(R.string.pref_title_about, 0, ACTION_ABOUT)
        aboutItem.summary = appVersion
        preferences.add(aboutItem)
        preferences.add(SettingsActionBarActivity.Item(R.string.pref_title_opensource, R.string.pref_descr_opensource, ACTION_LICENSES))

        if (BuildConfig.DEBUG) {
            preferences.add(SettingsActionBarActivity.Item(R.string.pref_export_db, 0, ACTION_EXPORT_DB))
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
            gDriveSync!!.onActivityResult(requestCode, resultCode, data)
            accountChooser?.onActivityResult(requestCode, resultCode, data)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPreferenceItemClick(action: Int, pref: SettingsActionBarActivity.Item) {
        when (action) {
            ACTION_EXPORT -> if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                val uri = Uri.parse(DbBackupManager.defaultBackupDir.absolutePath)
                intent.setDataAndType(uri, "application/json")
                intent.putExtra(Intent.EXTRA_TITLE, "appwatcher-" + DbBackupManager.generateFileName())
                startActivityForResult(intent, REQUEST_BACKUP_DEST)
            } else {
                val backupFile = DbBackupManager.generateBackupFile()
                ExportTask(this, this).execute(Uri.fromFile(backupFile))
            }
            ACTION_IMPORT -> if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                val mimeTypes = arrayOf("application/json", "text/plain", "*/*")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                startActivityForResult(intent, REQUEST_BACKUP_FILE)
            } else {
                startActivity(Intent(this, ListExportActivity::class.java))
            }
            ACTION_LICENSES -> LicensesDialog(this, R.raw.notices, false, true).show()
            ACTION_SYNC_ENABLE -> {
                syncNowItem!!.enabled = false // disable temporary sync now
                if (syncEnabledItem!!.checked) {
                    setProgressVisibility(true)
                    gDriveSync!!.connect()
                }

            }
            ACTION_SYNC_NOW -> if (syncNowItem!!.enabled) {
                syncNowItem!!.enabled = false
                gDriveSync!!.sync()
            }
            ACTION_GDRIVE_UPLOAD -> gDriveSync!!.upload()
            ACTION_AUTO_UPDATE -> {
                val useAutoSync = (pref as SettingsActionBarActivity.CheckboxItem).checked
                if (useAutoSync) {
                    SyncScheduler.schedule(this, prefs.isRequiresCharging, prefs.isWifiOnly)
                } else {
                    SyncScheduler.cancel(this)
                }
                prefs.useAutoSync = useAutoSync
                wifiItem!!.enabled = useAutoSync
                chargingItem!!.enabled = useAutoSync
            }
            ACTION_WIFI_ONLY -> {
                val useWifiOnly = (pref as SettingsActionBarActivity.CheckboxItem).checked
                prefs.isWifiOnly = useWifiOnly
                SyncScheduler.schedule(this, prefs.isRequiresCharging, useWifiOnly)
            }
            ACTION_REQUIRES_CHARGING -> {
                val requiresCharging = (pref as SettingsActionBarActivity.CheckboxItem).checked
                prefs.isRequiresCharging = requiresCharging
                SyncScheduler.schedule(this, requiresCharging, prefs.isWifiOnly)
            }
            ACTION_NOTIFY_UPTODATE -> {
                val notify = (pref as SettingsActionBarActivity.CheckboxItem).checked
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
                                val i = Intent(this@SettingsActivity, AppWatcherActivity::class.java)
                                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(i)
                            }
                        }.create()
                dialog.show()
            }
            ACTION_EXPORT_DB -> try {
                exportDb()
            } catch (e: IOException) {
                AppLog.e(e)
            }
        }
        notifyDataSetChanged()
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

    override fun onGDriveConnect() {
        syncEnabledItem!!.checked = true
        syncEnabledItem!!.enabled = true
        syncNowItem!!.enabled = true
        prefs.isDriveSyncEnabled = true
        App.provide(this).uploadServiceContentObserver
        notifyDataSetChanged()
        setProgressVisibility(false)

        Toast.makeText(this, R.string.gdrive_connected, Toast.LENGTH_SHORT).show()
    }

    override fun onGDriveSyncProgress() {

    }

    override fun onGDriveSyncStart() {
        setProgressVisibility(true)
        Toast.makeText(this, R.string.sync_start, Toast.LENGTH_SHORT).show()
    }

    override fun onGDriveSyncFinish() {
        setProgressVisibility(false)
        prefs.lastDriveSyncTime = System.currentTimeMillis()
        syncNowItem!!.summary = getString(R.string.pref_descr_drive_sync_now, getString(R.string.now))
        syncNowItem!!.enabled = syncEnabledItem!!.checked
        notifyDataSetChanged()
        Toast.makeText(this, R.string.sync_finish, Toast.LENGTH_SHORT).show()
    }

    override fun onGDriveError() {
        setProgressVisibility(false)
        syncNowItem!!.enabled = syncEnabledItem!!.checked
        notifyDataSetChanged()
        Toast.makeText(this, R.string.sync_error, Toast.LENGTH_SHORT).show()
    }

    override fun onAccountSelected(account: Account, authSubToken: String?) {
        if (authSubToken == null) {
            if (App.with(this).isNetworkAvailable) {
                Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, R.string.check_connection, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAccountNotFound(errorMessage: String) {
        if (errorMessage.isNotBlank()) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show()
        }
    }

    override fun onImportFinish(code: Int) {
        ImportTask.showImportFinishToast(this, code)
    }

    companion object {
        private const val ACTION_EXPORT = 3
        private const val ACTION_IMPORT = 4
        private const val ACTION_LICENSES = 6
        private const val ACTION_ABOUT = 5
        private const val ACTION_SYNC_ENABLE = 1
        private const val ACTION_SYNC_NOW = 2
        private const val ACTION_AUTO_UPDATE = 7
        private const val ACTION_WIFI_ONLY = 8
        private const val ACTION_REQUIRES_CHARGING = 9
        private const val ACTION_NOTIFY_UPTODATE = 10
        private const val ACTION_THEME = 11
        private const val ACTION_EXPORT_DB = 12
        private const val ACTION_GDRIVE_UPLOAD = 13

        private const val REQUEST_BACKUP_DEST = 1
        private const val REQUEST_BACKUP_FILE = 2
    }
}
