package com.anod.appwatcher.backup

import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppTag
import com.anod.appwatcher.model.Tag
import info.anodsplace.framework.json.JsonReader
import info.anodsplace.framework.json.JsonToken
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
        fun onAppRead(app: AppInfo, tags: List<String>)
        fun onTagRead(tag: Tag)
        fun onFinish()
    }

    @Throws(IOException::class)
    fun read(reader: Reader, listener: OnReadListener) {
        val jsonReader = JsonReader(reader)

        if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
            readApps(jsonReader, listener)
            reader.close()
            return
        }

        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            val name = jsonReader.nextName()
            if (name == "apps") {
                readApps(jsonReader, listener)
            } else if (name == "tags") {
                readTags(jsonReader, listener)
            } else {
                jsonReader.skipValue()
            }
        }

        jsonReader.endObject()
        reader.close()
        listener.onFinish()
    }

    @Throws(IOException::class)
    internal fun read(reader: Reader): Container {
        val jsonReader = JsonReader(reader)

        val apps = mutableListOf<AppInfo>()
        val tagList = mutableListOf<Tag>()
        val appsTags = mutableMapOf<String, List<String>>()

        val listener = object : OnReadListener {
            override fun onAppRead(app: AppInfo, tags: List<String>) {
                apps.add(app)
                appsTags.put(app.appId, tags)
            }

            override fun onTagRead(tag: Tag) {
                tagList.add(tag)
            }

            override fun onFinish() { }
        }

        // Old format
        if (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
            readApps(jsonReader, listener)
            reader.close()
            listener.onFinish()

            return Container(apps, tagList, listOf())
        }

        jsonReader.beginObject()
        while (jsonReader.hasNext()) {
            val name = jsonReader.nextName()
            if (name == "apps") {
                readApps(jsonReader, listener)
            } else if (name == "tags") {
                readTags(jsonReader, listener)
            } else {
                jsonReader.skipValue()
            }
        }

        jsonReader.endObject()
        reader.close()
        listener.onFinish()

        val namedTags = tagList.associate { it.name to it }
        val appTagList = mutableListOf<AppTag>()
        appsTags.forEach({ (appId, tags) ->
            tags.forEach {
                namedTags[it]?.let { appTagList.add(AppTag(appId, it.id)) }
            }
        })

        return Container(apps, tagList, appTagList)
    }

    @Throws(IOException::class)
    private fun readTags(jsonReader: JsonReader, listener: OnReadListener) {
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
    private fun readApps(jsonReader: JsonReader, listener: OnReadListener) {
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
