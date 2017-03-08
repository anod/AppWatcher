package com.anod.appwatcher;

import android.accounts.Account;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.anod.appwatcher.accounts.AccountChooserHelper;
import com.anod.appwatcher.backup.ExportTask;
import com.anod.appwatcher.backup.GDriveSync;
import com.anod.appwatcher.backup.ImportTask;
import com.anod.appwatcher.backup.ListBackupManager;
import com.anod.appwatcher.fragments.AccountChooserFragment;
import com.anod.appwatcher.sync.SyncScheduler;
import com.anod.appwatcher.ui.SettingsActionBarActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import de.psdev.licensesdialog.LicensesDialog;
import info.anodsplace.android.log.AppLog;


public class SettingsActivity extends SettingsActionBarActivity implements ExportTask.Listener, GDriveSync.Listener, AccountChooserHelper.OnAccountSelectionListener, ImportTask.Listener {
    private static final int ACTION_EXPORT = 3;
    private static final int ACTION_IMPORT = 4;
    private static final int ACTION_LICENSES = 6;
    private static final int ACTION_ABOUT = 5;
    private static final int ACTION_SYNC_ENABLE = 1;
    private static final int ACTION_SYNC_NOW = 2;
    private static final int ACTION_AUTO_UPDATE = 7;
    private static final int ACTION_WIFI_ONLY = 8;
    private static final int ACTION_REQUIRES_CHARGING = 9;
    private static final int ACTION_NOTIFY_UPTODATE = 10;
    private static final int ACTION_THEME = 11;

    private static final int REQUEST_BACKUP_DEST = 1;
    private static final int REQUEST_BACKUP_FILE = 2;


    private GDriveSync mGDriveSync;
    private CheckboxItem mSyncEnabledItem;
    private Item mSyncNowItem;
    private Preferences mPrefs;
    private AccountChooserHelper mAccountChooserHelper;
    private CheckboxItem mWifiItem;
    private CheckboxItem mChargingItem;

    @Override
    public void onExportStart() {
        AppLog.d("Exporting...");
        setProgressVisibility(true);
    }

