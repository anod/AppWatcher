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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by alex on 7/29/14.
 */
public class SyncTask extends ApiClientAsyncTask<Boolean, Boolean, SyncTask.Result> {
    public static final String APPLIST_JSON = "applist.json";
    public static final String MIME_TYPE = "application/json";
    private final Listener mListener;

    public static class Result {
        public boolean status;
        public Exception ex;

        public Result(boolean status, Exception ex) {
            this.status = status;
            this.ex = ex;
        }
    }

    public interface Listener {
        public void onResult(Result result);
    }

    public SyncTask(Context context, Listener listener, GoogleApiClient client) {
        super(context, client);
        mListener = listener;
    }

    @Override
    protected Result doInBackgroundConnected(Boolean... params) {

        try {
            doSyncInBackground();
        } catch (Exception e) {
            AppLog.ex(e);
            return new Result(false, e);
        }

        return new Result(true, null);
    }

    @Override
    protected void onPostExecute(Result result) {
        mListener.onResult(result);
    }

    private void doSyncInBackground() throws Exception {
        DriveId driveId = retrieveFileDriveId();

        InputStreamReader driveFileReader = null;
        if (driveId != null) {
            driveFileReader = getFileInputStream(driveId);
        }

        AppListContentProviderClient cr = new AppListContentProviderClient(mContext);

        syncLists(driveFileReader, cr);

        if (driveId == null) {
            driveId = createNewFile();
        }

        writeToDrive(driveId, cr);

        cr.release();
    }

    private void writeToDrive(DriveId driveId, AppListContentProviderClient cr) throws Exception {

        DriveFile target = Drive.DriveApi.getFile(getGoogleApiClient(), driveId);

        DriveApi.ContentsResult contentsResult = target.openContents(
                getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
        if (!contentsResult.getStatus().isSuccess()) {
            throw new Exception("Error open file for write : "+contentsResult.getStatus().getStatusMessage());
        }

        OutputStream outputStream = contentsResult.getContents().getOutputStream();
        AppListWriter writer = new AppListWriter();
        AppListCursor listCursor = cr.queryAllSorted();
        OutputStreamWriter outWriter = new OutputStreamWriter(outputStream);
        try {
            writer.writeJSON(outWriter, listCursor);
        } catch (IOException e) {
            listCursor.close();
            AppLog.ex(e);
        } finally {
            if (listCursor != null) {
                listCursor.close();
            }
        }
        com.google.android.gms.common.api.Status status = target.commitAndCloseContents(
                getGoogleApiClient(), contentsResult.getContents()).await();
        if (!status.getStatus().isSuccess()) {
            throw new Exception("Error commit changes to file : "+status.getStatusMessage());
        }
    }

    private InputStreamReader getFileInputStream(DriveId driveId) throws Exception {
        DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(), driveId);
        DriveApi.ContentsResult contentsResult =
                file.openContents(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
        if (!contentsResult.getStatus().isSuccess()) {
            throw new Exception("Error read file : "+contentsResult.getStatus().getStatusMessage());
        }

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


        // Add missing remote entries
        if (driveFileReader != null) {
            BufferedReader driveBufferedReader = new BufferedReader(driveFileReader);
            AppListReaderIterator driveAppsIterator = new AppListReaderIterator(driveBufferedReader);
            Map<String, Boolean> currentIds = cr.queryIdsMap();
            while (driveAppsIterator.hasNext()) {
                AppInfo app = driveAppsIterator.next();
                if (app!=null && currentIds.get(app.getAppId()) == null) {
                    cr.insert(app);
                }
            }
        }

        // Clean deleted
        cr.cleanDeleted();

    }

    private DriveId retrieveFileDriveId() throws Exception {

        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, APPLIST_JSON))
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
            AppLog.d("File not found " + APPLIST_JSON);
           return null;
        } else {
            Metadata metadata = metadataList.get(0);
            return metadata.getDriveId();
        }
    }

    private DriveId createNewFile() throws Exception {
        DriveApi.ContentsResult contentsResult = Drive.DriveApi.newContents(getGoogleApiClient()).await();

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
