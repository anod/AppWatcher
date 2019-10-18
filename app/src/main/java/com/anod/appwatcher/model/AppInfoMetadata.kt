package com.anod.appwatcher.model

/**
 * @author alex
 * *
 * @date 2015-02-27
 */
open class AppInfoMetadata internal constructor(var appId: String, var status: Int) {
    companion object {
        const val STATUS_NORMAL = 0
        const val STATUS_UPDATED = 1
        const val STATUS_DELETED = 2
    }
}
