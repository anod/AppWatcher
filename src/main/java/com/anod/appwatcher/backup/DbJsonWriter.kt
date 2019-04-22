package com.anod.appwatcher.backup

import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.watchlist.AppsList
import info.anodsplace.framework.json.JsonWriter
import java.io.IOException
import java.io.Writer

/**
 * @author alex
 * *
 * @date 2015-02-27
 */
class DbJsonWriter {

    @Throws(IOException::class)
    fun write(file: Writer, db: AppsDatabase) {
        val writer = JsonWriter(file)
        writer.beginObject()

        val tags = db.tags().load().associate { it.id to it }
        val appsTags = mutableMapOf<String, MutableList<Int>>()
        db.appTags().load().forEach {
            if (appsTags[it.appId] == null) {
                appsTags[it.appId] = mutableListOf()
            }
            appsTags[it.appId]!!.add(it.tagId)
        }

        val appsCursor = AppListCursor(AppListTable.Queries.load(true, db.apps()))
        val appList = writer.name("apps")
        appList.beginArray()
        appsCursor.forEach { appInfo ->
            val appTags = appsTags[appInfo.appId]?.map { tags.getValue(it) } ?: listOf()
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
