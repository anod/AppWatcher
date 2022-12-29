package com.anod.appwatcher.tags

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.TagsTable
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.AppInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

typealias TagAppItem = Pair<Tag,Boolean>

class TagsViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    private val database: AppsDatabase by inject()
    val appInfo = MutableStateFlow<AppInfo?>(null)

    val tagsAppItems: Flow<List<TagAppItem>> = appInfo.flatMapLatest tagsApp@{ info ->
        return@tagsApp database.tags().observe().flatMapLatest { tags ->
            if (info == null || info.appId.isEmpty()) {
                return@flatMapLatest flowOf(tags.map { TagAppItem(it, false) })
            }
            return@flatMapLatest database.appTags().forApp(info.appId).map { appTags ->
                val appTagsList = appTags.map { it.tagId }
                tags.map { TagAppItem(it, appTagsList.contains(it.id)) }
            }
        }
    }

    fun removeAppTag(tag: Tag) {
        val app = appInfo.value ?: return
        viewModelScope.launch {
            database.appTags().delete(tag.id, app.appId)
        }
    }

    fun addAppTag(tag: Tag) {
        val app = appInfo.value ?: return
        viewModelScope.launch {
            AppTagsTable.Queries.insert(tag.id, app.appId, database)
        }
    }
}