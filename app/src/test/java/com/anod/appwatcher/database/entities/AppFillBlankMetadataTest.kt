package com.anod.appwatcher.database.entities

import org.junit.Assert.assertEquals
import org.junit.Test

class AppFillBlankMetadataTest {

    @Test
    fun blankStoredFieldsAreFilledFromFreshDocument() {
        val stored = app(title = "", creator = "", iconUrl = "", detailsUrl = "")
        val fresh = app(
            title = "Fresh Title",
            creator = "Fresh Creator",
            iconUrl = "https://example.com/icon.png",
            detailsUrl = "details?doc=com.example.app"
        )

        val repaired = stored.fillBlankMetadata(fresh)

        assertEquals("Fresh Title", repaired.title)
        assertEquals("Fresh Creator", repaired.creator)
        assertEquals("https://example.com/icon.png", repaired.iconUrl)
        assertEquals("details?doc=com.example.app", repaired.detailsUrl)
    }

    @Test
    fun nullStoredDetailsUrlIsFilledFromFreshDocument() {
        val stored = app(detailsUrl = null)
        val fresh = app(detailsUrl = "details?doc=com.example.app")

        val repaired = stored.fillBlankMetadata(fresh)

        assertEquals("details?doc=com.example.app", repaired.detailsUrl)
    }

    @Test
    fun existingNonBlankStoredFieldsAreNotOverwritten() {
        val stored = app(
            title = "Stored Title",
            creator = "Stored Creator",
            iconUrl = "https://example.com/stored-icon.png",
            detailsUrl = "details?doc=com.example.app.stored"
        )
        val fresh = app(
            title = "Fresh Title",
            creator = "Fresh Creator",
            iconUrl = "https://example.com/fresh-icon.png",
            detailsUrl = "details?doc=com.example.app.fresh"
        )

        val repaired = stored.fillBlankMetadata(fresh)

        assertEquals("Stored Title", repaired.title)
        assertEquals("Stored Creator", repaired.creator)
        assertEquals("https://example.com/stored-icon.png", repaired.iconUrl)
        assertEquals("details?doc=com.example.app.stored", repaired.detailsUrl)
    }

    private fun app(
        title: String = "Example",
        creator: String = "Example Inc",
        iconUrl: String = "https://example.com/icon.png",
        detailsUrl: String? = "details?doc=com.example.app"
    ) = App(
        rowId = 1,
        appId = "com.example.app",
        packageName = "com.example.app",
        versionNumber = 1,
        versionName = "1.0",
        title = title,
        creator = creator,
        iconUrl = iconUrl,
        status = App.STATUS_UPDATED,
        uploadDate = "Jan 1, 2026",
        price = Price("", "", 0),
        detailsUrl = detailsUrl,
        uploadTime = 1,
        appType = "",
        syncTime = 1
    )
}