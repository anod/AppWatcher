package finsky.api

import android.content.Context
import android.text.TextUtils
import android.util.Base64

import finsky.api.utils.AndroidKeyczarReader

import org.keyczar.KeyczarFileReader
import org.keyczar.Verifier
import org.keyczar.exceptions.KeyczarException
import org.keyczar.interfaces.KeyczarReader

import java.io.File
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

import info.anodsplace.framework.AppLog
import info.anodsplace.playstore.BuildConfig

class DfeResponseVerifierImpl(private val mContext: Context) : DfeResponseVerifier {
    private val mNonce: ByteArray
    private var mNonceInitialized: Boolean = false

    init {
        this.mNonce = ByteArray(256)
    }

    @Throws(DfeResponseVerifier.DfeResponseVerifierException::class)
    private fun internalVerify(keyczarReader: KeyczarReader?, array: ByteArray, array2: ByteArray, s: String): Boolean {
        try {
            val verifier = Verifier(keyczarReader!!)
            val array3 = ByteArray(array.size + array2.size)
            System.arraycopy(array, 0, array3, 0, array.size)
            System.arraycopy(array2, 0, array3, array.size, array2.size)
            return verifier.verify(array3, extractResponseSignature(s))
        } catch (ex: KeyczarException) {
            AppLog.d("Keyczar exception during signature verification: %s", ex)
            return false
        }

    }

    override val signatureRequest: String
        get() {
        synchronized(this) {
            if (DfeResponseVerifierImpl.SECURE_RANDOM == null) {
                throw DfeResponseVerifier.DfeResponseVerifierException("Uninitialized SecureRandom.")
            }
        }
        if (!this.mNonceInitialized) {
            DfeResponseVerifierImpl.SECURE_RANDOM!!.nextBytes(this.mNonce)
            this.mNonceInitialized = true
        }
        // monitorexit(this)
        return "nonce=" + Base64.encodeToString(this.mNonce, 11)
    }

    @Throws(DfeResponseVerifier.DfeResponseVerifierException::class)
    override fun verify(paramArrayOfByte: ByteArray, paramString: String) {
        var b = this.internalVerify(getProdReader(this.mContext), this.mNonce, paramArrayOfByte, paramString)
        if (!b) {
            val fallbackReader = getFallbackReader(this.mContext)
            if (fallbackReader != null) {
                AppLog.d("Retry verification using fallback keys.")
                b = this.internalVerify(fallbackReader, this.mNonce, paramArrayOfByte, paramString)
            }
        }
        if (!b || BuildConfig.DEBUG) {
            AppLog.d("Response signature verified: %b", b)
        }
        if (!b) {
            throw DfeResponseVerifier.DfeResponseVerifierException("Response signature mismatch.")
        }
    }

    companion object {
        private val FALLBACK_KEYS_FILES_SUBDIR: String
        private val PROD_KEYS_ASSETS_SUBDIR: String
        private var SECURE_RANDOM: SecureRandom? = null
        private var sFallbackReader: KeyczarReader? = null
        private var sFallbackReaderInitialized: Boolean = false
        private var sProdReader: KeyczarReader? = null

        init {
            PROD_KEYS_ASSETS_SUBDIR = "keys" + File.separator + "dfe-response-auth"
            FALLBACK_KEYS_FILES_SUBDIR = "keys" + File.separator + "dfe-response-auth-dev"
            try {
                SECURE_RANDOM = SecureRandom.getInstance("SHA1PRNG")
            } catch (ex: NoSuchAlgorithmException) {
                AppLog.e("Could not initialize SecureRandom, SHA1PRNG not supported. %s", ex)
            }

        }

        @Throws(DfeResponseVerifier.DfeResponseVerifierException::class)
        private fun extractResponseSignature(s: String): ByteArray {
            if (TextUtils.isEmpty(s)) {
                AppLog.e("No signing response found.")
                throw DfeResponseVerifier.DfeResponseVerifierException("No signing response found.")
            }
            val split = s.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val length = split.size
            var i = 0
            while (i < length) {
                val trim = split[i].trim { it <= ' ' }
                if (trim.startsWith("signature=")) {
                    return Base64.decode(trim.substring(10), 11)
                }
                ++i
            }
            throw DfeResponseVerifier.DfeResponseVerifierException("Signature not found in response: " + s)
        }

        private fun getFallbackReader(context: Context): KeyczarReader? {
            synchronized(DfeResponseVerifierImpl::class.java) {
                if (!DfeResponseVerifierImpl.sFallbackReaderInitialized) {
                    val file = File(context.filesDir, DfeResponseVerifierImpl.FALLBACK_KEYS_FILES_SUBDIR)
                    if (file.exists()) {
                        DfeResponseVerifierImpl.sFallbackReader = KeyczarFileReader(file.absolutePath)
                    }
                    DfeResponseVerifierImpl.sFallbackReaderInitialized = true
                }
                return DfeResponseVerifierImpl.sFallbackReader
            }
        }

        private fun getProdReader(context: Context): KeyczarReader? {
            synchronized(DfeResponseVerifierImpl::class.java) {
                if (DfeResponseVerifierImpl.sProdReader == null) {
                    DfeResponseVerifierImpl.sProdReader = AndroidKeyczarReader(context.resources, DfeResponseVerifierImpl.PROD_KEYS_ASSETS_SUBDIR)
                }
                return DfeResponseVerifierImpl.sProdReader
            }
        }
    }
}
