package com.anod.appwatcher.backup;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.android.util.JsonReader;
import com.android.util.JsonToken;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.AppDetailsUploadDate;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author alex
 * @date 2015-02-27
 */
class AppListReader {

    List<AppInfo> readJsonList(@NonNull Reader reader) throws IOException {
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

    AppInfo readAppInfo(@NonNull JsonReader reader) throws IOException {
        String appId = null, pname = null, versionName = "", title = "", creator = "", uploadDate="", detailsUrl=null, iconUrl=null, appType = "";
        int versionNumber = 0, status = 0, syncVersion = 0;
        Bitmap icon = null;
        long refreshTime = 0;


        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            final boolean isNull = reader.peek() == JsonToken.NULL;
            boolean skipped = false;
            if (name.equals("id")) {
                appId = (isNull) ? null : reader.nextString();
            } else if (name.equals("packageName")) {
                pname = (isNull) ? null : reader.nextString();
            } else if (name.equals("title") && reader.peek() != JsonToken.NULL) {
                title = (isNull) ? "" : reader.nextString();
            } else if (name.equals("creator")) {
                creator = (isNull) ? "" : reader.nextString();
            } else if (name.equals("uploadDate")) {
                uploadDate = (isNull) ? "" : reader.nextString();
            } else if (name.equals("versionName")) {
                versionName = (isNull) ? "" : reader.nextString();
            } else if (name.equals("versionCode")) {
                versionNumber = (isNull) ? 0 : reader.nextInt();
            } else if (name.equals("status")) {
                status = (isNull) ? 0 : reader.nextInt();
            } else if (name.equals("detailsUrl")) {
                detailsUrl = (isNull) ? "" : reader.nextString();
            } else if (name.equals("iconUrl")) {
                iconUrl = (isNull) ? "" : reader.nextString();
            } else if (name.equals("refreshTime")) {
                refreshTime = reader.nextLong();
            } else if (name.equals("appType")) {
                appType = (isNull) ? "" : reader.nextString();
            } else if (name.equals("syncVersion")) {
                syncVersion = (isNull) ? 0 : reader.nextInt();
            } else {
                skipped = true;
                reader.skipValue();
            }
            if (isNull && !skipped) {
                reader.nextNull();
            }
        }
        reader.endObject();
        AppInfo info = null;
        if (appId != null && pname != null) {
            info = new AppInfo(0, appId, pname, versionNumber, versionName,
                    title, creator, iconUrl, status, uploadDate, null, null, null, detailsUrl, refreshTime, appType, syncVersion);
        }
        onUpgrade(info);
        return info;
    }


    private void onUpgrade(AppInfo info) {
        if (TextUtils.isEmpty(info.getDetailsUrl())) {
            String packageName = info.packageName;
            info.setAppId(packageName);
            info.setDetailsUrl(AppInfo.createDetailsUrl(packageName));
        }
    }
}
