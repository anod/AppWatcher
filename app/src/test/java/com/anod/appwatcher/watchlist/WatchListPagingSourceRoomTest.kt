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

    private fun createPagingSource(showOnDevice: Boolean) = WatchListPagingSource(
        config = WatchListPagingSource.Config(
            filterId = Filters.ALL,
            tagId = null,
            showRecentlyDiscovered = false,
            showOnDevice = showOnDevice,
            showRecentlyInstalled = false,
        ),
        prefs = Preferences(
            context = context,
            notificationManager = NotificationManager.NoOp(),
            appScope = CoroutineScope(Dispatchers.Unconfined)
        ).also { it.sortIndex = Preferences.SORT_NAME_ASC },
        packageManager = context.packageManager,
        database = db,
        installedApps = InstalledApps.StaticMap(
            mapOf(
                "local.only.watched" to InstalledApps.Info(versionCode = 1, versionName = "1"),
                "local.only.device" to InstalledApps.Info(versionCode = 1, versionName = "1"),
                "boundary.device" to InstalledApps.Info(versionCode = 1, versionName = "1"),
            )
        )
    )

    private suspend fun insertApp(appId: String, packageName: String, title: String) {
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
                status = App.STATUS_NORMAL,
                uploadDate = "",
                price = Price(text = "", cur = "", micros = 0),
                detailsUrl = null,
                uploadTime = 0,
                appType = "",
                syncTime = 0
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