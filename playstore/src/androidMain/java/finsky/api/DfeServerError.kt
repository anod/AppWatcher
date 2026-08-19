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
     * Play's definitive "no longer served" answer for a document. A bare 404 is not sufficient on
     * its own — a stale or malformed details URL produces one for a perfectly healthy app — so the
     * server must also have supplied its item-not-found display message, either on this error or
     * on the parsed error carried as the cause.
     */
    val isItemNotFound: Boolean
        get() = hasItemNotFoundMessage ||
            (statusCode == HTTP_NOT_FOUND && (cause as? DfeServerError)?.hasItemNotFoundMessage == true)

    private val hasItemNotFoundMessage: Boolean
        get() = message?.let { text -> ITEM_NOT_FOUND_MESSAGES.any { text.contains(it, ignoreCase = true) } } == true

    override fun toString(): String {
        return "DisplayErrorMessage[$message]"
    }

    private companion object {
        const val HTTP_NOT_FOUND = 404

        /**
         * Play localizes `displayErrorMessage`, so match the stable server-side wordings rather
         * than relying on a single English phrase. Deliberately excludes a bare "not found",
         * which is the synthetic message the client itself substitutes for an unparseable 404.
         */
        val ITEM_NOT_FOUND_MESSAGES = listOf(
            "item not found",
            "no longer available",
            "not available in your country"
        )
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