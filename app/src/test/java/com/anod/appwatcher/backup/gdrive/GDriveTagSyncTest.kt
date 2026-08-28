package com.anod.appwatcher.backup.gdrive

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.anod.appwatcher.backup.DbJsonReader
import com.anod.appwatcher.backup.DbJsonWriter
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.TagsTable
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.Price
import com.anod.appwatcher.database.entities.Tag
import java.io.StringReader
import java.io.StringWriter
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
class GDriveTagSyncTest {

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
    fun deletedTagIsNotRestoredAndIsExcludedFromNextUpload() = runBlocking {
        val tag = insertTag("Deleted")
        insertApp("tagged")
        db.appTags().insert("tagged", tag.id)
        val staleRemote = writeDatabase()

        TagsTable.Queries.delete(tag, db)
        GDriveSync.insertRemoteItems(StringReader(staleRemote), db)

        assertTrue(db.tags().load().isEmpty())
        assertTrue(db.appTags().load().isEmpty())
        assertEquals(listOf(tag.name), db.tags().loadDeletedNames())
        assertEquals(listOf(tag.id), db.tags().loadDeletedIds())
        assertTrue(DbJsonReader().read(StringReader(writeDatabase())).tags.isEmpty())
    }

    @Test
    fun recreatedTagIsNotDuplicatedByStaleRemoteData() = runBlocking {
        val deletedTag = insertTag("Recreated")
        val staleRemote = writeDatabase()
        TagsTable.Queries.delete(deletedTag, db)
        val recreatedTag = insertTag("Recreated")

        GDriveSync.insertRemoteItems(StringReader(staleRemote), db)
        GDriveSync.insertRemoteItems(StringReader(staleRemote), db)

        assertEquals(listOf(recreatedTag), db.tags().load())
        assertEquals(listOf(recreatedTag), DbJsonReader().read(StringReader(writeDatabase())).tags)
    }

    @Test
    fun existingTagCreateAndUpdateMergeBehaviorIsPreserved() = runBlocking {
        val existingTag = insertTag("Existing")
        val staleRemote = writeDatabase()
        val updatedTag = existingTag.copy(color = 0xFF00FF00.toInt())
        db.tags().update(updatedTag)
        val createdTag = insertTag("Created")

        GDriveSync.insertRemoteItems(StringReader(staleRemote), db)

        assertEquals(setOf(createdTag, updatedTag), db.tags().load().toSet())
        assertEquals(setOf(createdTag, updatedTag), DbJsonReader().read(StringReader(writeDatabase())).tags.toSet())
    }

    @Test
    fun uploadAcknowledgesOnlyTombstonesPresentBeforeWriting() = runBlocking {
        val uploadedTagId = db.tags().insertDeleted("Uploaded", Tag.DEFAULT_COLOR).toInt()
        val uploadedTagIds = db.tags().loadDeletedIds()
        db.tags().insertDeleted("Deleted during upload", Tag.DEFAULT_COLOR)

        db.tags().deleteDeleted(uploadedTagIds)

        assertEquals(listOf(uploadedTagId), uploadedTagIds)
        assertEquals(listOf("Deleted during upload"), db.tags().loadDeletedNames())
    }

    @Test
    fun renamingTagTombstonesOldNameAndClearsNewNameTombstone() = runBlocking {
        val tag = insertTag("Old")
        db.tags().insertDeleted("New", Tag.DEFAULT_COLOR)

        TagsTable.Queries.update(tag.copy(name = "New"), db)

        assertEquals(listOf("New"), db.tags().load().map { it.name })
        assertEquals(listOf("Old"), db.tags().loadDeletedNames())
    }

    private suspend fun insertTag(name: String): Tag {
        val id = TagsTable.Queries.insert(Tag(name), db).toInt()
        return db.tags().loadById(id)!!
    }

    private suspend fun insertApp(appId: String) {
        AppListTable.Queries.insert(
            App(
                rowId = 0,
                appId = appId,
                packageName = "$appId.package",
                versionNumber = 1,
                versionName = "1.0",
                title = appId,
                creator = "creator",
                iconUrl = "",
                status = App.STATUS_NORMAL,
                uploadDate = "",
                price = Price("", "", 0),
                detailsUrl = null,
                uploadTime = 0,
                appType = "",
                syncTime = 0
            ),
            db
        )
    }

    private suspend fun writeDatabase(): String {
        val writer = StringWriter()
        DbJsonWriter().write(writer, db)
        return writer.toString()
    }
}