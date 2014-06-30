package com.anod.appwatcher.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.anod.appwatcher.market.MarketInfo;

public class IntentUtils {
	private static final String SCHEME = "package";
	private static final String APP_PKG_NAME_22 = "pkg";
	private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
	private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

	/**
	 * 
	 * @param packageName
	 * @return
	 */
	public static Intent createApplicationDetailsIntent(String packageName) {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) { // above 2.3
            intent = new Intent(
            	Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            	Uri.fromParts(SCHEME, packageName, null)
            );
        } else { // below 2.3
        	intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME,  APP_DETAILS_CLASS_NAME);
            intent.putExtra(APP_PKG_NAME_22, packageName);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return intent;
    }
    

	/**
	 * @param pkg
	 * @return
	 */
	public static Intent createPlayStoreIntent(String pkg) {
		String url = String.format(MarketInfo.URL_PLAY_STORE, pkg);
		Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse(url));
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		return intent;
	}
    
	
}
