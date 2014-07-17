package com.anod.appwatcher.backup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.android.util.JsonWriter;
import com.android.util.MalformedJsonException;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.utils.BitmapUtils;

/**
 * Serialize and deserialize app info list into/from JSON file
 * @author alex
 *
 */
public class ListExportManager {
	private static final String DIR_BACKUP = "/data/com.anod.appwatcher/backup";
	public static final String FILE_EXT_DAT = ".json";
	public static final int RESULT_DONE = 0;
	public static final int ERROR_STORAGE_NOT_AVAILABLE = 1;
	public static final int ERROR_FILE_NOT_EXIST = 2;
	public static final int ERROR_FILE_READ = 3;
	public static final int ERROR_FILE_WRITE = 4;
	public static final int ERROR_DESERIALIZE = 5;

	/**
	 * We serialize access to our persistent data through a global static
	 * object. This ensures that in the unlikely event of the our backup/restore
	 * agent running to perform a backup while our UI is updating the file, the
	 * agent will not accidentally read partially-written data.
	 * 
	 * <p>
	 * Curious but true: a zero-length array is slightly lighter-weight than
	 * merely allocating an Object, and can still be synchronized on.
	 */
	static final Object[] sDataLock = new Object[0];
    public static final String DATE_FORMAT_FILENAME = "yyyyMMdd_HHmmss.SSS";

    private Context mContext;

	public ListExportManager(Context context) {
		mContext = context;
	}

	/**
	 * 
	 * @return last modified time
	 */
	public long getUpdateTime() {
		File saveDir = getBackupDir();
		if (!saveDir.isDirectory()) {
			return 0;
		}
		String[] files = saveDir.list();
		if (files.length == 0) {
			return 0;
		}
		return saveDir.lastModified();
	}

	/**
	 * List of files in the backup directory
	 * @return
	 */
	public File[] getFileList() {
		File saveDir = getBackupDir();
		if (!saveDir.isDirectory()) {
			return null;
		}
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(FILE_EXT_DAT);
			}
		};
		return saveDir.listFiles(filter);
	}

	/**
	 * write app list into filename
	 * @param filename
	 * @return status
	 */
	public int doExport(String filename) {
		if (!checkMediaWritable()) {
			return ERROR_STORAGE_NOT_AVAILABLE;
		}

		File saveDir = getBackupDir();
		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}

		File dataFile = new File(saveDir, filename + FILE_EXT_DAT);

		AppListContentProviderClient cr = new AppListContentProviderClient(mContext);
		AppListCursor listCursor = cr.queryAllSorted();
        AppListWriter writer = new AppListWriter();
		try {
			synchronized (ListExportManager.sDataLock) {
				BufferedWriter buf = new BufferedWriter(new FileWriter(dataFile));
                writer.writeJSON(buf, listCursor);
			}
		} catch (IOException e) {
			e.printStackTrace();
			listCursor.close();
			return ERROR_FILE_WRITE;
		} finally {
			if (listCursor != null) {
				listCursor.close();
			}
			cr.release();
		}
		saveDir.setLastModified(System.currentTimeMillis());
		return RESULT_DONE;
	}

	/**
	 * Import list from backup
	 * @param filename
	 * @return
	 */
	public int doImport(String filename) {
		if (!checkMediaReadable()) {
			return ERROR_STORAGE_NOT_AVAILABLE;
		}

		File saveDir = getBackupDir();
		File dataFile = new File(saveDir, filename + FILE_EXT_DAT);
		if (!dataFile.exists()) {
			return ERROR_FILE_NOT_EXIST;
		}
		if (!dataFile.canRead()) {
			return ERROR_FILE_READ;
		}

		List<AppInfo> appList = null;
        AppListReader reader = new AppListReader();
		try {
			synchronized (ListExportManager.sDataLock) {
				BufferedReader buf = new BufferedReader(new FileReader(dataFile));
				appList = reader.readFromJson(buf);
			}
		} catch (MalformedJsonException e) {
			e.printStackTrace();
			return ERROR_DESERIALIZE;
		} catch (IOException e) {
			e.printStackTrace();
			return ERROR_FILE_READ;
		}
		if (appList != null && appList.size() > 0) {
			AppListContentProviderClient cr = new AppListContentProviderClient(mContext);
			Map<String, Boolean> currentIds = cr.queryIdsMap();
			for(AppInfo app : appList) {
				if (currentIds.get(app.getAppId()) == null) {
					cr.insert(app);
				}
			}
			cr.release();
		}
		return RESULT_DONE;
	}



	/**
	 * 
	 * @return Full path to Backup dir
	 */
	public File getBackupDir() {
		File externalPath = Environment.getExternalStorageDirectory();
		return new File(externalPath.getAbsolutePath() + DIR_BACKUP);
	}

	/**
	 * Checks if it possible to write to the backup directory
	 * @return true/false
	 */
	private boolean checkMediaWritable() {
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			return false;
		}
		return true;
	}

	/**
	 * Checks if it possible to read from the backup directory
	 * @return true/false
	 */
	private boolean checkMediaReadable() {
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)
				&& !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return false;
		}
		return true;
	}

}
