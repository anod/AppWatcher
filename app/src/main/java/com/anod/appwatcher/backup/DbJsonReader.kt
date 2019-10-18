package com.anod.appwatcher.backup

import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.entities.Tag
import info.anodsplace.framework.json.JsonReader
import info.anodsplace.framework.json.JsonToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.Reader

/**
 * @author alex
 * *
 * @date 2015-02-27
 */
class DbJsonReader {

    internal class Container(val apps: List<AppInfo>, val tags: List<Tag>, val appTags: List<AppTag>)

    interface OnReadListener {
        suspend fun onAppRead(app: AppInfo, tags: List<String>)
        suspend fun onTagRead(tag: Tag)
        suspend fun onFinish()
    }

    @Throws(IOException::class)
    suspend fun read(reader: Reader, listener: OnReadListener) = withContext(Dispatchers.IO) {
        val jsonReader = JsonReader(reader)

        if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
            readApps(jsonReader, listener)
            reader.close()
            return@withContext
        }

        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "apps" -> readApps(jsonReader, listener)
                "tags" -> readTags(jsonReader, listener)
                else -> jsonReader.skipValue()
            }
        }

        jsonReader.endObject()
        reader.close()
        listener.onFinish()
    }

    @Throws(IOException::class)
    internal suspend fun read(reader: Reader): Container = withContext(Dispatchers.IO) {
        val jsonReader = JsonReader(reader)

        val apps = mutableListOf<AppInfo>()
        val tagList = mutableListOf<Tag>()
        val appsTags = mutableMapOf<String, List<String>>()

        val listener = object : OnReadListener {
            override suspend fun onAppRead(app: AppInfo, tags: List<String>) {
                apps.add(app)
                appsTags[app.appId] = tags
            }

            override suspend fun onTagRead(tag: Tag) {
                tagList.add(tag)
            }

            override suspend fun onFinish() { }
        }

        // Old format
        if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
            readApps(jsonReader, listener)
            reader.close()
            listener.onFinish()

            return@withContext Container(apps, tagList, listOf())
        }

        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "apps" -> readApps(jsonReader, listener)
                "tags" -> readTags(jsonReader, listener)
                else -> jsonReader.skipValue()
            }
        }

        jsonReader.endObject()
        reader.close()
        listener.onFinish()

        val namedTags = tagList.associate { it.name to it }
        val appTagList = mutableListOf<AppTag>()
        appsTags.forEach { (appId, tags) ->
            tags.forEach { tag ->
                namedTags[tag]?.let { appTagList.add(AppTag(appId, it.id)) }
            }
        }

        return@withContext Container(apps, tagList, appTagList)
    }

    @Throws(IOException::class)
    private suspend fun readTags(jsonReader: JsonReader, listener: OnReadListener) {
        jsonReader.beginArray()
        while (jsonReader.hasNext()) {
            val tag = TagJsonObject(jsonReader).tag
            if (tag != null) {
                listener.onTagRead(tag)
            }
        }
        jsonReader.endArray()
    }


    @Throws(IOException::class)
    private suspend fun readApps(jsonReader: JsonReader, listener: OnReadListener) {
        jsonReader.beginArray()
        while (jsonReader.hasNext()) {
            val json = AppJsonObject(jsonReader)
            json.app?.let {
                listener.onAppRead(it, json.tags)
            }
        }
        jsonReader.endArray()
    }
}
