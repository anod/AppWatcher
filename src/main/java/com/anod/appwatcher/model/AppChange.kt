package com.anod.appwatcher.model

/**
 * @author algavris
 * @date 02/09/2017
 */
class AppChange(val appId: String, val versionCode: Int, val versionName: String, val details: String, val uploadDate: String) {

    override fun equals(other: Any?): Boolean {
        other as? AppChange ?: return false
        return when {
            appId != other.appId -> false
            versionCode != other.versionCode -> false
            versionName != other.versionName -> false
            details != other.details -> false
            uploadDate != other.uploadDate -> false
            else -> true
        }
    }

    val isEmpty: Boolean
        get() = appId.isEmpty()
}
