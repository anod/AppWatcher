package com.anod.appwatcher.backup;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.android.util.JsonReader;
import com.android.util.JsonToken;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppTag;
import com.anod.appwatcher.model.Tag;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author alex
 * @date 2015-02-27
 */
public class DbJsonReader {

    static class Container {
        final List<AppInfo> apps;
        final List<Tag> tags;
        final List<AppTag> appTags;

        Container(List<AppInfo> apps, List<Tag> tags, List<AppTag> appTags) {
            this.apps = apps;
            this.tags = tags;
            this.appTags = appTags;
        }
    }

    public interface OnReadListener {
        void onAppRead(AppInfo app);
        void onTagRead(Tag tag);
        void onAppTagRead(AppTag appTag);
        void onFinish() throws IOException;
    }

    public void read(@NonNull Reader reader,@NonNull OnReadListener listener) throws IOException {
        JsonReader jsonReader = new JsonReader(reader);

        if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
            readAppList(jsonReader, listener);
            reader.close();
            return;
        }

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("apps")) {
                readAppList(jsonReader, listener);
            } else if (name.equals("tags")) {
                readTags(jsonReader, listener);
            } else if (name.equals("app_tags")) {
                readAppTags(jsonReader, listener);
            }
        }

        jsonReader.endObject();
        reader.close();
    }

    Container read(@NonNull Reader reader) throws IOException {
        final JsonReader jsonReader = new JsonReader(reader);

        final List<AppInfo> apps = new ArrayList<>();
        final List<Tag> tags = new ArrayList<>();
        final List<AppTag> appTags = new ArrayList<>();

        OnReadListener listener = new OnReadListener() {
            @Override
            public void onAppRead(AppInfo app) {
                apps.add(app);
            }
            @Override
            public void onTagRead(Tag tag) {
                tags.add(tag);
            }
            @Override
            public void onAppTagRead(AppTag appTag) {
                appTags.add(appTag);
            }
            @Override
            public void onFinish() { }
        };

        if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
            readAppList(jsonReader, listener);
            reader.close();
            listener.onFinish();

            return new Container(apps, tags, appTags);
        }


        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("apps")) {
                readAppList(jsonReader, listener);
            } else if (name.equals("tags")) {
                readTags(jsonReader, listener);
            } else if (name.equals("app_tags")) {
                readAppTags(jsonReader, listener);
            }
        }

        jsonReader.endObject();
        reader.close();
        listener.onFinish();

        return new Container(apps, tags, appTags);
    }

    private void readAppTags(@NonNull JsonReader jsonReader, OnReadListener listener) throws IOException {
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            AppTag appTag = readAppTag(jsonReader);
            if (appTag!=null) {
                listener.onAppTagRead(appTag);
            }
        }
        jsonReader.endArray();
    }

    private AppTag readAppTag(@NonNull JsonReader reader) throws IOException {
        String appid = "";
        int tagid = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (key.equals("appId")) {
                appid = reader.nextString();
            } else if (key.equals("tagId")) {
                tagid = reader.nextInt();
            }
        }
        reader.endObject();

        if (tagid > 0){
            return new AppTag(appid, tagid);
        }
        return null;
    }

    private void readTags(@NonNull JsonReader jsonReader, OnReadListener listener) throws IOException {
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            Tag tag = readTag(jsonReader);
            if (tag!=null) {
                listener.onTagRead(tag);
            }
        }
        jsonReader.endArray();
    }

    private Tag readTag(JsonReader reader) throws IOException {
        int id = 0;
        int color = Tag.DEFAULT_COLOR;
        String name = "Tag name";

        reader.beginObject();
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (key.equals("id")) {
                id = reader.nextInt();
            } else if (key.equals("name")) {
                name = reader.nextString();
            } else if (key.equals("color")) {
                color = reader.nextInt();
            }
        }
        reader.endObject();

        if (id > 0){
            return new Tag(id ,name, color);
        }
        return null;
    }

    private void readAppList(@NonNull JsonReader jsonReader, OnReadListener listener) throws IOException {
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            AppInfo info = readAppInfo(jsonReader);
            if (info!=null) {
                listener.onAppRead(info);
            }
        }
        jsonReader.endArray();
    }

    private AppInfo readAppInfo(@NonNull JsonReader reader) throws IOException {
        String appId = null, pname = null, versionName = "", title = "", creator = "", uploadDate="", detailsUrl=null, iconUrl=null, appType = "";
        int versionNumber = 0, status = 0, syncVersion = 0;
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
