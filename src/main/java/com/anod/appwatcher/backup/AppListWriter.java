package com.anod.appwatcher.backup;

import com.android.util.JsonWriter;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.content.AppListCursor;

import java.io.IOException;
import java.io.Writer;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 2015-02-27
 */
public class AppListWriter {

    public void writeJSON(Writer file, AppListCursor listCursor)
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

    void writeApp(JsonWriter writer, AppInfo appInfo)
            throws IOException {
        AppLog.d("Write app: " + appInfo.getAppId());
        writer.beginObject();
        writer.name("id").value(appInfo.getAppId());
        writer.name("packageName").value(appInfo.packageName);
        writer.name("title").value(appInfo.title);
        writer.name("creator").value(appInfo.creator);
        writer.name("uploadDate").value(appInfo.uploadDate);
        writer.name("versionName").value(appInfo.versionName);
        writer.name("versionCode").value(appInfo.versionNumber);
        writer.name("status").value(appInfo.getStatus());
        writer.name("detailsUrl").value(appInfo.getDetailsUrl());
        writer.name("iconUrl").value(appInfo.iconUrl);
        writer.name("refreshTime").value(appInfo.refreshTime);
        writer.name("appType").value(appInfo.appType);
        writer.name("syncVersion").value(appInfo.syncVersion);
        writer.endObject();
    }
}
