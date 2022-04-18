package com.anod.appwatcher.tags

import androidx.lifecycle.AndroidViewModel
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.preferences.Preferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author Alex Gavrishev
 * @date 19/04/2016.
 */
class AppsTagViewModel(application: android.app.Application) : AndroidViewModel(application), KoinComponent {
    val titleFilter = MutableStateFlow("")
    var tag = MutableStateFlow(Tag(""))

    private val database: AppsDatabase by inject()

    internal lateinit var tagAppsImport: TagAppsImport

    val apps = titleFilter.flatMapLatest { titleFilter ->
        val appsTable = database.apps()
        AppListTable.Queries.loadAppList(Preferences.SORT_NAME_ASC, titleFilter, appsTable)
    }

    val tags = tag.flatMapLatest { database.appTags().forTag(it.id) }

    suspend fun import() {
        tagAppsImport.run()
    }
}