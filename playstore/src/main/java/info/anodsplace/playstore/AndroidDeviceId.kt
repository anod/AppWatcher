package info.anodsplace.playstore

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.Settings.Secure

class AndroidDeviceId(private val context: Context) {
    private val GSERVICES = Uri.parse("content://com.google.android.gsf.gservices")

    fun load(): String {
        val cr = context.applicationContext.contentResolver
        val deviceId = queryDeviceId(cr)

        if (deviceId.isEmpty()) {
            return Secure.getString(cr, Secure.ANDROID_ID)
        }
        return deviceId
    }

    private fun queryDeviceId(cr: ContentResolver): String {
        val cursor = cr.query(GSERVICES, null, null, arrayOf("android_id"), null) ?: return ""
        if (!cursor.moveToFirst() || cursor.columnCount < 2) {
            cursor.close()
            return ""
        }
        var str = ""
        try {
            val androidId = cursor.getString(1) ?: ""
            if (androidId.isNotEmpty()) {
                str = java.lang.Long.toHexString(androidId.toLong())
            }
        } catch (_: NumberFormatException) { }

        cursor.close()
        return str
    }
}