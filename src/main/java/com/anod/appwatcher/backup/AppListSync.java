package com.anod.appwatcher.backup;

import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.model.AppInfoMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alex on 7/13/14.
 */
public class AppListSync {
    private List<AppInfo> mNewItems;
    private List<String> mStatusUpdateItems;

    public AppListSync() {
        mNewItems = new ArrayList<AppInfo>();
        mStatusUpdateItems = new ArrayList<String>();
    }

    public List<AppInfo> getNewItems() {
        return mNewItems;
    }

    public List<String> getStatusUpdateItems() {
        return mStatusUpdateItems;
    }

    public void sync(HashMap<String, AppInfoMetadata> existing, List<AppInfo> backup) {

        for(int idx = 0; idx < backup.size(); idx++) {

            AppInfo backupItem = backup.get(idx);
            AppInfoMetadata metaData = existing.get(backupItem.getAppId());

            if (metaData == null) {
                // Insert new item
                mNewItems.add(backupItem);
            } else {
                // THe status of the backup item is newer
                if (backupItem.getModifyTime() > metaData.getModifyTime()) {
                    // The item was added
                    if (metaData.getStatus() == AppInfoMetadata.STATUS_DELETED) {
                        // schedule status update
                        mStatusUpdateItems.add(metaData.getAppId());
                    }
                }
            }
        }

    }

}
