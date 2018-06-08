package com.anod.appwatcher.tags

import android.content.Context
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.database.entities.AppTag
import com.anod.appwatcher.database.entities.Tag
import info.anodsplace.framework.app.ApplicationContext

internal class TagAppsImport(private val tag: Tag, private val context: ApplicationContext) {

    constructor(tag: Tag, context: Context): this(tag, ApplicationContext(context))

    private var apps = mutableMapOf<String, Boolean>()
    private var defaultSelected: Boolean = false

    fun selectAll(select: Boolean) {
        apps = mutableMapOf()
        defaultSelected = select
    }

    fun updateApp(appId: String, checked: Boolean) {
        apps[appId] = checked
    }

    fun isSelected(appId: String): Boolean {
        if (apps.containsKey(appId)) {
            return apps[appId] ?: false
        }
        return defaultSelected
    }

    fun initSelected(list: List<AppTag> ) {
        if (list.isEmpty()) {
            return
        }
        this.apps = list.map { it.appId to true }.toMap().toMutableMap()
    }

    fun run(): Boolean {
        val appIds = apps.filter { it.value }.keys.toList()

        val cr = DbContentProviderClient(context)
        val result = cr.setAppsToTag(appIds, tag.id)
        cr.close()

        context.contentResolver.notifyChange(DbContentProvider.appsTagUri, null)
        context.contentResolver.notifyChange(DbContentProvider.appsUri, null)
        return result
    }
}