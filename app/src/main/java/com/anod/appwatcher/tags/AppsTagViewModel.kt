package com.anod.appwatcher.tags

import androidx.lifecycle.AndroidViewModel
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.Application
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.app.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapConcat

/**
 * @author Alex Gavrishev
 * @date 19/04/2016.
 */
class AppsTagViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val context = ApplicationContext(getApplication<AppWatcherApplication>())
    val titleFilter = MutableStateFlow("")
    var tag = MutableStateFlow(Tag(""))

    private val database: AppsDatabase
        get() = Application.provide(context).database

    internal lateinit var tagAppsImport: TagAppsImport

    val apps = titleFilter.flatMapConcat { titleFilter ->
        val appsTable = database.apps()
        AppListTable.Queries.loadAppList(Preferences.SORT_NAME_ASC, titleFilter, appsTable)
    }

    val tags = tag.flatMapConcat { database.appTags().forTag(it.id) }

    suspend fun import() {
        tagAppsImport.run()
    }
}