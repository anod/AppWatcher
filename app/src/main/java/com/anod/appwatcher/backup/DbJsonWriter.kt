package com.anod.appwatcher.backup

import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import info.anodsplace.framework.json.JsonWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.Writer

/**
 * @author alex
 * *
 * @date 2015-02-27
 */

typealias TagId = Int

class DbJsonWriter {

    @Throws(IOException::class)
    suspend fun write(file: Writer, db: AppsDatabase) = withContext(Dispatchers.IO) {
        val writer = JsonWriter(file)
        writer.beginObject()

        val tags = db.tags().load().associate { it.id to it }
        val appsTags = mutableMapOf<String, MutableList<TagId>>()
        db.appTags().load().forEach {
            if (appsTags[it.appId] == null) {
                appsTags[it.appId] = mutableListOf()
            }
            appsTags[it.appId]!!.add(it.tagId)
        }

        val appsCursor = AppListTable.Queries.load(true, db.apps())
        val appList = writer.name("apps")
        appList.beginArray()
        appsCursor.forEach { appInfo ->
            val appTags = appsTags[appInfo.appId]?.mapNotNull {
                tags[it]
            } ?: listOf()
            AppJsonObject(appInfo, appTags, appList)
        }
        appList.endArray()
        appsCursor.close()

        val tagList = writer.name("tags")
        tagList.beginArray()
        tags.forEach { TagJsonObject(it.value, tagList) }
        tagList.endArray()

        writer.endObject()
        writer.close()
    }
}
