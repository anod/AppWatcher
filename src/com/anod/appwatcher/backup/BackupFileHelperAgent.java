package com.anod.appwatcher.backup;

import java.io.File;
import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;

import com.anod.appwatcher.model.DbOpenHelper;

public class BackupFileHelperAgent extends BackupAgentHelper {
	// A key to uniquely identify the set of backup data
	private static final String APPDB_BACKUP_KEY = "appdb";
	// Object for intrinsic lock
	private static final Object[] sDataLock = new Object[0];
	
	@Override
	public void onCreate() {
		FileBackupHelper dbs = new FileBackupHelper(this, DbOpenHelper.DATABASE_NAME);
		addHelper(APPDB_BACKUP_KEY, dbs);
	}

	@Override
	public File getFilesDir() {
		File path = getDatabasePath(DbOpenHelper.DATABASE_NAME);
		return path.getParentFile();
	}
	
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
	          ParcelFileDescriptor newState) throws IOException {
	    // Hold the lock while the FileBackupHelper performs backup
	    synchronized (BackupFileHelperAgent.sDataLock) {
	        super.onBackup(oldState, data, newState);
	    }
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
	        ParcelFileDescriptor newState) throws IOException {
	    // Hold the lock while the FileBackupHelper restores the file
	    synchronized (BackupFileHelperAgent.sDataLock) {
	        super.onRestore(data, appVersionCode, newState);
	    }
	}	
}
