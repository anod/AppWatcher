package com.anod.appwatcher.utils

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <K: Any, V: Any> PagingSource<K, V>.asFlow(loadSize: Int = 100, placeholdersEnabled: Boolean = false): Flow<List<V>> = flow {
    var nextKey: K? = null
    var initialLoad = true
    while (nextKey != null || initialLoad) {
        val loadParams = if (initialLoad)
            PagingSource.LoadParams.Refresh<K>(key = null, loadSize, placeholdersEnabled)
        else
            PagingSource.LoadParams.Append<K>(key = nextKey!!, loadSize, placeholdersEnabled)
        initialLoad = false
        val nextResult = load(loadParams)
        if (nextResult is PagingSource.LoadResult.Page) {
            emit(nextResult.data)
            nextKey = nextResult.nextKey
        } else {
            nextKey = null
        }
    }
}