package com.anod.appwatcher.backup.gdrive;

import android.content.Context;

import com.anod.appwatcher.backup.AppListReaderIterator;
import com.anod.appwatcher.backup.AppListWriter;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppListContentProviderClient;
import com.anod.appwatcher.model.AppListCursor;
import com.anod.appwatcher.utils.AppLog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
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
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by alex on 7/30/14.
 */
public class SyncConnectedWorker {
    /**
     * Lock used when maintaining queue of requested updates.
     */
    public final static Object sLock = new Object();

    public static final String APPLIST_JSON = "applist.json";
    public static final String MIME_TYPE = "application/json";

    private final Context mContext;
    private final GoogleApiClient mGoogleApiClient;

    public SyncConnectedWorker(Context context, GoogleApiClient client) {
        mContext = context;
        mGoogleApiClient = client;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }
    public void doSyncInBackground() throws Exception {
        synchronized (sLock) {
            doSyncLocked();
        }
    }

    private void doSyncLocked() throws Exception {
        Drive.DriveApi.requestSync(mGoogleApiClient).await();

        DriveId driveId = retrieveFileDriveId();

        AppListContentProviderClient cr = new AppListContentProviderClient(mContext);

        InputStreamReader driveFileReader = null;
        DriveFile file = null;
        DriveApi.ContentsResult contentsResult = null;
        if (driveId != null) {
            file = Drive.DriveApi.getFile(getGoogleApiClient(), driveId);
            contentsResult = file.openContents(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
            if (!contentsResult.getStatus().isSuccess()) {
                throw new Exception("Error read file : "+contentsResult.getStatus().getStatusMessage());
            }
            driveFileReader = getFileInputStream(contentsResult);
        }

        syncLists(driveFileReader, cr);

        if (file != null) {
            file.discardContents(getGoogleApiClient(), contentsResult.getContents()).await();
        }
        if (driveFileReader != null) {
            driveFileReader.close();
        }


        if (driveId == null) {
            if (cr.getCount() > 0) {
                driveId = createNewFile();
            }
        }

        if (driveId!=null) {
            writeToDrive(driveId, cr);
        }

        cr.release();
        Drive.DriveApi.requestSync(mGoogleApiClient).await();

    }

    private void writeToDrive(DriveId driveId, AppListContentProviderClient cr) throws Exception {

        AppLog.d("[GDrive] Write full list to remote ");

        DriveFile target = Drive.DriveApi.getFile(getGoogleApiClient(), driveId);

        DriveApi.ContentsResult contentsResult = target.openContents(
                getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
        if (!contentsResult.getStatus().isSuccess()) {
            throw new Exception("Error open file for write : "+contentsResult.getStatus().getStatusMessage());
        }

        OutputStream outputStream = contentsResult.getContents().getOutputStream();
        AppListWriter writer = new AppListWriter();
        AppListCursor listCursor = cr.queryAllSorted();
        BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        try {
            writer.writeJSON(outWriter, listCursor);
        } catch (IOException e) {
            listCursor.close();
            AppLog.ex(e);
        } finally {
            if (listCursor != null) {
                listCursor.close();
            }
            outputStream.close();;
        }
        com.google.android.gms.common.api.Status status = target.commitAndCloseContents(
                getGoogleApiClient(), contentsResult.getContents()).await();
        if (!status.getStatus().isSuccess()) {
            throw new Exception("Error commit changes to file : "+status.getStatusMessage());
        }
    }

    private InputStreamReader getFileInputStream(DriveApi.ContentsResult contentsResult) throws Exception {
        InputStream inputStream = contentsResult.getContents().getInputStream();
        if (inputStream == null) {
            throw new Exception("Empty input stream ");
        }
        try {
            return new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            AppLog.ex(e);
            return null;
        }
    }

    private void syncLists(InputStreamReader driveFileReader, AppListContentProviderClient cr) {

        AppLog.d("[GDrive] Sync remote list " + APPLIST_JSON);
        // Add missing remote entries
        if (driveFileReader != null) {
            BufferedReader driveBufferedReader = new BufferedReader(driveFileReader);
            AppListReaderIterator driveAppsIterator = new AppListReaderIterator(driveBufferedReader);
            Map<String, Boolean> currentIds = cr.queryPackagesMap();
            AppLog.d("[GDrive] Read remote apps " + APPLIST_JSON);
            while (driveAppsIterator.hasNext()) {
                AppInfo app = driveAppsIterator.next();
                AppLog.d("[GDrive] Read app: " + app.getPackageName());
                if (app!=null && currentIds.get(app.getPackageName()) == null) {
                    cr.insert(app);
                }
            }
        }

        AppLog.d("[GDrive] Clean locally deleted apps ");
        // Clean deleted
        int numRows = cr.cleanDeleted();
        AppLog.d("[GDrive] Cleaned "+numRows+" rows");
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
                .getAppFolder(getGoogleApiClient())
                .queryChildren(getGoogleApiClient(), query)
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
        DriveApi.ContentsResult contentsResult = Drive.DriveApi.newContents(getGoogleApiClient()).await();
        AppLog.d("[GDrive] Create new file ");

        if (!contentsResult.getStatus().isSuccess()) {
            throw new Exception("[Google Drive] File create request filed: "+contentsResult.getStatus().getStatusMessage());
        }
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(APPLIST_JSON)
                .setMimeType(MIME_TYPE)
                .build();

        DriveFolder.DriveFileResult driveFileResult = Drive.DriveApi
                .getAppFolder(getGoogleApiClient())
                .createFile(getGoogleApiClient(), changeSet, contentsResult.getContents()).await();

        if (!driveFileResult.getStatus().isSuccess()) {
            throw new Exception("[Google Drive] File create result filed: "+driveFileResult.getStatus().getStatusMessage());
        }
        return driveFileResult.getDriveFile().getDriveId();
    }


}
