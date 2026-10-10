package finsky.api

import finsky.protos.AndroidCheckinResponse
import finsky.protos.AndroidIntentProto
import finsky.protos.ResponseWrapper
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtobufCompatibilityTest {

    @Test
    fun nestedResponseParsesKnownWireBytesAndPreservesUnknownFields() {
        // ResponseWrapper -> Payload -> DetailsResponse -> DocV2, including unknown field 100.
        val bytes = intArrayOf(
            0x0A, 0x16, 0x12, 0x14, 0x22, 0x12,
            0x0A, 0x03, 0x61, 0x70, 0x70,
            0x2A, 0x04, 0x54, 0x65, 0x73, 0x74,
            0x4A, 0x02, 0x28, 0x09,
            0xA0, 0x06, 0x07
        ).map { it.toByte() }.toByteArray()

        val response = ResponseWrapper.parseFrom(bytes)
        val doc = response.payload.detailsResponse.docV2

        assertEquals("app", doc.docid)
        assertEquals("Test", doc.title)
        assertTrue(doc.availability.hasRestriction())
        assertEquals(9, doc.availability.restriction)
        assertArrayEquals(bytes, response.toByteArray())
    }

    @Test
    fun legacyProto2GroupsParseAndRoundTrip() {
        val bytes = intArrayOf(
            0x0A, 0x03, 0x61, 0x63, 0x74,
            0x2B,
            0x32, 0x03, 0x6B, 0x65, 0x79,
            0x3A, 0x05, 0x76, 0x61, 0x6C, 0x75, 0x65,
            0x2C
        ).map { it.toByte() }.toByteArray()

        val intent = AndroidIntentProto.parseFrom(bytes)

        assertEquals("act", intent.action)
        assertEquals(1, intent.extraCount)
        assertEquals("key", intent.getExtra(0).name)
        assertEquals("value", intent.getExtra(0).value)
        assertArrayEquals(bytes, intent.toByteArray())
    }

    @Test
    fun checkinFixed64FieldsParseAndRoundTrip() {
        val bytes = intArrayOf(
            0x08, 0x01,
            0x39, 0x44, 0x33, 0x22, 0x11, 0x00, 0x00, 0x00, 0x00,
            0x41, 0xAA, 0x99, 0x88, 0x77, 0x00, 0x00, 0x00, 0x00
        ).map { it.toByte() }.toByteArray()

        val response = AndroidCheckinResponse.parseFrom(bytes)

        assertTrue(response.statsOk)
        assertEquals(0x11223344L, response.androidId)
        assertEquals(0x778899AAL, response.securityToken)
        assertArrayEquals(bytes, response.toByteArray())
    }
}