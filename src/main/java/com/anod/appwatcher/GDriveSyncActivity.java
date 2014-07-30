package com.anod.appwatcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.anod.appwatcher.backup.GDriveSync;

public class GDriveSyncActivity extends FragmentActivity implements GDriveSync.Listener {

    private GDriveSync mGDriveSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdrive_sync);

        mGDriveSync = new GDriveSync(this, this);

        if (!mGDriveSync.isSupported()) {
            // TODO: toast
            finish();
            return;
        }

        //mGDriveBackup.download();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mGDriveSync.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onGDriveConnect() {

    }

    @Override
    public void onGDriveActionStart() {

    }

    @Override
    public void onGDriveSyncProgress() {

    }

    @Override
    public void onGDriveSyncStart() {

    }

    @Override
    public void onGDriveSyncFinish() {

    }

    @Override
    public void onGDriveError() {

    }
}
