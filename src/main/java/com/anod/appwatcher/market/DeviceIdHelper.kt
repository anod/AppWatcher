package com.anod.appwatcher.market

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.Settings.Secure

import com.anod.appwatcher.Preferences

object DeviceIdHelper {
    private val URI_GSERVICES = Uri.parse("content://com.google.android.gsf.gservices")

    fun getDeviceId(context: Context, prefs: Preferences): String {
        var deviceId = prefs.deviceId
        if (deviceId.isNotEmpty()) {
            return deviceId
        }
        val cr = context.applicationContext.contentResolver
        deviceId = loadDeviceId(cr)

        if (deviceId.isEmpty()) {
            return Secure.getString(cr, Secure.ANDROID_ID)
        }
        prefs.deviceId = deviceId
        return deviceId
    }

    private fun loadDeviceId(cr: ContentResolver): String {
        val cursor = cr.query(URI_GSERVICES, null, null, arrayOf("android_id"), null
        ) ?: return ""
        if (!cursor.moveToFirst() || cursor.columnCount < 2) {
            cursor.close()
            return ""
        }
        var str: String = ""
        try {
            str = java.lang.Long.toHexString(cursor.getString(1).toLong())
        } catch (localNumberFormatException: NumberFormatException) { }

        cursor.close()
        return str
    }
}
