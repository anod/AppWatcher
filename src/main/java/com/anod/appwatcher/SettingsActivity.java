package com.anod.appwatcher;

import android.Manifest;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.anod.appwatcher.accounts.AccountChooserHelper;
import com.anod.appwatcher.backup.ExportTask;
import com.anod.appwatcher.backup.GDriveSync;
import com.anod.appwatcher.backup.ListExportManager;
import com.anod.appwatcher.fragments.AccountChooserFragment;
import com.anod.appwatcher.ui.SettingsActionBarActivity;
import com.anod.appwatcher.utils.AppPermissions;

import java.util.ArrayList;

import de.psdev.licensesdialog.LicensesDialog;
import info.anodsplace.android.log.AppLog;


public class SettingsActivity extends SettingsActionBarActivity implements ExportTask.Listener, GDriveSync.Listener, AccountChooserHelper.OnAccountSelectionListener {
    private static final int ACTION_EXPORT = 3;
    private static final int ACTION_IMPORT = 4;
    private static final int ACTION_LICENSES = 6;
    private static final int ACTION_ABOUT = 5;
    private static final int ACTION_SYNC_ENABLE = 1;
    private static final int ACTION_SYNC_NOW = 2;
    private static final int ACTION_AUTO_UPDATE = 7;
    private static final int ACTION_WIFI_ONLY = 8;

    private GDriveSync mGDriveSync;
    private CheckboxItem mSyncEnabledItem;
    private Item mSyncNowItem;
    private Preferences mPrefs;
    private AccountChooserHelper mAccountChooserHelper;
    private CheckboxItem mWifiItem;

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
        if (code == ListExportManager.RESULT_DONE) {
            Toast.makeText(this, r.getString(R.string.export_done), Toast.LENGTH_SHORT).show();
            return;
        }
        switch (code) {
            case ListExportManager.ERROR_STORAGE_NOT_AVAILABLE:
                Toast.makeText(this, r.getString(R.string.external_storage_not_available), Toast.LENGTH_SHORT).show();
                break;
            case ListExportManager.ERROR_FILE_WRITE:
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

        AppPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, AppPermissions.REQUEST_STORAGE_READ, new AppPermissions.PermissionResult() {
            @Override
            public void granted() {
                startActivity(new Intent(SettingsActivity.this, ListExportActivity.class));
            }

            @Override
            public void denied() {
                Toast.makeText(SettingsActivity.this, R.string.import_permission_denied, Toast.LENGTH_SHORT).show();
            }
        });


        AppPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, AppPermissions.REQUEST_STORAGE_WRITE, new AppPermissions.PermissionResult() {
            @Override
            public void granted() {
                SettingsActivity.this.runExport();
            }

            @Override
            public void denied() {
                Toast.makeText(SettingsActivity.this, R.string.export_permission_denied, Toast.LENGTH_SHORT).show();
            }
        });

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

        Account syncAccount = mAccountChooserHelper.getAccount();
        boolean useAutoSync = false;
        if (syncAccount != null) {
            useAutoSync = ContentResolver.getSyncAutomatically(syncAccount, AppListContentProvider.AUTHORITY);
        }
        preferences.add(new CheckboxItem(R.string.menu_auto_update, 0, ACTION_AUTO_UPDATE, useAutoSync));

        mWifiItem = new CheckboxItem(R.string.menu_wifi_only, 0, ACTION_WIFI_ONLY, mPrefs.isWifiOnly());
        preferences.add(mWifiItem);
        mWifiItem.enabled = useAutoSync;


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
        mGDriveSync.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void runExport()
    {
        new ExportTask(this, this).execute("");
    }

    @Override
    protected void onPreferenceItemClick(int action, SettingsActionBarActivity.Item pref) {
        if (action == ACTION_EXPORT) {

            if (AppPermissions.isGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                this.runExport();
            }
            else
            {
                AppPermissions.request(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, AppPermissions.REQUEST_STORAGE_WRITE);
            }

        } else if (action == ACTION_IMPORT) {

            if (AppPermissions.isGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                startActivity(new Intent(this, ListExportActivity.class));
            }
            else
            {
                AppPermissions.request(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, AppPermissions.REQUEST_STORAGE_READ);
            }

        } else if (action == ACTION_LICENSES) {
            new LicensesDialog(this, R.raw.notices, false, true).show();
        } else if (action == ACTION_SYNC_ENABLE) {
            mSyncNowItem.enabled = false; // disable temporary sync now
            notifyDataSetChanged();
            if (mSyncEnabledItem.checked) {
                setProgressVisibility(true);
                mGDriveSync.connect();
            }

        } else if (action == ACTION_SYNC_NOW) {
            if (mSyncNowItem.enabled) {
                mSyncNowItem.enabled = false;
                notifyDataSetChanged();
                mGDriveSync.sync();
            }
        } else if (action == ACTION_AUTO_UPDATE) {
            boolean useAutoSync = ((CheckboxItem) pref).checked;
            mAccountChooserHelper.setSync(useAutoSync);
            mWifiItem.enabled = useAutoSync;
            notifyDataSetChanged();
        } else if (action == ACTION_WIFI_ONLY) {
            boolean useWifiOnly = ((CheckboxItem) pref).checked;
            mPrefs.saveWifiOnly(useWifiOnly);
            mAccountChooserHelper.setSync(true);
            notifyDataSetChanged();
        }
    }

    private String getAppVersion() {
        String versionName = "";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return versionName;
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
}
