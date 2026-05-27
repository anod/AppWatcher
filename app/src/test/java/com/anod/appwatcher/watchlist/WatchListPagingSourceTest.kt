// Copyright (c) 2026. Alex Gavrishev
package com.anod.appwatcher.watchlist

import androidx.paging.PagingState
import com.anod.appwatcher.model.Filters
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

// Unit tests for WatchListPagingSource helper logic.
class WatchListPagingSourceTest {

    @Test
    fun `getRefreshKey returns page-aligned offset`() {
        assertEquals(0, WatchListPagingSource.getRefreshKey(0))
        assertEquals(0, WatchListPagingSource.getRefreshKey(5))
        assertEquals(20, WatchListPagingSource.getRefreshKey(20))
        assertEquals(20, WatchListPagingSource.getRefreshKey(39))
        assertEquals(40, WatchListPagingSource.getRefreshKey(40))
    }

    @Test
    fun `calculateOffsetAndLimit first page with recent header reduces limit`() {
        val (offset, limit) = WatchListPagingSource.calculateOffsetAndLimit(
            key = null,
            loadSize = 20,
            showRecentlyInstalled = true,
        )
        assertEquals(0, offset)
        assertEquals(19, limit)
    }

    @Test
    fun `calculateOffsetAndLimit first page without recent header keeps limit`() {
        val (offset, limit) = WatchListPagingSource.calculateOffsetAndLimit(
            key = null,
            loadSize = 20,
            showRecentlyInstalled = false,
        )
        assertEquals(0, offset)
        assertEquals(20, limit)
    }

    @Test
    fun `calculateOffsetAndLimit subsequent page ignores header and keeps limit`() {
        val (offset, limit) = WatchListPagingSource.calculateOffsetAndLimit(
            key = 20,
            loadSize = 20,
            showRecentlyInstalled = true,
        )
        assertEquals(20, offset)
        assertEquals(20, limit)
    }

    @Test
    fun `calculateKeys first page no previous and has next`() {
        val (prev, next) = WatchListPagingSource.calculateKeys(
            key = null,
            offset = 0,
            loadSize = 20,
            loadedDataSize = 20,
        )
        assertEquals(null, prev)
        assertEquals(20, next)
    }

    @Test
    fun `calculateKeys first page with recent header advances by reduced limit`() {
        val (prev, next) = WatchListPagingSource.calculateKeys(
            key = null,
            offset = 0,
            loadSize = 20,
            loadedDataSize = 19,
            limit = 19,
        )
        assertEquals(null, prev)
        assertEquals(19, next)
    }

    @Test
    fun `calculateKeys middle page has prev and next`() {
        val (prev, next) = WatchListPagingSource.calculateKeys(
            key = 20,
            offset = 20,
            loadSize = 20,
            loadedDataSize = 20,
        )
        // Single-page load with key != null, offset == loadSize: prevKey points to 0, nextKey advances by loadSize.
        assertEquals(0, prev)
        assertEquals(40, next)
    }

    @Test
    fun `calculateKeys last page has prev but no next when no data`() {
        val (prev, next) = WatchListPagingSource.calculateKeys(
            key = 40,
            offset = 40,
            loadSize = 20,
            loadedDataSize = 0,
        )
        // When there is no more data, nextKey is null but prevKey still steps back by loadSize.
        assertEquals(20, prev)
        assertEquals(null, next)
    }

    @Test
    fun `scroll and restore uses refresh key page-aligned to last visible position`() {
        // User scrolls down through multiple pages; assume last visible item is around index 125.
        val lastVisibleIndex = 125
        val refreshKey = WatchListPagingSource.getRefreshKey(lastVisibleIndex)
        assertEquals(120, refreshKey)

        // Simulate initial load after navigating back with a large loadSize used by Paging.
        // Since this is a deep refresh window, prevKey must still allow prepending earlier pages.
        val (firstPrev, firstNext) = WatchListPagingSource.calculateKeys(
            key = refreshKey,
            offset = refreshKey,
            loadSize = 60,
            loadedDataSize = 60,
        )
        assertEquals(60, firstPrev)
        assertEquals(180, firstNext)
    }

    @Test
    fun `calculateKeys deep multi-page refresh keeps previous key`() {
        val (prev, next) = WatchListPagingSource.calculateKeys(
            key = 80,
            offset = 80,
            loadSize = 60,
            loadedDataSize = 60,
        )
        assertEquals(20, prev)
        assertEquals(140, next)
    }

    @Test
    fun `calculateKeys partial last page has no next`() {
        val (prev, next) = WatchListPagingSource.calculateKeys(
            key = 120,
            offset = 120,
            loadSize = 60,
            loadedDataSize = 15,
        )
        assertEquals(60, prev)
        assertEquals(null, next)
    }

    @Test
    fun `getRefreshKey accounts for recent header`() {
        assertEquals(0, WatchListPagingSource.getRefreshKey(1, showRecentlyInstalled = true))
        assertEquals(19, WatchListPagingSource.getRefreshKey(20, showRecentlyInstalled = true))
        assertEquals(19, WatchListPagingSource.getRefreshKey(21, showRecentlyInstalled = true))
        assertEquals(39, WatchListPagingSource.getRefreshKey(40, showRecentlyInstalled = true))
    }

    @Test
    fun `calculateItemsBefore accounts for recent header after first page`() {
        assertEquals(0, WatchListPagingSource.calculateItemsBefore(0, showRecentlyInstalled = true))
        assertEquals(20, WatchListPagingSource.calculateItemsBefore(19, showRecentlyInstalled = true))
        assertEquals(40, WatchListPagingSource.calculateItemsBefore(39, showRecentlyInstalled = true))
        assertEquals(20, WatchListPagingSource.calculateItemsBefore(20, showRecentlyInstalled = false))
    }

    @Test
    fun `pager factory invalidates active paging source`() {
        val source = TestPagingSource()
        var invalidated = false
        source.registerInvalidatedCallback {
            invalidated = true
        }

        val factory = TestPagerFactory()
        factory.attach(source)
        factory.invalidatePagingSource()

        assertEquals(true, invalidated)
    }

    private class TestPagerFactory : WatchListPagerFactory(
        pagingSourceConfig = WatchListPagingSource.Config(
            filterId = Filters.ALL,
            tagId = null,
            showRecentlyDiscovered = false,
            showOnDevice = false,
            showRecentlyInstalled = false,
        ),
        cacheScope = CoroutineScope(EmptyCoroutineContext)
    ) {
        fun attach(source: TestPagingSource) {
            pagingSource = source
        }

        override fun createPagingSource(): FilterablePagingSource = TestPagingSource()

        override fun createSectionHeaderFactory(): SectionHeaderFactory = SectionHeaderFactory.Empty()
    }

    private class TestPagingSource : FilterablePagingSource() {
        override var filterQuery: String = ""

        override fun getRefreshKey(state: PagingState<Int, SectionItem>): Int? = null

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SectionItem> {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        }
    }
}
