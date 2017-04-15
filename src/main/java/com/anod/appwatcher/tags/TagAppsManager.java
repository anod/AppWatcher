package com.anod.appwatcher.tags;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.util.SimpleArrayMap;

import com.anod.appwatcher.content.DbContentProvider;
import com.anod.appwatcher.content.DbContentProviderClient;
import com.anod.appwatcher.model.Tag;
import com.anod.appwatcher.model.schema.AppTagsTable;
import com.anod.appwatcher.utils.CollectionsUtils;

import java.util.ArrayList;

class TagAppsManager {
    private SimpleArrayMap<String, Boolean> mApps = new SimpleArrayMap<>();
    private boolean mDefaultSelected;
    private final Tag mTag;
    private final Context mContext;

    TagAppsManager(Tag tag, Context context) {
        mTag = tag;
        mContext = context;
    }

    void selectAll(boolean select) {
        mApps.clear();
        mDefaultSelected = select;
    }

    void updateApp(String appId, boolean checked) {
        mApps.put(appId, checked);
    }

    boolean isSelected(String appId) {
        if (mApps.containsKey(appId)) {
            return mApps.get(appId);
        }
        return mDefaultSelected;
    }

    void initSelected(Cursor data) {
        if (data == null || data.getCount() == 0) {
            return;
        }
        data.moveToPosition(-1);
        while (data.moveToNext()) {
            String appId = data.getString(AppTagsTable.Projection.APPID);
            mApps.put(appId, true);
        }
        data.close();
    }

    boolean runImport() {
        ArrayList<String> appIds = new ArrayList<>();
        for (int i = 0; i <mApps.size(); i++)
        {
            if (mApps.valueAt(i)) {
                appIds.add(mApps.keyAt(i));
            }
        }

        DbContentProviderClient cr = new DbContentProviderClient(mContext);
        boolean result = cr.addAppsToTag(appIds, mTag);
        cr.close();

        mContext.getContentResolver().notifyChange(DbContentProvider.APPS_TAG_CONTENT_URI, null);
        return result;
    }
}