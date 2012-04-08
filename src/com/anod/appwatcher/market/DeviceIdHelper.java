package com.anod.appwatcher.market;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings.Secure;

import com.anod.appwatcher.Preferences;

public class DeviceIdHelper {
	private static final Uri URI_GSERVICES = Uri.parse("content://com.google.android.gsf.gservices");

	
	public static String getDeviceId(Context context, Preferences prefs) {
		String deviceId = prefs.getDeviceId();
		if (deviceId != null) {
			return deviceId;
		}
		ContentResolver cr = context.getApplicationContext().getContentResolver();

		deviceId = loadDeviceId(cr);
		if (deviceId == null) {
			return Secure.getString(cr, Secure.ANDROID_ID);
		}
		prefs.saveDeviceId(deviceId);
		return deviceId;
	}
	
	 private static String loadDeviceId(ContentResolver cr) {
		Cursor cursor = cr.query(
			URI_GSERVICES, null, null, new String[] { "android_id" }, null
		);
		if (!cursor.moveToFirst() || cursor.getColumnCount() < 2) {
		  return null;
		}
		String str = null;
		try {
		    str = Long.toHexString(Long.parseLong(cursor.getString(1)));
		} catch (NumberFormatException localNumberFormatException) {
			
		}
		return str;
	 }		
}
