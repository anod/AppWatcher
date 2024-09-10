package com.anod.appwatcher.backup

import com.anod.appwatcher.database.entities.App
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

    internal class Container(val apps: List<App>, val tags: List<Tag>, val appTags: Map<String, List<String>>)

    interface OnReadListener {
        suspend fun onAppRead(app: App, tags: List<String>)
        suspend fun onTagRead(tag: Tag)
        suspend fun onFinish(appsRead: Int, tagsRead: Int)
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
        var appsRead = 0
        var tagsRead = 0
        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "apps" -> {
                    appsRead = readApps(jsonReader, listener)
                }
                "tags" -> {
                    tagsRead = readTags(jsonReader, listener)
                }
                else -> jsonReader.skipValue()
            }
        }

        jsonReader.endObject()
        reader.close()
        listener.onFinish(appsRead, tagsRead)
    }

    @Throws(IOException::class)
    internal suspend fun read(reader: Reader): Container = withContext(Dispatchers.IO) {
        val jsonReader = JsonReader(reader)

        val apps = mutableListOf<App>()
        val tagList = mutableListOf<Tag>()
        val appsTags = mutableMapOf<String, List<String>>()

        val listener = object : OnReadListener {
            override suspend fun onAppRead(app: App, tags: List<String>) {
                apps.add(app)
                appsTags[app.appId] = tags
            }

            override suspend fun onTagRead(tag: Tag) {
                tagList.add(tag)
            }

            override suspend fun onFinish(appsRead: Int, tagsRead: Int) {}

        }

        // Old format
        if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
            readApps(jsonReader, listener)
            reader.close()
            listener.onFinish(-1, -1)

            return@withContext Container(apps, tagList, mapOf())
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
        listener.onFinish(-1, -1)

        return@withContext Container(apps, tagList, appsTags)
    }

    @Throws(IOException::class)
    private suspend fun readTags(jsonReader: JsonReader, listener: OnReadListener): Int {
        jsonReader.beginArray()
        var count = 0
        while (jsonReader.hasNext()) {
            val tag = TagJsonObject(jsonReader).tag
            if (tag != null) {
                listener.onTagRead(tag)
                count++
            }
        }
        jsonReader.endArray()
        return count
    }


    @Throws(IOException::class)
    private suspend fun readApps(jsonReader: JsonReader, listener: OnReadListener): Int {
        jsonReader.beginArray()
        var count = 0
        while (jsonReader.hasNext()) {
            val json = AppJsonObject(jsonReader)
            json.app?.let {
                listener.onAppRead(it, json.tags)
                count++
            }
        }
        jsonReader.endArray()
        return count
    }
}
