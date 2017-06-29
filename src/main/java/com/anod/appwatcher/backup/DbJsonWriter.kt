package com.anod.appwatcher.backup

import com.android.util.JsonWriter
import com.anod.appwatcher.content.DbContentProviderClient
import java.io.IOException
import java.io.Writer

/**
 * @author alex
 * *
 * @date 2015-02-27
 */
class DbJsonWriter {

    @Throws(IOException::class)
    fun write(file: Writer, client: DbContentProviderClient) {
        val writer = JsonWriter(file)
        writer.beginObject()

        val tagsCursor = client.queryTags()
        val tags = tagsCursor.associate { it.id to it }
        tagsCursor.close()

        val appTagsCursor = client.queryAppTags()
        val appsTags = mutableMapOf<String, MutableList<Int>>()
        appTagsCursor.forEach {
            if (appsTags[it.appId] == null) {
                appsTags[it.appId] = mutableListOf<Int>()
            }
            appsTags[it.appId]!!.add(it.tagId)
        }
        appTagsCursor.close()

        val appsCursor = client.queryAllSorted(true)
        val appList = writer.name("apps")
        appList.beginArray()
        appsCursor.forEach {
            // TODO: tags[it]!!
            val appTags = appsTags[it.appId]?.map { tags[it]!! } ?: listOf()
            AppJsonObject(it, appTags, appList)
        }
        appList.endArray()
        appsCursor.close()

        val tagList = writer.name("tags")
        tagList.beginArray()
        tags.forEach({ TagJsonObject(it.value, tagList) })
        tagList.endArray()

        writer.endObject()
        writer.close()
    }
}
