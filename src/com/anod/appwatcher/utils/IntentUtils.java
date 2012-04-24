package com.anod.appwatcher.utils;

import com.anod.appwatcher.market.MarketInfo;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

public class IntentUtils {

	
    public static Intent createApplicationDetailsIntent(String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null));
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
