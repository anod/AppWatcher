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
import com.anod.appwatcher.utils.PackageState
import com.anod.appwatcher.utils.PackageStateProvider
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
    fun onDeviceRefreshAndPrependDoNotRepeatPackages() = runBlocking {
        repeat(25) { index ->
            insertApp(appId = "overlap-$index", packageName = "overlap.watched.$index", title = "Overlap Watched $index")
        }
        repeat(40) { index ->
            installPackage(packageName = "overlap.device.$index", title = "Overlap Device ${index.toString().padStart(2, '0')}")
        }
        val source = createPagingSource(showOnDevice = true).also { it.filterQuery = "Overlap" }
        val refresh = source.load(
            PagingSource.LoadParams.Refresh(key = 40, loadSize = 20, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page
        val prepend = source.load(
            PagingSource.LoadParams.Prepend(key = refresh.prevKey!!, loadSize = 20, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page
        val items = prepend.data + refresh.data
        val duplicateKeys = items.groupBy { it.sectionKey }.filterValues { it.size > 1 }.keys

        assertEquals(emptySet<String>(), duplicateKeys)
        assertEquals(20, refresh.data.size)
        assertEquals(20, prepend.data.size)
        assertEquals("overlap.device.15", (refresh.data.first() as SectionItem.OnDevice).appListItem.app.packageName)
        assertEquals("overlap.device.14", (prepend.data.last() as SectionItem.OnDevice).appListItem.app.packageName)
    }

    @Test
    fun onDevicePagesCoverCombinedListInBothDirections() = runBlocking {
        for (watchedCount in listOf(0, 19, 20, 25)) {
            val fixture = "Combined$watchedCount"
            repeat(watchedCount) { index ->
                insertApp(appId = "$fixture-$index", packageName = "$fixture.watched.$index", title = "$fixture Watched $index")
            }
            repeat(45) { index ->
                installPackage(packageName = "$fixture.device.$index", title = "$fixture Device ${index.toString().padStart(2, '0')}")
            }
            for (showRecent in listOf(false, true)) {
                val source = createPagingSource(showOnDevice = true, showRecentlyInstalled = showRecent).also {
                    it.filterQuery = "$fixture "
                }
                val first = source.load(
                    PagingSource.LoadParams.Refresh(key = null, loadSize = 60, placeholdersEnabled = false)
                ) as PagingSource.LoadResult.Page
                val forwardItems = first.data.toMutableList()
                var next = first.nextKey
                while (next != null) {
                    val page = source.load(
                        PagingSource.LoadParams.Append(key = next, loadSize = 20, placeholdersEnabled = false)
                    ) as PagingSource.LoadResult.Page
                    assertTrue(page.data.size <= 20)
                    forwardItems.addAll(page.data)
                    next = page.nextKey
                }
                assertEquals(watchedCount + 45 + if (showRecent) 1 else 0, forwardItems.size)
                assertEquals(forwardItems.size, forwardItems.distinctBy { it.sectionKey }.size)

                for (refreshLoadSize in listOf(20, 60)) {
                    val lastOffset = (if (refreshLoadSize == 60) 40 else 60) - if (showRecent) 1 else 0
                    val refreshed = source.load(
                        PagingSource.LoadParams.Refresh(key = lastOffset, loadSize = refreshLoadSize, placeholdersEnabled = false)
                    ) as PagingSource.LoadResult.Page
                    val backwardItems = refreshed.data.toMutableList()
                    var previous = refreshed.prevKey
                    while (previous != null) {
                        val page = source.load(
                            PagingSource.LoadParams.Prepend(key = previous, loadSize = 20, placeholdersEnabled = false)
                        ) as PagingSource.LoadResult.Page
                        backwardItems.addAll(0, page.data)
                        previous = page.prevKey
                    }
                    assertEquals(forwardItems.map { it.sectionKey }, backwardItems.map { it.sectionKey })
                    val headerFactory = DefaultSectionHeaderFactory(showRecentlyDiscovered = false)
                    val headers = backwardItems.mapIndexedNotNull { index, item ->
                        headerFactory.insertSeparator(backwardItems.getOrNull(index - 1), item)
                    }
                    assertEquals(headers.size, headers.distinctBy { it.sectionKey }.size)
                }
            }
        }
    }

    @Test
    fun onDeviceSnapshotStaysStableUntilFilterOrGenerationChanges() = runBlocking {
        repeat(30) { index ->
            installPackage(packageName = "stable.device.$index", title = "Stable Device ${index.toString().padStart(2, '0')}")
        }
        val source = createPagingSource(showOnDevice = true).also { it.filterQuery = "Stable Device" }
        val first = source.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page
        installPackage(packageName = "stable.device.new", title = "Stable Device 00 New")
        insertApp(appId = "now-watched", packageName = "stable.device.25", title = "Stable Device 25")
        val second = source.load(
            PagingSource.LoadParams.Append(key = first.nextKey!!, loadSize = 20, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page
        val currentItems = first.data + second.data
        assertEquals(30, currentItems.size)
        assertEquals(30, currentItems.distinctBy { it.sectionKey }.size)
        assertTrue(currentItems.any { it.sectionKey == "ondevice-stable.device.25" })
        assertFalse(currentItems.any { it.sectionKey == "ondevice-stable.device.new" })

        val newSource = createPagingSource(showOnDevice = true).also { it.filterQuery = "Stable Device" }
        val refreshed = newSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 60, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page
        assertEquals(31, refreshed.data.size)
        assertTrue(refreshed.data.any { it.sectionKey == "ondevice-stable.device.new" })
        assertFalse(refreshed.data.any { it.sectionKey == "ondevice-stable.device.25" })
        source.filterQuery = "Stable Device 00 New"
        val filtered = source.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page
        assertEquals(listOf("ondevice-stable.device.new"), filtered.data.map { it.sectionKey })
    }

    @Test
    fun disabledInstalledAppIsNotShownAsUpdatable() = runBlocking {
        insertApp(
            appId = "disabled",
            packageName = "disabled.watched",
            title = "Z Disabled Watched",
            versionNumber = 2,
            status = App.STATUS_UPDATED
        )
        insertApp(
            appId = "enabled",
            packageName = "enabled.watched",
            title = "M Enabled Watched",
            versionNumber = 2,
            status = App.STATUS_UPDATED
        )
        insertApp(
            appId = "normal",
            packageName = "normal.watched",
            title = "A Normal Watched"
        )

        val allResult = createPagingSource(
            showOnDevice = false,
            packageEnabled = { it != "disabled.watched" }
        ).load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        val appItems = (allResult as PagingSource.LoadResult.Page)
            .data
            .filterIsInstance<SectionItem.App>()
        val disabledItem = appItems.single { it.appListItem.app.packageName == "disabled.watched" }

        assertFalse(disabledItem.isPackageEnabled)
        assertEquals(
            listOf("enabled.watched", "normal.watched", "disabled.watched"),
            appItems.map { it.appListItem.app.packageName }
        )
        val sectionHeaderFactory = DefaultSectionHeaderFactory(showRecentlyDiscovered = false)
        val headers = appItems.mapIndexedNotNull { index, item ->
            sectionHeaderFactory.insertSeparator(appItems.getOrNull(index - 1), item)
        }
        assertEquals(
            listOf(SectionHeader.New, SectionHeader.Watching),
            headers.map { it.type }
        )
        assertEquals(headers.size, headers.distinctBy { it.sectionKey }.size)
        assertEquals(
            SectionHeader.Watching,
            DefaultSectionHeaderFactory(showRecentlyDiscovered = false)
                .insertSeparator(before = appItems.first(), after = disabledItem)
                ?.type
        )

        val updatableResult = createPagingSource(
            showOnDevice = false,
            filterId = Filters.UPDATABLE,
            packageEnabled = { it != "disabled.watched" }
        ).load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))

        assertEquals(
            listOf("enabled.watched"),
            (updatableResult as PagingSource.LoadResult.Page)
                .data
                .filterIsInstance<SectionItem.App>()
                .map { it.appListItem.app.packageName }
        )
    }

    @Test
    fun sleepingAppUpdateIsKeptButShownAsDisabledUntilReenabled() = runBlocking {
        insertApp(
            appId = "disabled",
            packageName = "disabled.watched",
            title = "Disabled Watched",
            versionNumber = 2,
            status = App.STATUS_UPDATED,
            syncTime = System.currentTimeMillis()
        )
        val installedApps = InstalledApps.StaticMap(
            mapOf(
                "disabled.watched" to InstalledApps.Info(versionCode = 1, versionName = "1")
            )
        )

        val disabledItem = loadSingleApp(
            createPagingSource(
                showOnDevice = false,
                showRecentlyDiscovered = true,
                packageEnabled = { false },
                installedApps = installedApps
            )
        )

        assertFalse(disabledItem.isPackageEnabled)
        assertEquals(App.STATUS_UPDATED, disabledItem.appListItem.app.status)
        assertEquals(
            SectionHeader.Watching,
            DefaultSectionHeaderFactory(showRecentlyDiscovered = true)
                .insertSeparator(before = null, after = disabledItem)
                ?.type
        )
        assertTrue(
            loadApps(
                createPagingSource(
                    showOnDevice = false,
                    filterId = Filters.UPDATABLE,
                    packageEnabled = { false },
                    installedApps = installedApps
                )
            ).isEmpty()
        )
        assertEquals(App.STATUS_UPDATED, db.apps().loadApp("disabled")!!.status)

        val enabledItem = loadSingleApp(
            createPagingSource(
                showOnDevice = false,
                showRecentlyDiscovered = true,
                packageEnabled = { true },
                installedApps = installedApps
            )
        )

        assertEquals(
            SectionHeader.New,
            DefaultSectionHeaderFactory(showRecentlyDiscovered = true)
                .insertSeparator(before = null, after = enabledItem)
                ?.type
        )
        assertEquals(
            listOf("disabled.watched"),
            loadApps(
                createPagingSource(
                    showOnDevice = false,
                    filterId = Filters.UPDATABLE,
                    packageEnabled = { true },
                    installedApps = installedApps
                )
            ).map { it.appListItem.app.packageName }
        )
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
    fun pagingSourceDoesNotRenderRowsWhoseVersionChangedAfterSnapshot() = runBlocking {
        val packageNames = (0 until 40).map { index ->
            "version.${index.toString().padStart(2, '0')}"
        }
        packageNames.forEachIndexed { index, packageName ->
            insertApp(
                appId = "version-$index",
                packageName = packageName,
                title = "Version ${index.toString().padStart(2, '0')}",
                versionNumber = 2,
                status = App.STATUS_UPDATED
            )
        }
        val installedApps = InstalledApps.StaticMap(
            packageNames.associateWith {
                InstalledApps.Info(versionCode = 1, versionName = "1")
            }
        )
        val pagingSource = createPagingSource(
            showOnDevice = false,
            filterId = Filters.UPDATABLE,
            installedApps = installedApps
        )

        val firstResult = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )
        val firstPage = firstResult as PagingSource.LoadResult.Page
        val changedRow = AppListTable.Queries.loadAppListRows(
            sortId = Preferences.SORT_NAME_ASC,
            tagId = null,
            titleFilter = "",
            table = db.apps()
        )[25]
        db.openHelper.writableDatabase.execSQL(
            "UPDATE ${AppListTable.TABLE} SET ${AppListTable.Columns.VERSION_NUMBER} = ? WHERE _id = ?",
            arrayOf<Any>(1, changedRow.rowId)
        )

        val secondResult = pagingSource.load(
            PagingSource.LoadParams.Append(
                key = firstPage.nextKey!!,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )
        val secondPage = secondResult as PagingSource.LoadResult.Page

        assertTrue(
            secondPage.data
                .filterIsInstance<SectionItem.App>()
                .none { it.appListItem.app.rowId == changedRow.rowId }
        )
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
    fun packageEnabledStateIsPinnedForThePagingGeneration() = runBlocking {
        val packageNames = (0 until 40).map { index -> "snapshot.${index.toString().padStart(2, '0')}" }
        packageNames.forEachIndexed { index, packageName ->
            insertApp(
                appId = "snapshot-$index",
                packageName = packageName,
                title = "Snapshot ${index.toString().padStart(2, '0')}",
                versionNumber = 2,
                status = App.STATUS_UPDATED
            )
        }
        val installedApps = InstalledApps.StaticMap(
            packageNames.associateWith {
                InstalledApps.Info(versionCode = 1, versionName = "1")
            }
        )
        val enabledStates = packageNames.associateWith { true }.toMutableMap()
        var enabledLookups = 0
        val packageEnabled: (String) -> Boolean = { packageName ->
            enabledLookups++
            enabledStates.getValue(packageName)
        }
        val pagingSource = createPagingSource(
            showOnDevice = false,
            packageEnabled = packageEnabled,
            installedApps = installedApps,
        )

        val firstResult = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )
        val firstPage = firstResult as PagingSource.LoadResult.Page
        assertEquals(40, enabledLookups)

        enabledStates["snapshot.25"] = false
        val secondResult = pagingSource.load(
            PagingSource.LoadParams.Append(key = firstPage.nextKey!!, loadSize = 20, placeholdersEnabled = false)
        )
        val secondPage = secondResult as PagingSource.LoadResult.Page
        val currentGenerationItems = (firstPage.data + secondPage.data).filterIsInstance<SectionItem.App>()

        assertTrue(currentGenerationItems.single { it.appListItem.app.packageName == "snapshot.25" }.isPackageEnabled)
        assertEquals(40, enabledLookups)
        val currentGenerationWithHeaders = insertHeaders(currentGenerationItems)
        assertEquals(
            currentGenerationWithHeaders.map { it.sectionKey }.toSet().size,
            currentGenerationWithHeaders.size
        )
        assertEquals(1, currentGenerationWithHeaders.count { it.sectionKey == "header:new" })
        assertEquals(0, currentGenerationWithHeaders.count { it.sectionKey == "header:watching" })

        val refreshedSource = createPagingSource(
            showOnDevice = false,
            packageEnabled = packageEnabled,
            installedApps = installedApps,
        )
        val refreshedResult = refreshedSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 40, placeholdersEnabled = false)
        )
        val refreshedItems = (refreshedResult as PagingSource.LoadResult.Page)
            .data
            .filterIsInstance<SectionItem.App>()
        val refreshedWithHeaders = insertHeaders(refreshedItems)

        assertFalse(refreshedItems.single { it.appListItem.app.packageName == "snapshot.25" }.isPackageEnabled)
        assertEquals(80, enabledLookups)
        assertEquals(1, refreshedWithHeaders.count { it.sectionKey == "header:new" })
        assertEquals(1, refreshedWithHeaders.count { it.sectionKey == "header:watching" })
        assertEquals(refreshedWithHeaders.map { it.sectionKey }.toSet().size, refreshedWithHeaders.size)
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
        filterId: Int = Filters.ALL,
        packageEnabled: (String) -> Boolean = { true },
        installedApps: InstalledApps = defaultInstalledApps(),
        showRecentlyInstalled: Boolean = false,
    ) = WatchListPagingSource(
        config = WatchListPagingSource.Config(
            filterId = filterId,
            tagId = null,
            showRecentlyDiscovered = showRecentlyDiscovered,
            showOnDevice = showOnDevice,
            showRecentlyInstalled = showRecentlyInstalled,
        ),
        prefs = preferences,
        packageManager = context.packageManager,
        database = db,
        packageStates = packageStateProvider(installedApps, packageEnabled)
    )

    private suspend fun loadApps(pagingSource: WatchListPagingSource): List<SectionItem.App> =
        (pagingSource.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)) as PagingSource.LoadResult.Page)
            .data
            .filterIsInstance<SectionItem.App>()

    private suspend fun loadSingleApp(pagingSource: WatchListPagingSource): SectionItem.App =
        loadApps(pagingSource).single()

    private fun packageStateProvider(
        installedApps: InstalledApps,
        packageEnabled: (String) -> Boolean
    ) = PackageStateProvider { packageNames ->
        packageNames.associateWith { packageName ->
            val packageInfo = installedApps.packageInfo(packageName)
            PackageState(
                packageInfo = packageInfo,
                isEnabled = !packageInfo.isInstalled || packageEnabled(packageName)
            )
        }
    }

    private fun defaultInstalledApps(): InstalledApps = InstalledApps.StaticMap(
        mapOf(
            "local.only.watched" to InstalledApps.Info(versionCode = 1, versionName = "1"),
            "local.only.device" to InstalledApps.Info(versionCode = 1, versionName = "1"),
            "boundary.device" to InstalledApps.Info(versionCode = 1, versionName = "1"),
            "sort.device.alpha" to InstalledApps.Info(versionCode = 1, versionName = "1"),
            "sort.device.zulu" to InstalledApps.Info(versionCode = 1, versionName = "1"),
            "disabled.watched" to InstalledApps.Info(versionCode = 1, versionName = "1"),
            "enabled.watched" to InstalledApps.Info(versionCode = 1, versionName = "1"),
            "normal.watched" to InstalledApps.Info(versionCode = 1, versionName = "1"),
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
        versionNumber: Int = 1,
        status: Int = App.STATUS_NORMAL,
        syncTime: Long = 0,
    ) {
        AppListTable.Queries.insert(
            App(
                rowId = 0,
                appId = appId,
                packageName = packageName,
                versionNumber = versionNumber,
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