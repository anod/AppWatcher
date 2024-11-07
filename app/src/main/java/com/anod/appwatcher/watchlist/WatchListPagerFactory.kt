package com.anod.appwatcher.watchlist

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.insertSeparators
import com.anod.appwatcher.installed.InstalledPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Alex Gavrishev
 * @date 13/04/2018
 */

abstract class FilterablePagingSource : PagingSource<Int, SectionItem>() {
    abstract var filterQuery: String
}

abstract class WatchListPagerFactory(val pagingSourceConfig: WatchListPagingSource.Config) {
    var filterQuery: String = ""
        set(value) {
            field = value
            (pagingSource as? InstalledPagingSource)?.filterQuery = value
        }

    protected var pagingSource: FilterablePagingSource? = null
    private var _pagingData: Flow<PagingData<SectionItem>>? = null
    val pagingData: Flow<PagingData<SectionItem>>
        get() {
            if (_pagingData == null) {
                _pagingData = createPager()
            }
            return _pagingData!!
        }

    private lateinit var headerFactory: SectionHeaderFactory

    abstract fun createPagingSource(): FilterablePagingSource
    abstract fun createSectionHeaderFactory(): SectionHeaderFactory

    private fun createPager(): Flow<PagingData<SectionItem>> {
        headerFactory = createSectionHeaderFactory()

        // When initialLoadSize larger than pageSize it cause a bug
        // where after filter if there is only one pages items are shown multiple times
        return Pager(
            config = PagingConfig(
                pageSize = WatchListPagingSource.pageSize,
                enablePlaceholders = false,
                initialLoadSize = WatchListPagingSource.pageSize,
                maxSize = 1000
            ),
            initialKey = null,
            pagingSourceFactory = {
                createPagingSource().also {
                    pagingSource = it
                }
            }
        )
            .flow
            .map { pagingData: PagingData<SectionItem> ->
                pagingData.insertSeparators { before, after ->
                    headerFactory.insertSeparator(before, after)
                }
            }
    }
}