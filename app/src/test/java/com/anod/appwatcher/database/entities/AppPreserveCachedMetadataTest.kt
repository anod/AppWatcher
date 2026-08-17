package com.anod.appwatcher.database.entities

import org.junit.Assert.assertEquals
import org.junit.Test

class AppPreserveCachedMetadataTest {

    @Test
    fun blankFieldsFallBackToCachedValues() {
        val cached = app(
            title = "Cached Title",
            creator = "Cached Creator",
            iconUrl = "https://example.com/icon.png",
            detailsUrl = "details?doc=com.example.app"
        )
        val sparse = app(title = "", creator = "", iconUrl = "", detailsUrl = "")

        val merged = sparse.preserveCachedMetadata(cached)

        assertEquals("Cached Title", merged.title)
        assertEquals("Cached Creator", merged.creator)
        assertEquals("https://example.com/icon.png", merged.iconUrl)
        assertEquals("details?doc=com.example.app", merged.detailsUrl)
    }

    @Test
    fun nullDetailsUrlFallsBackToCachedValue() {
        val cached = app(detailsUrl = "details?doc=com.example.app")
        val sparse = app(detailsUrl = null)

        val merged = sparse.preserveCachedMetadata(cached)

        assertEquals("details?doc=com.example.app", merged.detailsUrl)
    }

    @Test
    fun nonBlankFieldsOverrideCachedValues() {
        val cached = app(
            title = "Cached Title",
            creator = "Cached Creator",
            iconUrl = "https://example.com/old-icon.png",
            detailsUrl = "details?doc=com.example.app"
        )
        val fresh = app(
            title = "Fresh Title",
            creator = "Fresh Creator",
            iconUrl = "https://example.com/new-icon.png",
            detailsUrl = "details?doc=com.example.app.v2"
        )

        val merged = fresh.preserveCachedMetadata(cached)

        assertEquals("Fresh Title", merged.title)
        assertEquals("Fresh Creator", merged.creator)
        assertEquals("https://example.com/new-icon.png", merged.iconUrl)
        assertEquals("details?doc=com.example.app.v2", merged.detailsUrl)
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