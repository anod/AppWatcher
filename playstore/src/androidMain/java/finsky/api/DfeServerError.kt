package finsky.api


open class DfeError(message: String, cause: Throwable? = null): Exception(message, cause) {

}

class DfeParseError(message: String, cause: Throwable? = null) : DfeError(message, cause) {

}

class DfeServerError(message: String) : DfeError(message) {
    override fun toString(): String {
        return "DisplayErrorMessage[$message]"
    }
}