package com.anod.appwatcher.backup;

import android.database.Cursor;

import com.android.util.JsonWriter;
import com.anod.appwatcher.content.DbContentProviderClient;
import com.anod.appwatcher.content.TagsCursor;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.content.AppListCursor;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.model.schema.AppTagsTable;

import java.io.IOException;
import java.io.Writer;

import info.anodsplace.android.log.AppLog;

/**
 * @author alex
 * @date 2015-02-27
 */
public class DbJsonWriter {

    public void write(Writer file, DbContentProviderClient client) throws IOException {
        JsonWriter writer = new JsonWriter(file);
        writer.beginObject();

        AppListCursor appsCursor = client.queryAllSorted(true);
        writeAppList(writer.name("apps"), appsCursor);
        appsCursor.close();

        TagsCursor tagsCursor = client.queryTags();
        writeTagsList(writer.name("tags"), tagsCursor);
        tagsCursor.close();

        Cursor appTagsCursor = client.queryAppTags();
        writeAppTagsList(writer.name("app_tags"), appTagsCursor);
        appTagsCursor.close();

        writer.endObject();
        writer.close();
    }

    private void writeAppTagsList(JsonWriter writer, Cursor cursor) throws IOException {
        writer.beginArray();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            writer.beginObject();
            writer.name("appId").value(cursor.getString(AppTagsTable.Projection.APPID));
            writer.name("tagId").value(cursor.getInt(AppTagsTable.Projection.TAGID));
            writer.endObject();
        }
        writer.endArray();
    }

    private void writeTagsList(JsonWriter writer, TagsCursor cursor) throws IOException {
        writer.beginArray();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            writeTag(writer, cursor.getTag());
        }
        writer.endArray();
    }

    private void writeTag(JsonWriter writer, Tag tag) throws IOException {
        writer.beginObject();
        writer.name("id").value(tag.id);
        writer.name("name").value(tag.name);
        writer.name("color").value(tag.color);
        writer.endObject();
    }

    private void writeAppList(JsonWriter writer, AppListCursor listCursor) throws IOException {
        writer.beginArray();
        listCursor.moveToPosition(-1);
        while (listCursor.moveToNext()) {
            writeApp(writer, listCursor.getAppInfo());
        }
        writer.endArray();
    }

    private void writeApp(JsonWriter writer, AppInfo appInfo) throws IOException {
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
