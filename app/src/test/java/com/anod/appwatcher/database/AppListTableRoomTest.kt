package com.anod.appwatcher.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppChange
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.preferences.Preferences
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun syncUpdatesSkipDeletedAppWithoutRollingBackOthers() = runBlocking {
        insertApp(appId = "active", title = "Active")
        insertApp(appId = "deleted", title = "Deleted", status = App.STATUS_DELETED)
        val activeApp = db.apps().loadApp("active")!!
        val deletedApp = db.apps().loadApp("deleted")!!
        val activeValues = ContentValues().apply {
            put(BaseColumns._ID, activeApp.rowId)
            put(AppListTable.Columns.STATUS, App.STATUS_UPDATED)
        }
        val deletedValues = ContentValues().apply {
            put(BaseColumns._ID, deletedApp.rowId)
            put(AppListTable.Columns.STATUS, App.STATUS_NORMAL)
        }

        val appliedRowIds = AppListTable.Queries.applySyncUpdates(
            listOf(
                syncUpdate(activeApp, activeValues),
                syncUpdate(deletedApp, deletedValues)
            ),
            db
        )

        assertEquals(setOf(activeApp.rowId.toLong()), appliedRowIds)
        assertEquals(App.STATUS_UPDATED, db.apps().loadAppRow(activeApp.rowId)?.status)
        assertEquals(App.STATUS_DELETED, db.apps().loadAppRow(deletedApp.rowId)?.status)
        assertEquals(listOf(2), db.changelog().ofApp("active").map { it.versionCode })
        assertTrue(db.changelog().ofApp("deleted").isEmpty())
    }

    @Test
    fun syncUpdatesCommitAppAndChangelogTogether() = runBlocking {
        insertApp(appId = "active", title = "Active")
        val activeApp = db.apps().loadApp("active")!!
        val values = ContentValues().apply {
            put(BaseColumns._ID, activeApp.rowId)
            put(AppListTable.Columns.STATUS, App.STATUS_UPDATED)
            put(AppListTable.Columns.VERSION_NUMBER, 2)
        }

        AppListTable.Queries.applySyncUpdates(listOf(syncUpdate(activeApp, values)), db)

        assertEquals(App.STATUS_UPDATED, db.apps().loadAppRow(activeApp.rowId)?.status)
        assertEquals(2, db.apps().loadAppRow(activeApp.rowId)?.versionNumber)
        assertEquals(listOf(2), db.changelog().ofApp("active").map { it.versionCode })
    }

    @Test
    fun syncUpdateCanSkipUnavailableChangelog() = runBlocking {
        insertApp(appId = "active", title = "Active")
        val activeApp = db.apps().loadApp("active")!!
        val values = ContentValues().apply {
            put(BaseColumns._ID, activeApp.rowId)
            put(AppListTable.Columns.VERSION_NUMBER, 1)
            put(AppListTable.Columns.STATUS, App.STATUS_NORMAL)
        }

        val appliedRowIds = AppListTable.Queries.applySyncUpdates(
            listOf(syncUpdate(activeApp, values, changelogValues = null)),
            db
        )

        assertEquals(setOf(activeApp.rowId.toLong()), appliedRowIds)
        assertEquals(App.STATUS_NORMAL, db.apps().loadAppRow(activeApp.rowId)?.status)
        assertTrue(db.changelog().ofApp("active").isEmpty())
    }

    @Test
    fun syncUpdatesSkipChangedIdentityWithoutRollingBackOthers() = runBlocking {
        insertApp(appId = "first", title = "First")
        insertApp(appId = "second", title = "Second")
        val firstApp = db.apps().loadApp("first")!!
        val secondApp = db.apps().loadApp("second")!!
        val firstValues = ContentValues().apply {
            put(BaseColumns._ID, firstApp.rowId)
            put(AppListTable.Columns.STATUS, App.STATUS_UPDATED)
        }
        val secondValues = ContentValues().apply {
            put(BaseColumns._ID, secondApp.rowId)
            put(AppListTable.Columns.STATUS, App.STATUS_UPDATED)
        }
        db.openHelper.writableDatabase.update(
            AppListTable.TABLE,
            SQLiteDatabase.CONFLICT_ABORT,
            ContentValues().apply {
                put(AppListTable.Columns.APP_ID, "replacement")
                put(AppListTable.Columns.PACKAGE_NAME, "replacement.package")
            },
            "${BaseColumns._ID}=?",
            arrayOf<Any>(secondApp.rowId)
        )

        val appliedRowIds = AppListTable.Queries.applySyncUpdates(
            listOf(
                syncUpdate(firstApp, firstValues),
                syncUpdate(secondApp, secondValues)
            ),
            db
        )

        assertEquals(setOf(firstApp.rowId.toLong()), appliedRowIds)
        assertEquals(App.STATUS_UPDATED, db.apps().loadAppRow(firstApp.rowId)?.status)
        assertEquals("replacement", db.apps().loadAppRow(secondApp.rowId)?.appId)
        assertEquals(listOf(2), db.changelog().ofApp("first").map { it.versionCode })
        assertTrue(db.changelog().ofApp("second").isEmpty())
    }

    private fun syncUpdate(
        app: App,
        values: ContentValues,
        changelogValues: ContentValues? = changelog(app.appId)
    ) = AppSyncUpdate(
        rowId = app.rowId.toLong(),
        expectedAppId = app.appId,
        expectedPackageName = app.packageName,
        values = values,
        changelogValues = changelogValues
    )

    private fun changelog(appId: String): ContentValues = AppChange(
        appId = appId,
        versionCode = 2,
        versionName = "2.0",
        details = "",
        uploadDate = "",
        noNewDetails = false
    ).contentValues

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