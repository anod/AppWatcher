package com.anod.appwatcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.anod.appwatcher.backup.GDriveBackup;

public class GDriveSyncActivity extends FragmentActivity implements GDriveBackup.Listener {

    private GDriveBackup mGDriveBackup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdrive_sync);

        mGDriveBackup = new GDriveBackup(this, this);

        if (!mGDriveBackup.isSupported()) {
            // TODO: toast
            finish();
            return;
        }

        //mGDriveBackup.download();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mGDriveBackup.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onGDriveConnect() {

    }

    @Override
    public void onGDriveActionStart() {

    }

    @Override
    public void onGDriveDownloadFinish() {

    }

    @Override
    public void onGDriveUploadFinish() {

    }

    @Override
    public void onGDriveError() {

    }
}
