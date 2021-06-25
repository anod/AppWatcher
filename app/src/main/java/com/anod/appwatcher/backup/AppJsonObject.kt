package com.anod.appwatcher.backup

import android.text.TextUtils
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.AppInfo
import info.anodsplace.applog.AppLog
import info.anodsplace.framework.json.JsonReader
import info.anodsplace.framework.json.JsonToken
import info.anodsplace.framework.json.JsonWriter

/**
 * @author Alex Gavrishev
 * @date 27/06/2017
 */
class AppJsonObject(val app: AppInfo?, val tags: List<String>) {

    constructor(app: AppInfo, tags: List<Tag>, writer: JsonWriter) : this(app, tags.map { it.name }) {
        write(app, tags, writer)
    }

    constructor(reader: JsonReader): this(read(reader))

    constructor(params: Pair<AppInfo?, List<String>>): this(params.first, params.second)

    companion object {
        fun write(app: AppInfo, tags: List<Tag>, writer: JsonWriter) {
            AppLog.d("Write app: " + app.appId)
            writer.beginObject()
            writer.name("id").value(app.appId)
            writer.name("packageName").value(app.packageName)
            writer.name("title").value(app.title)
            writer.name("creator").value(app.creator)
            writer.name("uploadDate").value(app.uploadDate)
            writer.name("versionName").value(app.versionName)
            writer.name("versionCode").value(app.versionNumber.toLong())
            writer.name("status").value(app.status.toLong())
            writer.name("detailsUrl").value(app.detailsUrl)
            writer.name("iconUrl").value(app.iconUrl)
            writer.name("uploadTime").value(app.uploadTime)
            writer.name("appType").value(app.appType)
            writer.name("refreshTimestamp").value(app.updateTime)

            val tagsWriter = writer.name("tags")
            tagsWriter.beginArray()
            tags.forEach {
                writer.value(it.name)
            }
            tagsWriter.endArray()

            writer.endObject()
        }

        fun read(reader: JsonReader): Pair<AppInfo?, List<String>> {
            var appId: String? = null
            var pname: String? = null
            var versionName = ""
            var title = ""
            var creator = ""
            var uploadDate = ""
            var detailsUrl = ""
            var iconUrl = ""
            var appType = ""
            var versionNumber = 0
            var status = 0
            var refreshTime: Long = 0
            var uploadTime: Long = 0
            val tags = mutableListOf<String>()
            val recentTime = AppListTable.recentTime

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
                } else if (name == "uploadTime") {
                    uploadTime = reader.nextLong()
                } else if (name == "appType") {
                    appType = if (isNull) "" else reader.nextString()
                } else if (name == "refreshTimestamp") {
                    refreshTime = if (isNull) 0 else reader.nextLong()
                } else if (name == "tags") {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        val tagName = reader.nextString()
                        tags.add(tagName)
                    }
                    reader.endArray()
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
                        title, creator, iconUrl, status, uploadDate, null, null, null,
                        detailsUrl, uploadTime, appType, refreshTime, uploadTime > recentTime)
                onUpgrade(info)
                return Pair(info, tags)
            }
            return Pair(null, listOf())
        }

        private fun onUpgrade(info: AppInfo) {
            if (TextUtils.isEmpty(info.detailsUrl)) {
                val packageName = info.packageName
                info.appId = packageName
                info.detailsUrl = AppInfo.createDetailsUrl(packageName)
            }
        }
    }
}