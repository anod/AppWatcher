package finsky.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DfeApiImplTest {

    @Test
    fun bulkDetailsRequestIncludesSortedPackageNames() {
        val request = createBulkDetailsRequest(
            docIds = listOf(
                BulkDocId("com.example.second", 202),
                BulkDocId("com.example.first", 101)
            ),
            includeDetails = true
        )

        assertTrue(request.includeDetails)
        assertEquals(
            listOf("com.example.first", "com.example.second"),
            request.docidList
        )
        assertEquals(0, request.docsCount)
    }

    @Test
    fun bulkDetailsRequestRespectsIncludeDetails() {
        val request = createBulkDetailsRequest(
            docIds = listOf(BulkDocId("com.example.app", 1)),
            includeDetails = false
        )

        assertFalse(request.includeDetails)
    }

    @Test
    fun updateCheckUsesAutoUpdateRequestPurpose() {
        assertEquals("${DfeApi.BULK_DETAILS_URI}?au=1", bulkDetailsUrl(forUpdateCheck = true))
        assertEquals(DfeApi.BULK_DETAILS_URI, bulkDetailsUrl(forUpdateCheck = false))
    }
}