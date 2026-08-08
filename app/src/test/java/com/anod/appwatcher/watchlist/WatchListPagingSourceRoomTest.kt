package com.anod.appwatcher.watchlist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.notification.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WatchListPagingSourceRoomTest {

    private lateinit var context: Context
    private lateinit var db: AppsDatabase

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun showOnDeviceAppendsInstalledAppsAtEndOfFirstPage() = runBlocking {
        insertApp(appId = "watched", packageName = "local.only.watched", title = "Local Only Watched")
        installPackage(packageName = "local.only.watched", title = "Local Only Watched")
        installPackage(packageName = "local.only.device", title = "Local Only Device")

        val pagingSource = createPagingSource(showOnDevice = true).also {
            it.filterQuery = "Local Only"
        }

        val result = pagingSource.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        val page = result as PagingSource.LoadResult.Page

        assertEquals(listOf("local.only.watched"), page.data.filterIsInstance<SectionItem.App>().map { it.appListItem.app.packageName })
        assertEquals(listOf("local.only.device"), page.data.filterIsInstance<SectionItem.OnDevice>().map { it.appListItem.app.packageName })
        assertEquals(null, page.nextKey)
        assertEquals(PagingSource.LoadResult.Page.COUNT_UNDEFINED, page.itemsBefore)
        assertEquals(PagingSource.LoadResult.Page.COUNT_UNDEFINED, page.itemsAfter)
    }

    @Test
    fun showOnDeviceUsesUndefinedCountSoExactPageBoundaryCanLoadInstalledApps() = runBlocking {
        repeat(20) { index ->
            insertApp(appId = "boundary-$index", packageName = "boundary.watched.$index", title = "Boundary Watched $index")
        }
        installPackage(packageName = "boundary.device", title = "Boundary Device")

        val pagingSource = createPagingSource(showOnDevice = true).also {
            it.filterQuery = "Boundary"
        }

        val firstResult = pagingSource.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        val firstPage = firstResult as PagingSource.LoadResult.Page

        assertEquals(20, firstPage.data.filterIsInstance<SectionItem.App>().size)
        assertFalse(firstPage.data.any { it is SectionItem.OnDevice })
        assertEquals(20, firstPage.nextKey)
        assertEquals(PagingSource.LoadResult.Page.COUNT_UNDEFINED, firstPage.itemsAfter)

        val secondResult = pagingSource.load(PagingSource.LoadParams.Append(key = 20, loadSize = 20, placeholdersEnabled = false))
        val secondPage = secondResult as PagingSource.LoadResult.Page

        assertTrue(secondPage.data.none { it is SectionItem.App })
        assertEquals(listOf("boundary.device"), secondPage.data.filterIsInstance<SectionItem.OnDevice>().map { it.appListItem.app.packageName })
        assertEquals(null, secondPage.nextKey)
    }

    @Test
    fun pagingSourceKeepsStableRowsWhenStatusChangesMoveItemsAcrossOffsets() = runBlocking {
        repeat(10) { index ->
            insertApp(
                appId = "updated-$index",
                packageName = "updated.$index",
                title = "Zzz Moved Later $index",
                status = App.STATUS_UPDATED
            )
        }
        repeat(100) { index ->
            insertApp(appId = "normal-$index", packageName = "normal.$index", title = "Normal $index")
        }
        val pagingSource = createPagingSource(showOnDevice = false)

        val firstResult = pagingSource.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 60, placeholdersEnabled = false))
        val firstPage = firstResult as PagingSource.LoadResult.Page
        firstPage.data
            .filterIsInstance<SectionItem.App>()
            .filter { it.appListItem.app.status == App.STATUS_UPDATED }
            .take(5)
            .forEach {
                db.apps().updateStatus(it.appListItem.app.rowId, App.STATUS_NORMAL)
            }
        val unloadedRow = AppListTable.Queries.loadAppListRows(
            sortId = Preferences.SORT_NAME_ASC,
            orderByRecentlyDiscovered = false,
            tagId = null,
            titleFilter = "",
            table = db.apps()
        )[70]
        db.apps().updateStatus(unloadedRow.rowId, App.STATUS_UPDATED)

        val pages = mutableListOf(firstPage)
        var nextKey = firstPage.nextKey
        while (nextKey != null) {
            val result = pagingSource.load(PagingSource.LoadParams.Append(key = nextKey, loadSize = 20, placeholdersEnabled = false))
            val page = result as PagingSource.LoadResult.Page
            pages.add(page)
            nextKey = page.nextKey
        }
        val items = pages.flatMap { it.data }.filterIsInstance<SectionItem.App>()
        val itemsWithHeaders = insertHeaders(items)

        assertEquals(110, items.size)
        assertEquals(items.map { it.sectionKey }.toSet().size, items.size)
        assertEquals(App.STATUS_NORMAL, items.single { it.appListItem.app.rowId == unloadedRow.rowId }.appListItem.app.status)
        assertEquals(itemsWithHeaders.map { it.sectionKey }.toSet().size, itemsWithHeaders.size)
        assertEquals(1, itemsWithHeaders.count { it.sectionKey == "header:watching" })
    }

    @Test
    fun pagingSourceDoesNotRenderRowsDeletedAfterSnapshot() = runBlocking {
        repeat(40) { index ->
            insertApp(appId = "app-$index", packageName = "app.$index", title = "App $index")
        }
        val pagingSource = createPagingSource(showOnDevice = false)

        val firstResult = pagingSource.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        val firstPage = firstResult as PagingSource.LoadResult.Page
        val secondPageRow = AppListTable.Queries.loadAppListRows(
            sortId = Preferences.SORT_NAME_ASC,
            orderByRecentlyDiscovered = false,
            tagId = null,
            titleFilter = "",
            table = db.apps()
        )[20]
        db.apps().updateStatus(secondPageRow.rowId, App.STATUS_DELETED)

        val secondResult = pagingSource.load(PagingSource.LoadParams.Append(key = firstPage.nextKey!!, loadSize = 20, placeholdersEnabled = false))
        val secondPage = secondResult as PagingSource.LoadResult.Page

        assertTrue(secondPage.data.filterIsInstance<SectionItem.App>().none { it.appListItem.app.rowId == secondPageRow.rowId })
        assertEquals(PagingSource.LoadResult.Page.COUNT_UNDEFINED, secondPage.itemsBefore)
        assertEquals(PagingSource.LoadResult.Page.COUNT_UNDEFINED, secondPage.itemsAfter)
    }

    @Test
    fun pagingSourceKeepsRecentSectionsStableWhenUnloadedRowChanges() = runBlocking {
        repeat(5) { index ->
            insertApp(
                appId = "recent-$index",
                packageName = "recent.$index",
                title = "Recent $index",
                syncTime = System.currentTimeMillis()
            )
        }
        repeat(35) { index ->
            insertApp(appId = "old-$index", packageName = "old.$index", title = "Old $index")
        }
        val pagingSource = createPagingSource(
            showOnDevice = false,
            showRecentlyDiscovered = true,
        )

        val firstResult = pagingSource.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        val firstPage = firstResult as PagingSource.LoadResult.Page
        val unloadedRow = AppListTable.Queries.loadAppListRows(
            sortId = Preferences.SORT_NAME_ASC,
            orderByRecentlyDiscovered = true,
            tagId = null,
            titleFilter = "",
            table = db.apps()
        )[25]
        db.openHelper.writableDatabase.execSQL(
            "UPDATE ${AppListTable.TABLE} SET ${AppListTable.Columns.SYNC_TIMESTAMP} = ? WHERE _id = ?",
            arrayOf<Any>(System.currentTimeMillis(), unloadedRow.rowId)
        )

        val secondResult = pagingSource.load(PagingSource.LoadParams.Append(key = firstPage.nextKey!!, loadSize = 20, placeholdersEnabled = false))
        val secondPage = secondResult as PagingSource.LoadResult.Page
        val items = (firstPage.data + secondPage.data).filterIsInstance<SectionItem.App>()
        val itemsWithHeaders = insertHeaders(items, showRecentlyDiscovered = true)

        assertFalse(items.single { it.appListItem.app.rowId == unloadedRow.rowId }.appListItem.recentFlag)
        assertEquals(itemsWithHeaders.map { it.sectionKey }.toSet().size, itemsWithHeaders.size)
        assertEquals(1, itemsWithHeaders.count { it.sectionKey == "header:recently-discovered" })
        assertEquals(1, itemsWithHeaders.count { it.sectionKey == "header:watching" })
    }

    @Test
    fun pagingSourceKeepsSortStableForOnDeviceItems() = runBlocking {
        repeat(20) { index ->
            insertApp(
                appId = "sort-$index",
                packageName = "sort.watched.$index",
                title = "Paging Sort Fixture Watched $index"
            )
        }
        installPackage(packageName = "sort.device.alpha", title = "Paging Sort Fixture Alpha")
        installPackage(packageName = "sort.device.zulu", title = "Paging Sort Fixture Zulu")
        val preferences = createPreferences()
        val pagingSource = createPagingSource(showOnDevice = true, preferences = preferences).also {
            it.filterQuery = "Paging Sort Fixture"
        }

        val firstResult = pagingSource.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        val firstPage = firstResult as PagingSource.LoadResult.Page
        preferences.sortIndex = Preferences.SORT_NAME_DESC

        val secondResult = pagingSource.load(PagingSource.LoadParams.Append(key = firstPage.nextKey!!, loadSize = 20, placeholdersEnabled = false))
        val secondPage = secondResult as PagingSource.LoadResult.Page

        assertEquals(
            listOf("sort.device.alpha", "sort.device.zulu"),
            secondPage.data.filterIsInstance<SectionItem.OnDevice>().map { it.appListItem.app.packageName }
        )
    }

    private fun createPagingSource(
        showOnDevice: Boolean,
        preferences: Preferences = createPreferences(),
        showRecentlyDiscovered: Boolean = false,
    ) = WatchListPagingSource(
        config = WatchListPagingSource.Config(
            filterId = Filters.ALL,
            tagId = null,
            showRecentlyDiscovered = showRecentlyDiscovered,
            showOnDevice = showOnDevice,
            showRecentlyInstalled = false,
        ),
        prefs = preferences,
        packageManager = context.packageManager,
        database = db,
        installedApps = InstalledApps.StaticMap(
            mapOf(
                "local.only.watched" to InstalledApps.Info(versionCode = 1, versionName = "1"),
                "local.only.device" to InstalledApps.Info(versionCode = 1, versionName = "1"),
                "boundary.device" to InstalledApps.Info(versionCode = 1, versionName = "1"),
                "sort.device.alpha" to InstalledApps.Info(versionCode = 1, versionName = "1"),
                "sort.device.zulu" to InstalledApps.Info(versionCode = 1, versionName = "1"),
            )
        )
    )

    private fun createPreferences() = Preferences(
        context = context,
        notificationManager = NotificationManager.NoOp(),
        appScope = CoroutineScope(Dispatchers.Unconfined)
    ).also { it.sortIndex = Preferences.SORT_NAME_ASC }

    private fun insertHeaders(
        items: List<SectionItem.App>,
        showRecentlyDiscovered: Boolean = false,
    ): List<SectionItem> {
        val headerFactory = DefaultSectionHeaderFactory(showRecentlyDiscovered)
        return buildList {
            var before: SectionItem? = null
            items.forEach { item ->
                val header = headerFactory.insertSeparator(before, item)
                if (header != null) {
                    add(header)
                }
                add(item)
                before = item
            }
        }
    }

    private suspend fun insertApp(
        appId: String,
        packageName: String,
        title: String,
        status: Int = App.STATUS_NORMAL,
        syncTime: Long = 0,
    ) {
        AppListTable.Queries.insert(
            App(
                rowId = 0,
                appId = appId,
                packageName = packageName,
                versionNumber = 1,
                versionName = "1.0",
                title = title,
                creator = "creator",
                iconUrl = "",
                status = status,
                uploadDate = "",
                price = Price(text = "", cur = "", micros = 0),
                detailsUrl = null,
                uploadTime = 0,
                appType = "",
                syncTime = syncTime
            ),
            db
        )
    }

    private fun installPackage(packageName: String, title: String) {
        val packageInfo = PackageInfo().apply {
            this.packageName = packageName
            versionCode = 1
            versionName = "1"
            lastUpdateTime = 1
            applicationInfo = ApplicationInfo().apply {
                this.packageName = packageName
                flags = 0
                nonLocalizedLabel = title
            }
        }
        shadowOf(context.packageManager).installPackage(packageInfo)
    }
}