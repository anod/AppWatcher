package com.anod.appwatcher.navigation

import android.accounts.Account
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.utils.prefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author Alex Gavrishev
 * @date 18/04/2018
 */

typealias TagCountList = List<Pair<Tag, Int>>

class DrawerViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    private val database: AppsDatabase by inject()

    val lastUpdateTime = MutableLiveData<Long>()
    val tags: Flow<TagCountList> = database.appTags().queryCounts()
            .combine(database.tags().observe()) { counts, tags ->
                val tagCounts: Map<Int, Int> = counts.associate { Pair(it.tagId, it.count) }
                val result: TagCountList = tags.map { Pair(it, tagCounts[it.id] ?: 0) }
                result
            }
    val account = MutableLiveData<Account>()

    fun refreshLastUpdateTime() {
        this.lastUpdateTime.value = prefs.lastUpdateTime
    }
}