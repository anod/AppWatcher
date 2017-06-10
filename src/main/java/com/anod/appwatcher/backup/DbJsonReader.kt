package com.anod.appwatcher.backup

import android.text.TextUtils
import com.android.util.JsonReader
import com.android.util.JsonToken
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppTag
import com.anod.appwatcher.model.Tag
import java.io.IOException
import java.io.Reader
import java.util.*

/**
 * @author alex
 * *
 * @date 2015-02-27
 */
class DbJsonReader {

    internal class Container(val apps: List<AppInfo>, val tags: List<Tag>, val appTags: List<AppTag>)

    interface OnReadListener {
        fun onAppRead(app: AppInfo)
        fun onTagRead(tag: Tag)
        fun onAppTagRead(appTag: AppTag)
        @Throws(IOException::class)
        fun onFinish()
    }

    @Throws(IOException::class)
    fun read(reader: Reader, listener: OnReadListener) {
        val jsonReader = JsonReader(reader)

        if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
            readAppList(jsonReader, listener)
            reader.close()
            return
        }

        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            val name = jsonReader.nextName()
            if (name == "apps") {
                readAppList(jsonReader, listener)
            } else if (name == "tags") {
                readTags(jsonReader, listener)
            } else if (name == "app_tags") {
                readAppTags(jsonReader, listener)
            }
        }

        jsonReader.endObject()
        reader.close()
    }

    @Throws(IOException::class)
    internal fun read(reader: Reader): Container {
        val jsonReader = JsonReader(reader)

        val apps = ArrayList<AppInfo>()
        val tags = ArrayList<Tag>()
        val appTags = ArrayList<AppTag>()

        val listener = object : OnReadListener {
            override fun onAppRead(app: AppInfo) {
                apps.add(app)
            }

            override fun onTagRead(tag: Tag) {
                tags.add(tag)
            }

            override fun onAppTagRead(appTag: AppTag) {
                appTags.add(appTag)
            }

            override fun onFinish() {}
        }

        if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
            readAppList(jsonReader, listener)
            reader.close()
            listener.onFinish()

            return Container(apps, tags, appTags)
        }


        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            val name = jsonReader.nextName()
            if (name == "apps") {
                readAppList(jsonReader, listener)
            } else if (name == "tags") {
                readTags(jsonReader, listener)
            } else if (name == "app_tags") {
                readAppTags(jsonReader, listener)
            }
        }

        jsonReader.endObject()
        reader.close()
        listener.onFinish()

        return Container(apps, tags, appTags)
    }

    @Throws(IOException::class)
    private fun readAppTags(jsonReader: JsonReader, listener: OnReadListener) {
        jsonReader.beginArray()
        while (jsonReader.hasNext()) {
            val appTag = readAppTag(jsonReader)
            if (appTag != null) {
                listener.onAppTagRead(appTag)
            }
        }
        jsonReader.endArray()
    }

    @Throws(IOException::class)
    private fun readAppTag(reader: JsonReader): AppTag? {
        var appid = ""
        var tagid = 0

        reader.beginObject()
        while (reader.hasNext()) {
            val key = reader.nextName()
            if (key == "appId") {
                appid = reader.nextString()
            } else if (key == "tagId") {
                tagid = reader.nextInt()
            }
        }
        reader.endObject()

        if (tagid > 0) {
            return AppTag(appid, tagid)
        }
        return null
    }

    @Throws(IOException::class)
    private fun readTags(jsonReader: JsonReader, listener: OnReadListener) {
        jsonReader.beginArray()
        while (jsonReader.hasNext()) {
            val tag = readTag(jsonReader)
            if (tag != null) {
                listener.onTagRead(tag)
            }
        }
        jsonReader.endArray()
    }

    @Throws(IOException::class)
    private fun readTag(reader: JsonReader): Tag? {
        var id = 0
        var color = Tag.DEFAULT_COLOR
        var name = "Tag name"

        reader.beginObject()
        while (reader.hasNext()) {
            val key = reader.nextName()
            if (key == "id") {
                id = reader.nextInt()
            } else if (key == "name") {
                name = reader.nextString()
            } else if (key == "color") {
                color = reader.nextInt()
            }
        }
        reader.endObject()

        if (id > 0) {
            return Tag(id, name, color)
        }
        return null
    }

    @Throws(IOException::class)
    private fun readAppList(jsonReader: JsonReader, listener: OnReadListener) {
        jsonReader.beginArray()
        while (jsonReader.hasNext()) {
            val info = readAppInfo(jsonReader)
            if (info != null) {
                listener.onAppRead(info)
            }
        }
        jsonReader.endArray()
    }

    @Throws(IOException::class)
    private fun readAppInfo(reader: JsonReader): AppInfo? {
        var appId: String? = null
        var pname: String? = null
        var versionName = ""
        var title = ""
        var creator = ""
        var uploadDate = ""
        var detailsUrl: String = ""
        var iconUrl: String = ""
        var appType = ""
        var versionNumber = 0
        var status = 0
        var syncVersion = 0
        var refreshTime: Long = 0

        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            val isNull = reader.peek() == JsonToken.NULL
            var skipped = false
            if (name == "id") {
                appId = if (isNull) null else reader.nextString()
            } else if (name == "packageName") {
                pname = if (isNull) null else reader.nextString()
            } else if (name == "title" && reader.peek() != JsonToken.NULL) {
                title = if (isNull) "" else reader.nextString()
            } else if (name == "creator") {
                creator = if (isNull) "" else reader.nextString()
            } else if (name == "uploadDate") {
                uploadDate = if (isNull) "" else reader.nextString()
            } else if (name == "versionName") {
                versionName = if (isNull) "" else reader.nextString()
            } else if (name == "versionCode") {
                versionNumber = if (isNull) 0 else reader.nextInt()
            } else if (name == "status") {
                status = if (isNull) 0 else reader.nextInt()
            } else if (name == "detailsUrl") {
                detailsUrl = if (isNull) "" else reader.nextString()
            } else if (name == "iconUrl") {
                iconUrl = if (isNull) "" else reader.nextString()
            } else if (name == "refreshTime") {
                refreshTime = reader.nextLong()
            } else if (name == "appType") {
                appType = if (isNull) "" else reader.nextString()
            } else if (name == "syncVersion") {
                syncVersion = if (isNull) 0 else reader.nextInt()
            } else {
                skipped = true
                reader.skipValue()
            }
            if (isNull && !skipped) {
                reader.nextNull()
            }
        }
        reader.endObject()
        if (appId != null && pname != null) {
            val info = AppInfo(0, appId, pname, versionNumber, versionName,
                    title, creator, iconUrl, status, uploadDate, null, null, null, detailsUrl, refreshTime, appType, syncVersion)
            onUpgrade(info)
            return info
        }
        return null
    }


    private fun onUpgrade(info: AppInfo) {
        if (TextUtils.isEmpty(info.detailsUrl)) {
            val packageName = info.packageName
            info.appId = packageName
            info.detailsUrl = AppInfo.createDetailsUrl(packageName)
        }
    }
}
