package com.anod.appwatcher.backup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.android.util.JsonReader;
import com.android.util.JsonToken;
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
		try {
			synchronized (ListExportManager.sDataLock) {
				BufferedWriter buf = new BufferedWriter(
						new FileWriter(dataFile));
				writeJSONAppsList(buf, listCursor);
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
	 * Write list in JSON format
	 * @param file
	 * @param listCursor
	 * @throws IOException
	 */
	private void writeJSONAppsList(Writer file, AppListCursor listCursor)
			throws IOException {
		JsonWriter writer = new JsonWriter(file);
		writer.beginArray();
		listCursor.moveToPosition(-1);
		while (listCursor.moveToNext()) {
			writeApp(writer, listCursor.getAppInfo());
		}
		writer.endArray();
		writer.close();
	}

	/**
	 * Write one app into json
	 * @param writer
	 * @param appInfo
	 * @throws IOException
	 */
	private void writeApp(JsonWriter writer, AppInfo appInfo)
			throws IOException {
		writer.beginObject();
		writer.name("id").value(appInfo.getAppId());
		writer.name("packageName").value(appInfo.getPackageName());
		writer.name("title").value(appInfo.getTitle());
		writer.name("creator").value(appInfo.getCreator());
		writer.name("updateTime").value(appInfo.getUpdateTime());
		writer.name("versionName").value(appInfo.getVersionName());
		writer.name("versionCode").value(appInfo.getVersionCode());
		writer.name("status").value(appInfo.getStatus());
		Bitmap icon = appInfo.getIcon();
		if (icon != null) {
			byte[] iconData = BitmapUtils.flattenBitmap(icon);
			writer.name("icon").beginArray();
			for(int i=0; i<iconData.length; i++) {
				writer.value(iconData[i]);
			}
			writer.endArray();
		} else {
			writer.name("icon").beginArray().endArray();
		}
		writer.endObject();
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
		try {
			synchronized (ListExportManager.sDataLock) {
				BufferedReader buf = new BufferedReader(new FileReader(dataFile));
				appList = readJsonAppsList(buf);
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
	 * @param reader
	 * @return List of apps
	 * @throws IOException
	 */
	public List<AppInfo> readJsonAppsList(Reader reader) throws IOException {
		JsonReader jsonReader = new JsonReader(reader);
		List<AppInfo> apps = new ArrayList<AppInfo>();
		try {
			jsonReader.beginArray();
			while (jsonReader.hasNext()) {
				AppInfo info = readAppInfo(jsonReader);
				if (info!=null) {
					apps.add(info);
				}
			}
			jsonReader.endArray();
		} finally {
			reader.close();
		}
		return apps;
	}

	/**
	 * Reads one app from json
	 * @param reader
	 * @return app info
	 * @throws IOException
	 */
	private AppInfo readAppInfo(JsonReader reader) throws IOException {
		String appId = null, pname = null, versionName = "", title = "", creator = "";
		int versionNumber = 0, status = 0;
		long updateTime = 0;
		Bitmap icon = null;
		
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("id")) {
				appId = reader.nextString();
			} else if (name.equals("packageName")) {
				pname = reader.nextString();
			} else if (name.equals("title") && reader.peek() != JsonToken.NULL) {
				title = reader.nextString();
			} else if (name.equals("creator")) {
				creator = reader.nextString();
			} else if (name.equals("updateTime")) {
				updateTime = reader.nextLong();
			} else if (name.equals("versionName")) {
				versionName = reader.nextString();
			} else if (name.equals("versionCode")) {
				versionNumber = reader.nextInt();
			} else if (name.equals("status")) {
				status = reader.nextInt();
			} else if (name.equals("icon")) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				reader.beginArray();
				while(reader.hasNext()) {
					baos.write(reader.nextInt());
				}
				reader.endArray();
				icon = BitmapUtils.unFlattenBitmap(baos.toByteArray());
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		if (appId != null && pname != null) {
			return new AppInfo(0, appId, pname, versionNumber, versionName,
					title, creator, icon, status, updateTime);
		}
		return null;
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
