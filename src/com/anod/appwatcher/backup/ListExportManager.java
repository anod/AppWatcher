package com.anod.appwatcher.backup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Environment;

import com.android.util.JsonWriter;
import com.anod.appwatcher.AppListContentProvider;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.model.AppListTable;
import com.anod.appwatcher.utils.BitmapUtils;

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
     * object.  This ensures that in the unlikely event of the our backup/restore
     * agent running to perform a backup while our UI is updating the file, the
     * agent will not accidentally read partially-written data.
     *
     * <p>Curious but true: a zero-length array is slightly lighter-weight than
     * merely allocating an Object, and can still be synchronized on.
     */
    static final Object[] sDataLock = new Object[0];

    private Context mContext;
    
    public ListExportManager(Context context) {
    	mContext = context;
    }
    
    public long getMainTime() {
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
    
    public File[] getMainBackups() {
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
    
	public int doExport(String filename) {
        if (!checkMediaWritable()) {
        	return ERROR_STORAGE_NOT_AVAILABLE;
        }

        File saveDir = getBackupDir();
        if (!saveDir.exists()) {
        	saveDir.mkdirs();
        } 
        
        File dataFile = new File(saveDir, filename+FILE_EXT_DAT);

        Cursor cr = mContext.getContentResolver().query(
        	AppListContentProvider.CONTENT_URI,
        	AppListTable.APPLIST_PROJECTION, null, null,
        	AppListTable.Columns.KEY_STATUS + " DESC, " +AppListTable.Columns.KEY_TITLE + " COLLATE LOCALIZED ASC"
        );
        AppListCursor listCursor = new AppListCursor(cr);
        try {
            synchronized (ListExportManager.sDataLock) {
            	BufferedWriter buf = new BufferedWriter(new FileWriter(dataFile));
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
		}
		saveDir.setLastModified(System.currentTimeMillis());
		return RESULT_DONE;
	}

	private void writeJSONAppsList(Writer file, AppListCursor listCursor) throws IOException {
		JsonWriter writer = new JsonWriter(file);
		writer.setIndent("  ");
		writer.beginArray();
		listCursor.moveToPosition(-1);
		while (listCursor.moveToNext()) {
			writeApp(writer, listCursor.getAppInfo());
		}
		writer.endArray();
		writer.close();
	}

	private void writeApp(JsonWriter writer, AppInfo appInfo) throws IOException {
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
   			writer.name("icon").value(new String(iconData));
   	    } else {
   			writer.name("icon").value("");
   	    }
		writer.endObject();
	}

	public int doImport(String filename) {
		if (!checkMediaReadable()) {
			return ERROR_STORAGE_NOT_AVAILABLE;
		}
		
        File saveDir = getBackupDir();
        File dataFile = new File(saveDir, filename+FILE_EXT_DAT);
        if (!dataFile.exists()) {
        	return ERROR_FILE_NOT_EXIST;  
        }
        if (!dataFile.canRead()) {
        	return ERROR_FILE_READ;       	
        }
        
        //PreferencesBackup prefs = null;
        //try {
            synchronized (ListExportManager.sDataLock) {
            	//FileInputStream fis = new FileInputStream(dataFile);
            	//ObjectInputStream is = new ObjectInputStream(fis);
                //prefs = (PreferencesBackup) is.readObject();
                //is.close();
            }
		//} catch (IOException e) {
		//	e.printStackTrace();
		//	return ERROR_FILE_READ;
		//} catch (ClassNotFoundException e) {
		//	e.printStackTrace();
        //    return ERROR_DESERIALIZE;
        //}
        //PreferencesStorage storage = new PreferencesStorage(mContext, appWidgetId);
		//storage.save(prefs.getMain());
		return RESULT_DONE;
	}
	
	public File getBackupDir() {
		File externalPath = Environment.getExternalStorageDirectory();
		return new File(externalPath.getAbsolutePath() + DIR_BACKUP);
	}
	
	private boolean checkMediaWritable() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
        	return false;        	
        }
        return true;
	}
	
	private boolean checkMediaReadable() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
        	return false;
        }
        return true;
	}
	
}
