package finsky.api

import java.io.IOException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DfeServerErrorTest {

    @Test
    fun displayErrorMessageIsItemNotFound() {
        assertTrue(DfeServerError("Item not found.", statusCode = null, cause = null).isItemNotFound)
        assertTrue(DfeServerError("This app is no longer available.", statusCode = null, cause = null).isItemNotFound)
        assertTrue(
            DfeServerError(
                "This item is not available in your country.",
                statusCode = null,
                cause = null
            ).isItemNotFound
        )
    }

    /**
     * A bare 404 is produced by a stale or malformed details URL for a perfectly healthy app,
     * so it must never on its own be treated as Play's definitive item-not-found answer.
     */
    @Test
    fun bareNotFoundStatusIsNotItemNotFound() {
        assertFalse(DfeServerError("Not Found", statusCode = 404, cause = null).isItemNotFound)
        assertFalse(DfeServerError("Status code 404", statusCode = 404, cause = null).isItemNotFound)
        assertFalse(
            DfeServerError(
                message = "Not Found",
                statusCode = 404,
                cause = IOException("unparseable body")
            ).isItemNotFound
        )
    }

    @Test
    fun otherServerErrorsAreNotItemNotFound() {
        assertFalse(DfeServerError("Unauthorized", statusCode = 401, cause = null).isItemNotFound)
        assertFalse(DfeServerError("Server error", statusCode = 500, cause = null).isItemNotFound)
    }

    /**
     * DfeApiImpl wraps the parsed display error as the cause of the transport-level 404.
     */
    @Test
    fun itemNotFoundIsDetectedThroughCauseChain() {
        val wrapped = DfeServerError(
            message = "Item not found.",
            statusCode = 404,
            cause = DfeServerError("Item not found.", statusCode = null, cause = null)
        )

        assertTrue(wrapped.isItemNotFound)
        assertTrue(wrapped.isItemNotFoundError)
        assertTrue(IOException("network", wrapped).isItemNotFoundError)
    }

    @Test
    fun unrelatedErrorsAreNotItemNotFound() {
        assertFalse(IOException("network").isItemNotFoundError)
        assertFalse(
            IOException(
                "network",
                DfeServerError("Unauthorized", statusCode = 401, cause = null)
            ).isItemNotFoundError
        )
    }

    /**
     * The cause walk is depth-bounded, so an item-not-found buried deeper than the bound is
     * ignored rather than causing an unbounded traversal.
     */
    @Test
    fun deepCauseChainIsBounded() {
        val itemNotFound = DfeServerError("Item not found.", statusCode = null, cause = null)
        var error: Throwable = itemNotFound
        repeat(10) { index -> error = IOException("wrap $index", error) }

        assertFalse(error.isItemNotFoundError)
    }
}
