package com.anod.appwatcher.backup.gdrive;

import android.content.Context;
import android.support.v4.util.SimpleArrayMap;

import com.anod.appwatcher.backup.AppListReaderIterator;
import com.anod.appwatcher.backup.AppListWriter;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.content.DbContentProviderClient;
import com.anod.appwatcher.content.AppListCursor;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 2014-11-15
 */
public class SyncConnectedWorker {
    /**
     * Lock used when maintaining queue of requested updates.
     */
    private final static Object sLock = new Object();

    private static final String APPLIST_JSON = "applist.json";
    private static final String MIME_TYPE = "application/json";

    private final Context mContext;
    private final GoogleApiClient mGoogleApiClient;

    public SyncConnectedWorker(Context context, GoogleApiClient client) {
        mContext = context;
        mGoogleApiClient = client;
    }

    public void doSyncInBackground() throws Exception {
        synchronized (sLock) {
            DbContentProviderClient cr = new DbContentProviderClient(mContext);
            try {
                doSyncLocked(cr);
            } catch (Exception e) {
                throw new Exception(e);
            } finally {
                cr.close();
            }
        }
    }

    private void doSyncLocked(DbContentProviderClient cr) throws Exception {
        Drive.DriveApi.requestSync(mGoogleApiClient).await();

        DriveId driveId = retrieveFileDriveId();
        // There is as file exist, create driveFileReader
        if (driveId != null) {
            insertRemoteItems(driveId, cr);
        }

        if (driveId == null) {
            if (cr.getCount(false) > 0) {
                driveId = createNewFile();
            }
        }

        if (driveId != null) {
            writeToDrive(driveId, cr);
        }

        AppLog.d("[GDrive] Clean locally deleted apps ");
        // Clean deleted
        int numRows = cr.cleanDeleted();
        AppLog.d("[GDrive] Cleaned " + numRows + " rows");

        Drive.DriveApi.requestSync(mGoogleApiClient).await();
    }

    private void writeToDrive(DriveId driveId, DbContentProviderClient cr) throws Exception {

        AppLog.d("[GDrive] Write full list to remote ");

        DriveFile target = driveId.asDriveFile();

        DriveApi.DriveContentsResult contentsResult = target.open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).await();
        if (!contentsResult.getStatus().isSuccess()) {
            throw new Exception("Error open file for write : " + contentsResult.getStatus().getStatusMessage());
        }

        DriveContents contents = contentsResult.getDriveContents();
        OutputStream outputStream = contents.getOutputStream();
        AppListWriter writer = new AppListWriter();
        AppListCursor listCursor = cr.queryAllSorted(false);
        BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        try {
            writer.writeJSON(outWriter, listCursor);
        } catch (IOException e) {
            listCursor.close();
            AppLog.e(e);
        } finally {
            if (listCursor != null) {
                listCursor.close();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                AppLog.e(e);
            }
        }
        com.google.android.gms.common.api.Status status = contents.commit(mGoogleApiClient, null).await();
        if (!status.getStatus().isSuccess()) {
            throw new Exception("Error commit changes to file : " + status.getStatusMessage());
        }
    }

    private InputStreamReader getFileInputStream(DriveContents contents) throws Exception {
        InputStream inputStream = contents.getInputStream();
        if (inputStream == null) {
            throw new Exception("Empty input stream ");
        }
        return new InputStreamReader(inputStream, "UTF-8");
    }

    private void insertRemoteItems(DriveId driveId, DbContentProviderClient cr) throws Exception {
        DriveFile file = driveId.asDriveFile();
        DriveApi.DriveContentsResult contentsResult = file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).await();
        if (!contentsResult.getStatus().isSuccess()) {
            throw new Exception("Error read file : " + contentsResult.getStatus().getStatusMessage());
        }
        DriveContents contents = contentsResult.getDriveContents();
        InputStreamReader driveFileReader = getFileInputStream(contents);

        AppLog.d("[GDrive] Sync remote list " + APPLIST_JSON);

        // Add missing remote entries
        BufferedReader driveBufferedReader = new BufferedReader(driveFileReader);
        AppListReaderIterator driveAppsIterator = new AppListReaderIterator(driveBufferedReader);
        SimpleArrayMap<String, Integer> currentIds = cr.queryPackagesMap(true);
        AppLog.d("[GDrive] Read remote apps " + APPLIST_JSON);
        while (driveAppsIterator.hasNext()) {
            AppInfo app = driveAppsIterator.next();
            AppLog.d("[GDrive] Read app: " + app.packageName);
            if (!currentIds.containsKey(app.packageName)) {
                cr.insert(app);
            }
        }

        driveFileReader.close();
        contents.discard(mGoogleApiClient);
    }

    private DriveId retrieveFileDriveId() throws Exception {

        SortOrder order = new SortOrder.Builder()
                .addSortDescending(SortableField.QUOTA_USED)
                .build();

        Query query = new Query.Builder()
                .addFilter(Filters.and(
                        Filters.eq(SearchableField.MIME_TYPE, MIME_TYPE),
                        Filters.eq(SearchableField.TITLE, APPLIST_JSON)
                ))
                .setSortOrder(order)
                .build();

        DriveApi.MetadataBufferResult metadataBufferResult = Drive.DriveApi
                .getAppFolder(mGoogleApiClient)
                .queryChildren(mGoogleApiClient, query)
                .await();


        if (!metadataBufferResult.getStatus().isSuccess()) {
            throw new Exception("Problem retrieving " + APPLIST_JSON + " : " + metadataBufferResult.getStatus().getStatusMessage());
        }
        MetadataBuffer metadataList = metadataBufferResult.getMetadataBuffer();
        if (metadataList.getCount() == 0) {
            AppLog.d("[GDrive] File NOT found " + APPLIST_JSON);
            return null;
        } else {
            Metadata metadata = metadataList.get(0);
            AppLog.d("[GDrive] File found " + APPLIST_JSON);
            return metadata.getDriveId();
        }
    }

    private DriveId createNewFile() throws Exception {
        DriveApi.DriveContentsResult contentsResult = Drive.DriveApi.newDriveContents(mGoogleApiClient).await();
        AppLog.d("[GDrive] Create new file ");

        if (!contentsResult.getStatus().isSuccess()) {
            throw new Exception("[Google Drive] File create request filed: " + contentsResult.getStatus().getStatusMessage());
        }
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(APPLIST_JSON)
                .setMimeType(MIME_TYPE)
                .build();

        DriveFolder.DriveFileResult driveFileResult = Drive.DriveApi
                .getAppFolder(mGoogleApiClient)
                .createFile(mGoogleApiClient, changeSet, contentsResult.getDriveContents()).await();

        if (!driveFileResult.getStatus().isSuccess()) {
            throw new Exception("[Google Drive] File create result filed: " + driveFileResult.getStatus().getStatusMessage());
        }
        return driveFileResult.getDriveFile().getDriveId();
    }


}
