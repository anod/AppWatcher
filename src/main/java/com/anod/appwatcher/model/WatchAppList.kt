package com.anod.appwatcher.model

import android.content.Context
import android.support.v4.util.SimpleArrayMap
import com.anod.appwatcher.content.DbContentProviderClient


/**
 * @author alex
 * *
 * @date 2015-09-19
 */
class WatchAppList(private val mListener: WatchAppList.Listener?) {
    private var mAddedApps: SimpleArrayMap<String, Int> = SimpleArrayMap()
    private var mContentProvider: DbContentProviderClient? = null

    interface Listener {
        fun onWatchListChangeSuccess(info: AppInfo, newStatus: Int)
        fun onWatchListChangeError(info: AppInfo, error: Int)
    }

    fun attach(context: Context) {
        attach(DbContentProviderClient(context))
    }

    private fun attach(contentProvider: DbContentProviderClient) {
        mContentProvider = contentProvider
        mAddedApps = mContentProvider!!.queryPackagesMap(false)
    }

    fun detach() {
        mContentProvider?.close()
        mContentProvider = null
    }

    operator fun contains(packageName: String): Boolean {
        return mAddedApps.containsKey(packageName)
    }


    internal fun addSync(info: AppInfo): Int {
        if (mAddedApps.containsKey(info.packageName)) {
            return 0
        }

        mAddedApps.put(info.packageName, -1)
        val existingApp = mContentProvider?.queryAppId(info.packageName)
        if (existingApp != null) {
            if (existingApp.status == AppInfoMetadata.STATUS_DELETED) {
                val success = mContentProvider?.updateStatus(existingApp.rowId, AppInfoMetadata.STATUS_NORMAL) ?: -1
                if (success > 0) {
                    return RESULT_OK
                } else {
                    return ERROR_INSERT
                }
            }
            return ERROR_ALREADY_ADDED
        }

        mContentProvider?.insert(info) ?: return ERROR_INSERT
        return RESULT_OK
    }

    fun add(info: AppInfo) {
        if (mAddedApps.containsKey(info.packageName)) {
            return
        }

        mAddedApps.put(info.packageName, -1)
        val existingApp = mContentProvider?.queryAppId(info.packageName)
        if (existingApp != null) {
            if (existingApp.status == AppInfoMetadata.STATUS_DELETED) {
                val success = mContentProvider?.updateStatus(existingApp.rowId, AppInfoMetadata.STATUS_NORMAL) ?: -1
                if (success > 0) {
                    mListener?.onWatchListChangeSuccess(info, AppInfoMetadata.STATUS_NORMAL)
                } else {
                    mListener?.onWatchListChangeError(info, ERROR_INSERT)
                }
                return
            }
            mListener?.onWatchListChangeError(info, ERROR_ALREADY_ADDED)
            return
        }

        insertApp(info)
    }

    fun delete(info: AppInfo) {
        if (!mAddedApps.containsKey(info.packageName)) return

        val existingApp = mContentProvider?.queryAppId(info.packageName)
        if (existingApp != null) {
            val success = mContentProvider?.updateStatus(existingApp.rowId, AppInfoMetadata.STATUS_DELETED) ?: -1
            if (success > 0) {
                mContentProvider!!.deleteAppTags(existingApp.appId)
                mAddedApps.remove(info.packageName)
                mListener?.onWatchListChangeSuccess(info, AppInfoMetadata.STATUS_DELETED)
            } else {
                mListener?.onWatchListChangeError(info, ERROR_DELETE)
            }
        }
    }

    private fun insertApp(info: AppInfo) {
        val uri = mContentProvider?.insert(info)

        if (uri == null) {
            mListener?.onWatchListChangeError(info, ERROR_INSERT)
        } else {
            val rowId = Integer.parseInt(uri.lastPathSegment)
            info.rowId = rowId
            mAddedApps.put(info.appId, -1)
            mListener?.onWatchListChangeSuccess(info, AppInfoMetadata.STATUS_NORMAL)
        }
    }

    companion object {
        const val RESULT_OK = 0
        const val ERROR_INSERT = 1
        const val ERROR_ALREADY_ADDED = 2
        const val ERROR_DELETE = 3
    }
}
