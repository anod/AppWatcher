package com.anod.appwatcher.tags

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.TagsTable
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.map
import com.anod.appwatcher.utils.switchMap
import info.anodsplace.framework.os.BackgroundTask

typealias TagAppItem = Pair<Tag,Boolean>

class TagsListViewModel(application: Application): AndroidViewModel(application) {

    val appInfo = MutableLiveData<AppInfo?>()

    private val provide: AppComponent
        get() = getApplication<AppWatcherApplication>().appComponent

    val tagsAppItems: LiveData<List<TagAppItem>> = appInfo.switchMap { info ->
        return@switchMap provide.database.tags().observe().switchMap { tags ->
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
        BackgroundTask(object : BackgroundTask.Worker<Tag, Int>(tag) {
            override fun finished(result: Int) {}

            override fun run(param: Tag): Int {
                if (param.id > 0) {
                    provide.database.tags().update(param)
                    return param.id
                }
                return TagsTable.Queries.insert(param, provide.database).toInt()
            }
        }).execute()
    }

    fun deleteTag(tag: Tag) {
        BackgroundTask(object : BackgroundTask.Worker<Tag, Boolean>(tag) {
            override fun finished(result: Boolean) {}

            override fun run(param: Tag): Boolean {
                provide.database.tags().delete(tag)
                return true
            }
        }).execute()
    }

    fun removeAppTag(tag: Tag) {
        val app = appInfo.value ?: return
        BackgroundTask(object : BackgroundTask.Worker<Tag, Int>(tag) {
            override fun finished(result: Int) {}

            override fun run(param: Tag): Int {
                return provide.database.appTags().delete(param.id, app.appId)
            }
        }).execute()
    }

    fun addAppTag(tag: Tag) {
        val app = appInfo.value ?: return
        BackgroundTask(object : BackgroundTask.Worker<Tag, Long>(tag) {
            override fun finished(result: Long) {}

            override fun run(param: Tag): Long {
                return AppTagsTable.Queries.insert(tag.id, app.appId, provide.database)
            }
        }).execute()
    }
}