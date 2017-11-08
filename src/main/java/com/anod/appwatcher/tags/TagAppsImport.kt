package com.anod.appwatcher.tags

import android.content.Context
import android.database.Cursor
import android.support.v4.util.SimpleArrayMap
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.model.schema.AppTagsTable
import com.anod.appwatcher.framework.ApplicationContext

internal class TagAppsImport(private val tag: Tag, private val context: ApplicationContext) {

    constructor(tag: Tag, context: Context): this(tag, ApplicationContext(context))

    private val apps = SimpleArrayMap<String, Boolean>()
    private var defaultSelected: Boolean = false

    fun selectAll(select: Boolean) {
        apps.clear()
        defaultSelected = select
    }

    fun updateApp(appId: String, checked: Boolean) {
        apps.put(appId, checked)
    }

    fun isSelected(appId: String): Boolean {
        if (apps.containsKey(appId)) {
            return apps.get(appId)
        }
        return defaultSelected
    }

    fun initSelected(data: Cursor?) {
        if (data == null || data.count == 0) {
            return
        }
        data.moveToPosition(-1)
        while (data.moveToNext()) {
            val appId = data.getString(AppTagsTable.Projection.appId)
            apps.put(appId, true)
        }
        data.close()
    }

    fun run(): Boolean {
        val appIds = (0 until apps.size())
                .filter { apps.valueAt(it) }
                .map { apps.keyAt(it) }

        val cr = DbContentProviderClient(context)
        val result = cr.setAppsToTag(appIds, tag.id)
        cr.close()

        context.contentResolver.notifyChange(DbContentProvider.appsTagUri, null)
        return result
    }
}