package com.anod.appwatcher.backup;

import android.graphics.Bitmap;

import com.android.util.JsonReader;
import com.android.util.JsonToken;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 7/9/14.
 */
public class AppListReader {

    /**
     * @param reader
     * @return List of apps
     * @throws java.io.IOException
     */
    public List<AppInfo> readFromJson(Reader reader) throws IOException {
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
                    title, creator, icon, status, updateTime, null, null, null);
        }
        return null;
    }
}
