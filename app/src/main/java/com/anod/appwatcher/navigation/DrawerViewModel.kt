package com.anod.appwatcher.navigation

import android.accounts.Account
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.anod.appwatcher.AppComponent
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.database.entities.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * @author Alex Gavrishev
 * @date 18/04/2018
 */

typealias TagCountList = List<Pair<Tag, Int>>

class DrawerViewModel(application: Application) : AndroidViewModel(application) {

    private val appComponent: AppComponent
        get() = getApplication<AppWatcherApplication>().appComponent

    val lastUpdateTime = MutableLiveData<Long>()
    val tags: Flow<TagCountList> = appComponent.database.appTags().queryCounts()
            .combine(appComponent.database.tags().observe()) { counts, tags ->
                val tagCounts: Map<Int, Int> = counts.associate { Pair(it.tagId, it.count) }
                val result: TagCountList = tags.map { Pair(it, tagCounts[it.id] ?: 0) }
                result
            }
    val account = MutableLiveData<Account>()

    fun refreshLastUpdateTime() {
        this.lastUpdateTime.value = appComponent.prefs.lastUpdateTime
    }

}