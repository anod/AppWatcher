package finsky.api

interface DfeResponseVerifier {
    val signatureRequest: String

    @Throws(DfeResponseVerifier.DfeResponseVerifierException::class)
    fun verify(paramArrayOfByte: ByteArray, paramString: String)

    class DfeResponseVerifierException(val errorMessage: String) : Exception()
}