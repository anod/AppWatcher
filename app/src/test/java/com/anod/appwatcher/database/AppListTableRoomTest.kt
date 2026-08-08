package com.anod.appwatcher.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.preferences.Preferences
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppListTableRoomTest {

    private lateinit var db: AppsDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun loadAppListAppliesTagSelectionsAndTitleFilter() = runBlocking {
        insertApp(appId = "alpha", title = "Alpha")
        insertApp(appId = "beta", title = "Beta")
        insertApp(appId = "gamma", title = "Gamma")
        insertApp(appId = "deleted", title = "Deleted", status = App.STATUS_DELETED)
        insertTag(appId = "alpha", tagId = 1)
        insertTag(appId = "alpha", tagId = 2)
        insertTag(appId = "beta", tagId = 2)
        insertTag(appId = "deleted", tagId = 1)
        insertDuplicateTagRow(appId = "alpha", tagId = 1)

        assertEquals(listOf("alpha", "beta", "gamma"), loadIds(tagId = null, titleFilter = ""))
        assertEquals(listOf("alpha"), loadIds(tagId = 1, titleFilter = ""))
        assertEquals(listOf("alpha", "beta"), loadIds(tagId = 2, titleFilter = ""))
        assertEquals(listOf("gamma"), loadIds(tagId = Tag.empty.id, titleFilter = ""))
        assertEquals(listOf("alpha"), loadIds(tagId = null, titleFilter = "Alp"))
        assertEquals(listOf("beta"), loadIds(tagId = 2, titleFilter = "Bet"))
        assertEquals(listOf("gamma"), loadIds(tagId = Tag.empty.id, titleFilter = "Gam"))
    }

    private suspend fun insertApp(appId: String, title: String, status: Int = App.STATUS_NORMAL) {
        AppListTable.Queries.insert(
            App(
                rowId = 0,
                appId = appId,
                packageName = "$appId.package",
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
                syncTime = 0
            ),
            db
        )
    }

    private suspend fun insertTag(appId: String, tagId: Int) {
        db.appTags().insert(appId, tagId)
    }

    private fun insertDuplicateTagRow(appId: String, tagId: Int) {
        db.openHelper.writableDatabase.execSQL("DROP INDEX IF EXISTS `index_app_tags_app_id_tags_id`")
        db.openHelper.writableDatabase.execSQL(
            "INSERT INTO ${AppTagsTable.TABLE} " +
                "(${AppTagsTable.Columns.APP_ID}, ${AppTagsTable.Columns.TAGS_ID}) " +
                "VALUES (?, ?)",
            arrayOf<Any>(appId, tagId)
        )
    }

    private suspend fun loadIds(tagId: Int?, titleFilter: String): List<String> {
        val rows = AppListTable.Queries.loadAppListRows(
            sortId = Preferences.SORT_NAME_ASC,
            orderByRecentlyDiscovered = false,
            tagId = tagId,
            titleFilter = titleFilter,
            table = db.apps()
        )
        return AppListTable.Queries.loadAppList(
            rowIds = rows.map { it.rowId },
            table = db.apps()
        ).map { item -> item.app.appId }
    }
}