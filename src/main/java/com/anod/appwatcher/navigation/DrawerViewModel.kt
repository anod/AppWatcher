package com.anod.appwatcher.navigation

import android.accounts.Account
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.content.TagsContentProviderClient
import com.anod.appwatcher.database.entities.Tag
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.os.BackgroundTask

/**
 * @author Alex Gavrishev
 * @date 18/04/2018
 */

typealias TagCountList = List<Pair<Tag, Int>>

private class TagsUpdateObserver(private val viewModel: DrawerViewModel) : ContentObserver(Handler()) {

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        uri?.let {
            AppLog.d("onChange: $it")
            viewModel.updateTags()
        }
    }
}

class DrawerViewModel(application: Application) : AndroidViewModel(application) {

    private val observer = TagsUpdateObserver(this)

    init {
        application.contentResolver.registerContentObserver(DbContentProvider.tagsUri, true, observer)
        application.contentResolver.registerContentObserver(DbContentProvider.appsTagUri, true, observer)
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<AppWatcherApplication>().contentResolver.unregisterContentObserver(observer)
    }

    val lastUpdateTime =MutableLiveData<Long>()
    val tags= MutableLiveData<TagCountList>()
    val account = MutableLiveData<Account>()

    fun refreshLastUpdateTime() {
        val application = getApplication<AppWatcherApplication>()
        this.lastUpdateTime.value = application.appComponent.prefs.lastUpdateTime
    }

    fun updateTags() {
        BackgroundTask(object : BackgroundTask.Worker<Application, List<Pair<Tag, Int>>>(getApplication()) {
            override fun run(param: Application): List<Pair<Tag, Int>> {
                val tagsClient = TagsContentProviderClient(ApplicationContext(param))
                val counts = tagsClient.queryTagsAppsCounts()
                val cr = tagsClient.queryAll()
                val result: List<Pair<Tag, Int>> = cr.map { Pair(it, counts.get(it.id)) }
                cr.close()
                tagsClient.close()
                return result
            }

            override fun finished(result: List<Pair<Tag, Int>>) {
                this@DrawerViewModel.tags.value = result
            }
        }).execute()
    }
}