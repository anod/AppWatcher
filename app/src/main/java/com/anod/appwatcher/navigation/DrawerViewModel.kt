package com.anod.appwatcher.navigation

import android.accounts.Account
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.utils.combineLatest

/**
 * @author Alex Gavrishev
 * @date 18/04/2018
 */

typealias TagCountList = List<Pair<Tag, Int>>

class DrawerViewModel(application: Application) : AndroidViewModel(application) {

    private val appComponent: AppComponent
        get() = getApplication<AppWatcherApplication>().appComponent

    val lastUpdateTime =MutableLiveData<Long>()
    val tags: LiveData<TagCountList> = appComponent.database.appTags().queryCounts()
            .combineLatest(appComponent.database.tags().observe())
            .map { value ->
                val counts: Map<Int, Int> = value.first.associate { Pair(it.tagId, it.count) }
                val tags = value.second
                val result: TagCountList = tags.map { Pair(it, counts[it.id] ?: 0) }
                result
            }
    val account = MutableLiveData<Account>()

    fun refreshLastUpdateTime() {
        this.lastUpdateTime.value = appComponent.prefs.lastUpdateTime
    }

}