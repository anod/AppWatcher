package com.anod.appwatcher.backup;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.android.util.MalformedJsonException;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.AppListCursor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import info.anodsplace.android.log.AppLog;

/**
 * Serialize and deserialize app info list into/from JSON file
 *
 * @author alex
 */
public class ListBackupManager {
    private static final String DIR_BACKUP = "/data/com.anod.appwatcher/backup";

    public static final String FILE_EXT_DAT = ".json";
    public static final int RESULT_OK = 0;
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
     * <p/>
     * <p/>
     * Curious but true: a zero-length array is slightly lighter-weight than
     * merely allocating an Object, and can still be synchronized on.
     */
    static final Object[] sDataLock = new Object[0];
    static final String DATE_FORMAT_FILENAME = "yyyyMMdd_HHmmss";

    private Context mContext;

    public ListBackupManager(Context context) {
        mContext = context;
    }

    /**
     * @return Full path to Backup dir
     */
    public static File getDefaultBackupDir() {
        File externalPath = Environment.getExternalStorageDirectory();
        return new File(externalPath.getAbsolutePath() + DIR_BACKUP);
    }

    /**
     * List of files in the backup directory
     *
     * @return
     */
    public File[] getFileList() {
        File saveDir = getDefaultBackupDir();
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

    int doExport(Uri destUri) {
        OutputStream outputStream;
        try {
            outputStream = mContext.getContentResolver().openOutputStream(destUri);
        } catch (FileNotFoundException e) {
            return ERROR_FILE_WRITE;
        }

        if (!writeList(outputStream)) {
            return ERROR_FILE_WRITE;
        }
        return RESULT_OK;
    }

    boolean writeList(@NonNull  OutputStream outputStream) {
        AppLog.d("Write into: " + outputStream.toString());
        AppListWriter writer = new AppListWriter();
        AppListContentProviderClient cr = new AppListContentProviderClient(mContext);
        AppListCursor listCursor = cr.queryAllSorted(true);
        try {
            synchronized (ListBackupManager.sDataLock) {
                BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(outputStream));
                writer.writeJSON(buf, listCursor);
            }
        } catch (IOException e) {
            AppLog.e(e);
            listCursor.close();
            cr.release();
            return false;
        } finally {
            if (listCursor != null && !listCursor.isClosed()) {
                listCursor.close();
            }
            cr.release();
        }
        return true;
    }

    int doImport(Uri uri) {
        InputStream inputStream;
        try {
            inputStream = mContext.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            return ERROR_FILE_READ;
        }
        if (inputStream == null) {
            return ERROR_FILE_READ;
        }
        AppListReader reader = new AppListReader();
        List<AppInfo> appList;
        try {
            synchronized (ListBackupManager.sDataLock) {
                BufferedReader buf = new BufferedReader(new InputStreamReader(inputStream));
                appList = reader.readJsonList(buf);
            }
        } catch (MalformedJsonException e) {
            AppLog.e(e);
            return ERROR_DESERIALIZE;
        } catch (IOException e) {
            AppLog.e(e);
            return ERROR_FILE_READ;
        }
        if (appList != null && appList.size() > 0) {
            AppListContentProviderClient cr = new AppListContentProviderClient(mContext);
            cr.addList(appList);
            cr.release();
        }
        return RESULT_OK;
    }

    public static String generateFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat(ListBackupManager.DATE_FORMAT_FILENAME, Locale.US);
        return sdf.format(new Date(System.currentTimeMillis())) + FILE_EXT_DAT;
    }

    public static File generateBackupFile() {
        return new File(getDefaultBackupDir(), generateFileName());
    }

    public static File getBackupFile(String filename) {
        return new File(getDefaultBackupDir(), filename + FILE_EXT_DAT);
    }
}
