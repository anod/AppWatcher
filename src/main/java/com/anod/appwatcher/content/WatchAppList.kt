package com.anod.appwatcher.content

import android.content.Context
import android.support.v4.util.SimpleArrayMap
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppInfoMetadata


/**
 * @author alex
 * *
 * @date 2015-09-19
 */
class WatchAppList(private val listener: Listener?) {
    private var addedApps: SimpleArrayMap<String, Int> = SimpleArrayMap()
    private var contentProvider: DbContentProviderClient? = null

    interface Listener {
        fun onWatchListChangeSuccess(info: AppInfo, newStatus: Int)
        fun onWatchListChangeError(info: AppInfo, error: Int)
    }

    fun attach(context: Context) {
        attach(DbContentProviderClient(context))
    }

    private fun attach(contentProvider: DbContentProviderClient) {
        this.contentProvider = contentProvider
        addedApps = this.contentProvider!!.queryPackagesMap(false)
    }

    fun detach() {
        contentProvider?.close()
        contentProvider = null
    }

    operator fun contains(packageName: String): Boolean {
        return addedApps.containsKey(packageName)
    }

    internal fun addSync(info: AppInfo): Int {
        if (addedApps.containsKey(info.packageName)) {
            return 0
        }

        addedApps.put(info.packageName, -1)
        val existingApp = contentProvider?.queryAppId(info.packageName)
        if (existingApp != null) {
            if (existingApp.status == AppInfoMetadata.STATUS_DELETED) {
                val success = contentProvider?.updateStatus(existingApp.rowId, AppInfoMetadata.STATUS_NORMAL) ?: -1
                return if (success > 0) {
                    RESULT_OK
                } else {
                    ERROR_INSERT
                }
            }
            return ERROR_ALREADY_ADDED
        }

        contentProvider?.insert(info) ?: return ERROR_INSERT
        return RESULT_OK
    }

    fun add(info: AppInfo) {
        if (addedApps.containsKey(info.packageName)) {
            return
        }

        addedApps.put(info.packageName, -1)
        val existingApp = contentProvider?.queryAppId(info.packageName)
        if (existingApp != null) {
            if (existingApp.status == AppInfoMetadata.STATUS_DELETED) {
                val success = contentProvider?.updateStatus(existingApp.rowId, AppInfoMetadata.STATUS_NORMAL) ?: -1
                if (success > 0) {
                    listener?.onWatchListChangeSuccess(info, AppInfoMetadata.STATUS_NORMAL)
                } else {
                    listener?.onWatchListChangeError(info, ERROR_INSERT)
                }
                return
            }
            listener?.onWatchListChangeError(info, ERROR_ALREADY_ADDED)
            return
        }

        insertApp(info)
    }

    fun delete(info: AppInfo) {
        if (!addedApps.containsKey(info.packageName)) return

        val existingApp = contentProvider?.queryAppId(info.packageName)
        if (existingApp != null) {
            val success = contentProvider?.updateStatus(existingApp.rowId, AppInfoMetadata.STATUS_DELETED) ?: -1
            if (success > 0) {
                contentProvider!!.deleteAppTags(existingApp.appId)
                addedApps.remove(info.packageName)
                listener?.onWatchListChangeSuccess(info, AppInfoMetadata.STATUS_DELETED)
            } else {
                listener?.onWatchListChangeError(info, ERROR_DELETE)
            }
        }
    }

    private fun insertApp(info: AppInfo) {
        val uri = contentProvider?.insert(info)

        if (uri == null) {
            listener?.onWatchListChangeError(info, ERROR_INSERT)
        } else {
            val rowId = Integer.parseInt(uri.lastPathSegment)
            info.rowId = rowId
            addedApps.put(info.appId, -1)
            listener?.onWatchListChangeSuccess(info, AppInfoMetadata.STATUS_NORMAL)
        }
    }

    companion object {
        const val RESULT_OK = 0
        const val ERROR_INSERT = 1
        const val ERROR_ALREADY_ADDED = 2
        const val ERROR_DELETE = 3
    }
}
