package finsky.api


open class DfeError(message: String, cause: Throwable? = null): Exception(message, cause) {

}

class DfeParseError(message: String, cause: Throwable? = null) : DfeError(message, cause) {

}

class DfeServerError(
    message: String,
    val statusCode: Int?,
    cause: Throwable?
) : DfeError(message, cause) {
    val isAuthenticationError: Boolean
        get() = statusCode == 401 || statusCode == 403

    /**
     * Play returns this definitive error for documents that are no longer served,
     * either as a 404 or as a display error message in the response commands.
     */
    val isItemNotFound: Boolean
        get() = statusCode == 404 || message?.contains(ITEM_NOT_FOUND, ignoreCase = true) == true

    override fun toString(): String {
        return "DisplayErrorMessage[$message]"
    }

    private companion object {
        const val ITEM_NOT_FOUND = "item not found"
    }
}

/**
 * True when [this] (or any of its causes) is Play's definitive "item not found" response.
 */
val Throwable.isItemNotFoundError: Boolean
    get() = generateSequence(this) { it.cause }
        .take(MAX_CAUSE_DEPTH)
        .any { it is DfeServerError && it.isItemNotFound }

private const val MAX_CAUSE_DEPTH = 5