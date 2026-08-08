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

    override fun toString(): String {
        return "DisplayErrorMessage[$message]"
    }
}