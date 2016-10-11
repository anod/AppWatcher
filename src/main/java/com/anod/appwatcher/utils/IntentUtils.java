package com.anod.appwatcher.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.anod.appwatcher.fragments.AppWatcherListFragment;
import com.anod.appwatcher.market.MarketInfo;

import info.anodsplace.android.log.AppLog;

public class IntentUtils {
    private static final String SCHEME = "package";
    private static final String APP_PKG_NAME_22 = "pkg";
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

    public static Intent createApplicationDetailsIntent(String packageName) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) { // above 2.3
            intent = new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts(SCHEME, packageName, null)
            );
        } else { // below 2.3
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
            intent.putExtra(APP_PKG_NAME_22, packageName);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return intent;
    }

    public static Intent createPlayStoreIntent(String pkg) {
        String url = String.format(MarketInfo.URL_PLAY_STORE, pkg);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        return intent;
    }

    public static Intent createMyAppsIntent(boolean update)
    {
        Intent marketIntent = new Intent("com.google.android.finsky.VIEW_MY_DOWNLOADS")
                .setComponent(new ComponentName("com.android.vending",
                        "com.google.android.finsky.activities.MainActivity"))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (update) {
            marketIntent.putExtra("trigger_update_all", true);
        }
        return marketIntent;
    }

    public static Intent createUninstallIntent(String packageName) {
        return new Intent(Intent.ACTION_UNINSTALL_PACKAGE, Uri.fromParts(SCHEME, packageName, null));
    }

    public static void startActivitySafely(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (Exception e)
        {
            AppLog.e(e);
            Toast.makeText(context, "Cannot start activity: "+intent.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
