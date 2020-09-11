package com.anod.appwatcher.utils

import java.security.MessageDigest

/**
 * @author Alex Gavrishev
 * @date 20/02/2018
 */
class Hash(private val source: String, private val type: String) {

    companion object {
        fun sha512(source: String) = Hash(source, "SHA-512")
        fun sha256(source: String) = Hash(source, "SHA-256")
        fun sha1(source: String) = Hash(source, "SHA-1")
    }

    val encoded: String
        get() {
            val bytes = MessageDigest.getInstance(this.type).digest(this.source.toByteArray())
            val result = StringBuilder(bytes.size * 2)
            val hexChars = "0123456789ABCDEF"
            bytes.forEach {
                val i = it.toInt()
                result.append(hexChars[i shr 4 and 0x0f])
                result.append(hexChars[i and 0x0f])
            }
            return result.toString()
        }
}

fun hashCodeOf(vararg input: Any): Int {
    var hashCode = 1
    for (item in input) {
        hashCode = 31 * hashCode + item.hashCode()
    }
    return hashCode
}