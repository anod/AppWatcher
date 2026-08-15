package finsky.api

import finsky.protos.Availability
import finsky.protos.DocV2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentTest {

    @Test
    fun absentAvailabilityFailsOpen() {
        val document = Document(DocV2.newBuilder().build())

        assertNull(document.availabilityRestriction)
        assertFalse(document.isUnavailableForUpdate)
    }

    @Test
    fun availabilityWithoutRestrictionFailsOpen() {
        val document = Document(
            DocV2.newBuilder()
                .setAvailability(Availability.newBuilder())
                .build()
        )

        assertNull(document.availabilityRestriction)
        assertFalse(document.isUnavailableForUpdate)
    }

    @Test
    fun explicitRestrictionMarksDocumentUnavailable() {
        val original = documentProtoWithAvailability(restriction = 9)
        val document = Document(DocV2.parseFrom(original.toByteArray()))

        assertEquals(9, document.availabilityRestriction)
        assertTrue(document.isUnavailableForUpdate)
    }

    @Test
    fun availableIfOwnedFailsOpen() {
        val document = documentWithAvailability(
            restriction = 8,
            availableIfOwned = true
        )

        assertFalse(document.isUnavailableForUpdate)
    }

    private fun documentWithAvailability(
        restriction: Int,
        availableIfOwned: Boolean = false
    ): Document = Document(documentProtoWithAvailability(restriction, availableIfOwned))

    private fun documentProtoWithAvailability(
        restriction: Int,
        availableIfOwned: Boolean = false
    ): DocV2 =
        DocV2.newBuilder()
            .setAvailability(
                Availability.newBuilder()
                    .setRestriction(restriction)
                    .setAvailableIfOwned(availableIfOwned)
            )
            .build()
}