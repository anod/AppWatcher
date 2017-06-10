package com.anod.appwatcher.backup

import android.database.Cursor
import com.android.util.JsonWriter
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.content.TagsCursor
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.model.schema.AppTagsTable
import info.anodsplace.android.log.AppLog
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

        val appsCursor = client.queryAllSorted(true)
        writeAppList(writer.name("apps"), appsCursor)
        appsCursor.close()

        val tagsCursor = client.queryTags()
        writeTagsList(writer.name("tags"), tagsCursor)
        tagsCursor.close()

        val appTagsCursor = client.queryAppTags()
        writeAppTagsList(writer.name("app_tags"), appTagsCursor)
        appTagsCursor.close()

        writer.endObject()
        writer.close()
    }

    @Throws(IOException::class)
    private fun writeAppTagsList(writer: JsonWriter, cursor: Cursor) {
        writer.beginArray()
        cursor.moveToPosition(-1)
        while (cursor.moveToNext()) {
            writer.beginObject()
            writer.name("appId").value(cursor.getString(AppTagsTable.Projection.APPID))
            writer.name("tagId").value(cursor.getInt(AppTagsTable.Projection.TAGID).toLong())
            writer.endObject()
        }
        writer.endArray()
    }

    @Throws(IOException::class)
    private fun writeTagsList(writer: JsonWriter, cursor: TagsCursor) {
        writer.beginArray()
        cursor.moveToPosition(-1)
        while (cursor.moveToNext()) {
            writeTag(writer, cursor.tag)
        }
        writer.endArray()
    }

    @Throws(IOException::class)
    private fun writeTag(writer: JsonWriter, tag: Tag) {
        writer.beginObject()
        writer.name("id").value(tag.id.toLong())
        writer.name("name").value(tag.name)
        writer.name("color").value(tag.color.toLong())
        writer.endObject()
    }

    @Throws(IOException::class)
    private fun writeAppList(writer: JsonWriter, listCursor: AppListCursor) {
        writer.beginArray()
        listCursor.moveToPosition(-1)
        while (listCursor.moveToNext()) {
            writeApp(writer, listCursor.appInfo)
        }
        writer.endArray()
    }

    @Throws(IOException::class)
    private fun writeApp(writer: JsonWriter, appInfo: AppInfo) {
        AppLog.d("Write app: " + appInfo.appId)
        writer.beginObject()
        writer.name("id").value(appInfo.appId)
        writer.name("packageName").value(appInfo.packageName)
        writer.name("title").value(appInfo.title)
        writer.name("creator").value(appInfo.creator)
        writer.name("uploadDate").value(appInfo.uploadDate)
        writer.name("versionName").value(appInfo.versionName)
        writer.name("versionCode").value(appInfo.versionNumber.toLong())
        writer.name("status").value(appInfo.status.toLong())
        writer.name("detailsUrl").value(appInfo.detailsUrl)
        writer.name("iconUrl").value(appInfo.iconUrl)
        writer.name("refreshTime").value(appInfo.refreshTime)
        writer.name("appType").value(appInfo.appType)
        writer.name("syncVersion").value(appInfo.syncVersion.toLong())
        writer.endObject()
    }
}
