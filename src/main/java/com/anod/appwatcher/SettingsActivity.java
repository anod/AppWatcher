package com.anod.appwatcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.widget.Toast;

import com.anod.appwatcher.backup.ExportTask;
import com.anod.appwatcher.backup.GDriveBackup;
import com.anod.appwatcher.backup.ListExportManager;
import com.anod.appwatcher.utils.EmailReportSender;
import com.anod.appwatcher.utils.SettingsActionBarActivity;

import org.acra.ACRA;
import org.acra.ErrorReporter;

import java.util.ArrayList;

import de.psdev.licensesdialog.LicensesDialog;

public class SettingsActivity extends SettingsActionBarActivity implements ExportTask.Listener, GDriveBackup.Listener {
    private static final int ACTION_EXPORT = 3;
    private static final int ACTION_IMPORT = 4;
    private static final int ACTION_LICENSES = 6;
    private static final int ACTION_ABOUT = 5;
    private static final int ACTION_SYNC_ENABLE = 1;
    private static final int ACTION_SYNC_NOW = 2;

    private int mAboutCounter;
    private GDriveBackup mGDriveBackup;
    private CheckboxItem mSyncEnabledItem;
    private Item mSyncNowItem;

    @Override
    public void onExportStart() {
        setSupportProgressBarIndeterminate(true);
    }

    @Override
    public void onExportFinish(int code) {
        setSupportProgressBarIndeterminate(true);
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
        mGDriveBackup = new GDriveBackup(this, this);
        mAboutCounter = 0;
    }

    @Override
    protected ArrayList<SettingsActionBarActivity.Preference> getPreferenceItems() {
        ArrayList<Preference> preferences = new ArrayList<Preference>();

        preferences.add(new Category(R.string.pref_header_drive_sync));

        mSyncEnabledItem = new CheckboxItem(R.string.pref_title_drive_sync_enabled, R.string.pref_descr_drive_sync_enabled, ACTION_SYNC_ENABLE);
        mSyncNowItem = new Item(R.string.pref_title_drive_sync_now, R.string.pref_descr_drive_sync_now, ACTION_SYNC_NOW);
        if (!mGDriveBackup.isSupported()) {
            mSyncEnabledItem.checked = false;
            mSyncEnabledItem.enabled = false;
            mSyncNowItem.enabled = false;
        } else {
            mSyncEnabledItem.checked = false;
            mSyncNowItem.enabled = mSyncEnabledItem.checked;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mGDriveBackup.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPreferenceItemClick(int action, SettingsActionBarActivity.Item pref) {
        if (action== ACTION_EXPORT) {
            new ExportTask(this, this).execute("");
        } else if (action==ACTION_IMPORT) {
            startActivity(new Intent(this, ListExportActivity.class));
        } else if (action == ACTION_LICENSES) {
            new LicensesDialog(this, R.raw.notices, false, true).show();
        } else if (action == ACTION_ABOUT) {
            onAboutAction();
        } if (action==ACTION_SYNC_ENABLE) {
            mSyncEnabledItem.checked=!mSyncEnabledItem.checked;
            notifyDataSetChanged();
            if (mSyncEnabledItem.checked) {
                mGDriveBackup.connect();
            }
        } if (action==ACTION_SYNC_NOW) {
            mGDriveBackup.sync();
        }
    }


    private void onAboutAction() {
        if (mAboutCounter >= 4) {
            ErrorReporter rs = ACRA.getErrorReporter();
            rs.removeAllReportSenders();

            EmailReportSender sender = new EmailReportSender(getApplicationContext());
            rs.setReportSender(sender);
            Throwable ex = new Throwable("Report a problem");
            rs.handleException(ex);
        } else {
            if (mAboutCounter ==3 ) {
                Toast.makeText(this, "1 more tap to report a problem", Toast.LENGTH_SHORT).show();
            }
            mAboutCounter++;
        }
    }


    private String getAppVersion() {
        String versionName = "";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return versionName;
    }


    @Override
    public void onGDriveConnect() {
        mSyncEnabledItem.checked=true;
        mSyncEnabledItem.enabled=true;
        mSyncNowItem.enabled=true;
        notifyDataSetChanged();

        Toast.makeText(this,"Google Drive connected",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGDriveActionStart() {
        Toast.makeText(this,"Google Drive start",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGDriveDownloadFinish() {
        Toast.makeText(this,"Google Drive finish",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGDriveUploadFinish() {
        Toast.makeText(this,"Google Drive finish",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGDriveError() {
        mSyncEnabledItem.checked=false;
        mSyncEnabledItem.enabled=false;
        mSyncNowItem.enabled=false;
        notifyDataSetChanged();

        Toast.makeText(this,"Google Drive error",Toast.LENGTH_SHORT).show();
    }
}
