package com.anod.appwatcher.tags

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.database.TagsTable
import com.anod.appwatcher.database.entities.Tag
import info.anodsplace.framework.os.BackgroundTask

class TagsListViewModel(application: Application): AndroidViewModel(application) {

    private val appComponent: AppComponent
        get() = getApplication<AppWatcherApplication>().appComponent

    val tags = appComponent.database.tags().observe()

    fun saveTag(tag: Tag) {
        BackgroundTask(object : BackgroundTask.Worker<Tag, Int>(tag) {
            override fun finished(result: Int) {}

            override fun run(tag: Tag): Int {
                if (tag.id > 0) {
                    appComponent.database.tags().update(tag)
                    return tag.id
                }
                return TagsTable.Queries.insert(tag, appComponent.database).toInt()
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
}