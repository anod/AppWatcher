package com.anod.appwatcher.tags

import android.app.Application
import androidx.lifecycle.*
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.TagsTable
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.AppInfo
import kotlinx.coroutines.launch

typealias TagAppItem = Pair<Tag,Boolean>

class TagsListViewModel(application: Application): AndroidViewModel(application) {

    val appInfo = MutableLiveData<AppInfo?>()

    private val provide: AppComponent
        get() = getApplication<AppWatcherApplication>().appComponent

    val tagsAppItems: LiveData<List<TagAppItem>> = appInfo.switchMap tagsApp@ { info ->
        return@tagsApp provide.database.tags().observe().switchMap { tags ->
            if (info == null || info.appId.isEmpty()) {
                return@switchMap MutableLiveData(tags.map { TagAppItem(it, false) })
            }
            return@switchMap provide.database.appTags().forApp(info.appId).map { appTags ->
                val appTagsList = appTags.map { it.tagId }
                tags.map { TagAppItem(it, appTagsList.contains(it.id)) }
            }
        }
    }

    fun saveTag(tag: Tag) {
        viewModelScope.launch {
            if (tag.id > 0) {
                provide.database.tags().update(tag)
            } else {
                TagsTable.Queries.insert(tag, provide.database).toInt()
            }
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            provide.database.tags().delete(tag)
        }
    }

    fun removeAppTag(tag: Tag) {
        val app = appInfo.value ?: return
        viewModelScope.launch {
            provide.database.appTags().delete(tag.id, app.appId)
        }
    }

    fun addAppTag(tag: Tag) {
        val app = appInfo.value ?: return
        viewModelScope.launch {
            AppTagsTable.Queries.insert(tag.id, app.appId, provide.database)
        }
    }
}