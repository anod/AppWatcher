package com.anod.appwatcher.search

import org.junit.Assert.assertEquals
import org.junit.Test

class ListEndpointPagingSourceTest {

    @Test
    fun `resolveNextPageUrl returns new continuation url`() {
        assertEquals(
            "purchaseHistory?o=158",
            ListEndpointPagingSource.resolveNextPageUrl(
                requestedNextPageUrl = "purchaseHistory?o=79",
                responseNextPageUrl = "purchaseHistory?o=158"
            )
        )
    }

    @Test
    fun `resolveNextPageUrl stops paging when continuation url repeats`() {
        assertEquals(
            null,
            ListEndpointPagingSource.resolveNextPageUrl(
                requestedNextPageUrl = "purchaseHistory?o=158",
                responseNextPageUrl = "purchaseHistory?o=158"
            )
        )
    }

    @Test
    fun `resolveNextPageUrl stops paging when continuation url is missing`() {
        assertEquals(
            null,
            ListEndpointPagingSource.resolveNextPageUrl(
                requestedNextPageUrl = "purchaseHistory?o=158",
                responseNextPageUrl = null
            )
        )
    }

    @Test
    fun `resolveNextPageUrl stops paging when continuation url is blank`() {
        assertEquals(
            null,
            ListEndpointPagingSource.resolveNextPageUrl(
                requestedNextPageUrl = "purchaseHistory?o=158",
                responseNextPageUrl = ""
            )
        )
    }
}