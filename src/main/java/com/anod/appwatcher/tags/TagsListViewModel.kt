package com.anod.appwatcher.tags

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.database.AppTagsTable
import com.anod.appwatcher.database.TagsTable
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.AppInfo
import info.anodsplace.framework.os.BackgroundTask

class TagsListViewModel(application: Application): AndroidViewModel(application) {

    var appInfo: AppInfo? = null

    private val appComponent: AppComponent
        get() = getApplication<AppWatcherApplication>().appComponent

    val tags = appComponent.database.tags().observe()

    fun saveTag(tag: Tag) {
        BackgroundTask(object : BackgroundTask.Worker<Tag, Int>(tag) {
            override fun finished(result: Int) {}

            override fun run(param: Tag): Int {
                if (param.id > 0) {
                    appComponent.database.tags().update(param)
                    return param.id
                }
                return TagsTable.Queries.insert(param, appComponent.database).toInt()
            }
        }).execute()
    }

    fun deleteTag(tag: Tag) {
        BackgroundTask(object : BackgroundTask.Worker<Tag, Boolean>(tag) {
            override fun finished(result: Boolean) {}

            override fun run(param: Tag): Boolean {
                appComponent.database.tags().delete(tag)
                return true
            }
        }).execute()
    }

    fun appHasTag(tag: Tag): Boolean {
        val app = appInfo ?: return false
        return appComponent.database.appTags().appWithTag(app.appId, tag.id) != null
    }

    fun removeAppTag(tag: Tag) {
        val app = appInfo ?: return
        BackgroundTask(object : BackgroundTask.Worker<Tag, Int>(tag) {
            override fun finished(result: Int) {}

            override fun run(param: Tag): Int {
                return appComponent.database.appTags().delete(param.id, app.appId)
            }
        }).execute()
    }

    fun addAppTag(tag: Tag) {
        val app = appInfo ?: return
        BackgroundTask(object : BackgroundTask.Worker<Tag, Long>(tag) {
            override fun finished(result: Long) {}

            override fun run(param: Tag): Long {
                return AppTagsTable.Queries.insert(tag.id, app.appId, appComponent.database)
            }
        }).execute()
    }
}