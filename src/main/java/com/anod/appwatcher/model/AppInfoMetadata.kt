package com.anod.appwatcher.model

/**
 * @author alex
 * *
 * @date 2015-02-27
 */
open class AppInfoMetadata internal constructor(var appId: String, var status: Int) {
    companion object {
        val STATUS_NORMAL = 0
        val STATUS_UPDATED = 1
        val STATUS_DELETED = 2
    }
}
