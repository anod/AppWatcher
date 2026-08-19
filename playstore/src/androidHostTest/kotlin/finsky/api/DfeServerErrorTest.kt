package finsky.api

import java.io.IOException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DfeServerErrorTest {

    @Test
    fun notFoundStatusCodeIsItemNotFound() {
        assertTrue(DfeServerError("Not Found", statusCode = 404, cause = null).isItemNotFound)
    }

    @Test
    fun displayErrorMessageIsItemNotFound() {
        assertTrue(DfeServerError("Item not found.", statusCode = null, cause = null).isItemNotFound)
    }

    @Test
    fun otherServerErrorsAreNotItemNotFound() {
        assertFalse(DfeServerError("Unauthorized", statusCode = 401, cause = null).isItemNotFound)
        assertFalse(DfeServerError("Server error", statusCode = 500, cause = null).isItemNotFound)
    }

    @Test
    fun itemNotFoundIsDetectedThroughCauseChain() {
        val wrapped = DfeServerError(
            message = "Status code 404",
            statusCode = 404,
            cause = DfeServerError("Item not found.", statusCode = null, cause = null)
        )

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
}
