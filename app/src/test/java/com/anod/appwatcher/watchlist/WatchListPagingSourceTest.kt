package com.anod.appwatcher.watchlist

import org.junit.Assert.assertEquals
import org.junit.Test

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
            hasData = true,
        )
        assertEquals(null, prev)
        assertEquals(20, next)
    }

    @Test
    fun `calculateKeys middle page has prev and next`() {
        val (prev, next) = WatchListPagingSource.calculateKeys(
            key = 20,
            offset = 20,
            loadSize = 20,
            hasData = true,
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
            hasData = false,
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
        // Since key == offset and loadSize > PAGE_SIZE, this is treated as a refresh window
        // with no previous page (prevKey = null) and nextKey advancing by loadSize.
        val (firstPrev, firstNext) = WatchListPagingSource.calculateKeys(
            key = refreshKey,
            offset = refreshKey,
            loadSize = 60,
            hasData = true,
        )
        assertEquals(null, firstPrev)
        assertEquals(180, firstNext)
    }
}
