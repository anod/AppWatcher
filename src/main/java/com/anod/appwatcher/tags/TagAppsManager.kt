package com.anod.appwatcher.tags

import android.content.Context
import android.database.Cursor
import android.support.v4.util.SimpleArrayMap
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.model.schema.AppTagsTable
import java.util.*

internal class TagAppsManager(private val mTag: Tag, private val mContext: Context) {
    private val mApps = SimpleArrayMap<String, Boolean>()
    private var mDefaultSelected: Boolean = false

    fun selectAll(select: Boolean) {
        mApps.clear()
        mDefaultSelected = select
    }

    fun updateApp(appId: String, checked: Boolean) {
        mApps.put(appId, checked)
    }

    fun isSelected(appId: String): Boolean {
        if (mApps.containsKey(appId)) {
            return mApps.get(appId)
        }
        return mDefaultSelected
    }

    fun initSelected(data: Cursor?) {
        if (data == null || data.count == 0) {
            return
        }
        data.moveToPosition(-1)
        while (data.moveToNext()) {
            val appId = data.getString(AppTagsTable.Projection.APPID)
            mApps.put(appId, true)
        }
        data.close()
    }

    fun runImport(): Boolean {
        val appIds = (0..mApps.size() - 1)
                .filter { mApps.valueAt(it) }
                .map { mApps.keyAt(it) }

        val cr = DbContentProviderClient(mContext)
        val result = cr.setAppsToTag(appIds, mTag.id)
        cr.close()

        mContext.contentResolver.notifyChange(DbContentProvider.APPS_TAG_CONTENT_URI, null)
        return result
    }
}