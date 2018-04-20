package info.anodsplace.playstore

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.Settings.Secure

interface DeviceIdStorage {
    var deviceId: String
}

class DeviceId(private val context: Context,private val storage: DeviceIdStorage) {
    private val GSERVICES = Uri.parse("content://com.google.android.gsf.gservices")

    fun load(): String {
        var deviceId = storage.deviceId
        if (deviceId.isNotEmpty()) {
            return deviceId
        }
        val cr = context.applicationContext.contentResolver
        deviceId = queryDeviceId(cr)

        if (deviceId.isEmpty()) {
            return Secure.getString(cr, Secure.ANDROID_ID)
        }
        storage.deviceId = deviceId
        return deviceId
    }

    private fun queryDeviceId(cr: ContentResolver): String {
        val cursor = cr.query(GSERVICES, null, null, arrayOf("android_id"), null
        ) ?: return ""
        if (!cursor.moveToFirst() || cursor.columnCount < 2) {
            cursor.close()
            return ""
        }
        var str = ""
        try {
            str = java.lang.Long.toHexString(cursor.getString(1).toLong())
        } catch (localNumberFormatException: NumberFormatException) { }

        cursor.close()
        return str
    }
}