    @Override
    public void onExportFinish(int code) {
        AppLog.d("Code: "+code);
        setProgressVisibility(false);
        Resources r = getResources();
        if (code == ListBackupManager.RESULT_OK) {
            Toast.makeText(this, r.getString(R.string.export_done), Toast.LENGTH_SHORT).show();
            return;
        }
        switch (code) {
            case ListBackupManager.ERROR_STORAGE_NOT_AVAILABLE:
                Toast.makeText(this, r.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show();
                break;
            case ListBackupManager.ERROR_FILE_WRITE:
                Toast.makeText(this, r.getString(R.string.failed_to_write_file), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void init() {
        mGDriveSync = new GDriveSync(this);
        mPrefs = new Preferences(this);
        mAccountChooserHelper = new AccountChooserHelper(this, mPrefs, this);
        mAccountChooserHelper.init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mAccountChooserHelper.onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGDriveSync.setListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSyncNowItem.summary = renderDriveSyncTime();
        mGDriveSync.setListener(this);
    }

    @Override
    protected ArrayList<SettingsActionBarActivity.Preference> initPreferenceItems() {
        ArrayList<Preference> preferences = new ArrayList<Preference>();


        preferences.add(new Category(R.string.category_updates));

        boolean useAutoSync = mPrefs.useAutoSync();
        preferences.add(new CheckboxItem(R.string.menu_auto_update, 0, ACTION_AUTO_UPDATE, useAutoSync));

        mWifiItem = new CheckboxItem(R.string.menu_wifi_only, 0, ACTION_WIFI_ONLY, mPrefs.isWifiOnly());
        preferences.add(mWifiItem);
        mWifiItem.enabled = useAutoSync;

        mChargingItem = new CheckboxItem(R.string.menu_requires_charging, 0, ACTION_REQUIRES_CHARGING, mPrefs.isRequiresCharging());
        preferences.add(mChargingItem);
        mChargingItem.enabled = useAutoSync;

        preferences.add(new Category(R.string.settings_notifications));
        preferences.add(new CheckboxItem(R.string.uptodate_title, R.string.uptodate_summary, ACTION_NOTIFY_UPTODATE, mPrefs.isNotifyInstalledUpToDate()));


        preferences.add(new Category(R.string.pref_header_drive_sync));

        mSyncEnabledItem = new CheckboxItem(R.string.pref_title_drive_sync_enabled, R.string.pref_descr_drive_sync_enabled, ACTION_SYNC_ENABLE);
        mSyncNowItem = new Item(R.string.pref_title_drive_sync_now, 0, ACTION_SYNC_NOW);
        if (!mGDriveSync.isSupported()) {
            mSyncEnabledItem.checked = false;
            mSyncEnabledItem.enabled = false;
            mSyncNowItem.enabled = false;
            mSyncEnabledItem.summaryRes = 0;
            mSyncEnabledItem.summary = mGDriveSync.getPlayServiceStatusText();
        } else {
            mSyncEnabledItem.checked = mPrefs.isDriveSyncEnabled();
            mSyncNowItem.enabled = mSyncEnabledItem.checked;
            mSyncNowItem.summary = renderDriveSyncTime();
        }
        preferences.add(mSyncEnabledItem);
        preferences.add(mSyncNowItem);

        preferences.add(new Category(R.string.pref_header_backup));
        preferences.add(new Item(R.string.pref_title_export, R.string.pref_descr_export, ACTION_EXPORT));
        preferences.add(new Item(R.string.pref_title_import, R.string.pref_descr_import, ACTION_IMPORT));

        preferences.add(new Category(R.string.pref_header_interface));
        preferences.add(new Item(R.string.pref_title_theme, R.string.pref_descr_theme, ACTION_THEME));

        preferences.add(new Category(R.string.pref_header_about));

        final Item aboutItem = new Item(R.string.pref_title_about, 0, ACTION_ABOUT);
        aboutItem.summary = getAppVersion();
        preferences.add(aboutItem);
        preferences.add(new Item(R.string.pref_title_opensource, R.string.pref_descr_opensource, ACTION_LICENSES));

        return preferences;
    }

    private String renderDriveSyncTime() {
        long time = mPrefs.getLastDriveSyncTime();
        if (time == -1) {
            return getString(R.string.pref_descr_drive_sync_now, getString(R.string.never));
        } else {
            return getString(R.string.pref_descr_drive_sync_now,
                    DateUtils.getRelativeDateTimeString(this, time, 0, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL)
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BACKUP_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                new ImportTask(this, this).execute(data.getData());
            }
        } else if (requestCode == REQUEST_BACKUP_DEST) {
            if (resultCode == Activity.RESULT_OK) {
                new ExportTask(this, this).execute(data.getData());
            }
        } else {
            mGDriveSync.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPreferenceItemClick(int action, SettingsActionBarActivity.Item pref) {
        if (action == ACTION_EXPORT) {

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                Uri uri = Uri.parse(ListBackupManager.getDefaultBackupDir().getAbsolutePath());
                intent.setDataAndType(uri, "application/json");
                intent.putExtra(Intent.EXTRA_TITLE, "appwatcher-" + ListBackupManager.generateFileName());
                startActivityForResult(intent, REQUEST_BACKUP_DEST);
            } else {
                File backupFile = ListBackupManager.generateBackupFile();
                new ExportTask(this, this).execute(Uri.fromFile(backupFile));
            }

        } else if (action == ACTION_IMPORT) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                String[] mimeTypes = {"application/json", "text/plain", "*/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(intent, REQUEST_BACKUP_FILE);
            } else {
                startActivity(new Intent(this, ListExportActivity.class));
            }
        } else if (action == ACTION_LICENSES) {
            new LicensesDialog(this, R.raw.notices, false, true).show();
        } else if (action == ACTION_SYNC_ENABLE) {
            mSyncNowItem.enabled = false; // disable temporary sync now
            if (mSyncEnabledItem.checked) {
                setProgressVisibility(true);
                mGDriveSync.connect();
            }

        } else if (action == ACTION_SYNC_NOW) {
            if (mSyncNowItem.enabled) {
                mSyncNowItem.enabled = false;
                mGDriveSync.sync();
            }
        } else if (action == ACTION_AUTO_UPDATE) {
            boolean useAutoSync = ((CheckboxItem) pref).checked;
            if (useAutoSync) {
                SyncScheduler.schedule(this, mPrefs.isRequiresCharging());
            } else {
                SyncScheduler.cancel(this);
            }
            mPrefs.setUseAutoSync(useAutoSync);
            mWifiItem.enabled = useAutoSync;
            mChargingItem.enabled = useAutoSync;
        } else if (action == ACTION_WIFI_ONLY) {
            boolean useWifiOnly = ((CheckboxItem) pref).checked;
            mPrefs.saveWifiOnly(useWifiOnly);
        } else if (action == ACTION_REQUIRES_CHARGING) {
            boolean requiresCharging = ((CheckboxItem) pref).checked;
            mPrefs.setRequiresCharging(requiresCharging);
            SyncScheduler.schedule(this, requiresCharging);
        } else if (action == ACTION_NOTIFY_UPTODATE) {
            boolean notify = ((CheckboxItem) pref).checked;
            mPrefs.setNotifyInstalledUpToDate(notify);
        } else if (action == ACTION_THEME)
        {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.pref_title_theme)
                    .setItems(R.array.themes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mPrefs.getNightMode() != which) {
                                mPrefs.setNightMode(which);
                                AppCompatDelegate.setDefaultNightMode(which);
                                SettingsActivity.this.recreate();
                                Intent i = new Intent(SettingsActivity.this, AppWatcherActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                            }
                        }
                    }).create();
            dialog.show();
        }
        notifyDataSetChanged();
    }

    private String getAppVersion() {
        return String.format(Locale.US, "%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
    }

    @Override
    public void onGDriveConnect() {
        mSyncEnabledItem.checked = true;
        mSyncEnabledItem.enabled = true;
        mSyncNowItem.enabled = true;
        mPrefs.saveDriveSyncEnabled(true);
        notifyDataSetChanged();
        setProgressVisibility(false);

        Toast.makeText(this, R.string.gdrive_connected, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGDriveSyncProgress() {

    }

    @Override
    public void onGDriveSyncStart() {
        setProgressVisibility(true);
        Toast.makeText(this, R.string.sync_start, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGDriveSyncFinish() {
        setProgressVisibility(false);
        mPrefs.saveDriveSyncTime(System.currentTimeMillis());
        mSyncNowItem.summary = getString(R.string.pref_descr_drive_sync_now, getString(R.string.now));
        mSyncNowItem.enabled = mSyncEnabledItem.checked;
        notifyDataSetChanged();
        Toast.makeText(this, R.string.sync_finish, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onGDriveError() {
        setProgressVisibility(false);
        mSyncNowItem.enabled = mSyncEnabledItem.checked;
        notifyDataSetChanged();
        Toast.makeText(this, R.string.sync_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onHelperAccountSelected(Account account, String authSubToken) {
        if (authSubToken == null) {
            Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onHelperAccountNotFound() {
        Toast.makeText(this, R.string.failed_gain_access, Toast.LENGTH_LONG).show();
    }

    @Override
    public AccountChooserFragment.OnAccountSelectionListener getAccountSelectionListener() {
        return mAccountChooserHelper;
    }

    @Override
    public void onImportFinish(int code) {
        ImportTask.showImportFinishToast(this, code);
    }
}
