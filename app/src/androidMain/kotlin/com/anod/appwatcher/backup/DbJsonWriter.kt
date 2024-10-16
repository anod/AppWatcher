package com.anod.appwatcher.backup

import androidx.paging.PagingSource
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.utils.asFlow
import info.anodsplace.framework.json.JsonWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
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

        val tags = db.tags().load().associateBy { it.id }
        val appsTags = mutableMapOf<String, MutableList<TagId>>()
        db.appTags().load().forEach {
            if (appsTags[it.appId] == null) {
                appsTags[it.appId] = mutableListOf()
            }
            appsTags[it.appId]!!.add(it.tagId)
        }

        val appList = writer.name("apps")
        appList.beginArray()

        val pagingSource = db.apps().load(includeDeleted = true)
        pagingSource.asFlow()
            .onEach { result ->
                result.forEach { appInfo ->
                    val appTags = appsTags[appInfo.app.appId]?.mapNotNull {
                        tags[it]
                    } ?: listOf()
                    AppJsonObject(appInfo.app, appTags, appList)
                }
            }
            .collect()
        appList.endArray()

        val tagList = writer.name("tags")
        tagList.beginArray()
        tags.forEach { TagJsonObject(it.value, tagList) }
        tagList.endArray()

        writer.endObject()
        writer.close()
    }
}
