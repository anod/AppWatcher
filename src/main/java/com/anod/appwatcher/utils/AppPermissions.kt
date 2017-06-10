package com.anod.appwatcher.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

/**
 * @author algavris
 * *
 * @date 08/05/2016.
 */
object AppPermissions {
    val REQUEST_STORAGE_WRITE = 300
    val REQUEST_STORAGE_READ = 301

    fun isGranted(activity: Activity, permissionName: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permissionName) == PackageManager.PERMISSION_GRANTED
    }

    fun request(activity: Activity, permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    interface PermissionResult {
        fun granted()

        fun denied()
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray, checkPermission: Int, result: PermissionResult) {
        if (requestCode == checkPermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                result.granted()
            } else {
                result.denied()
            }
        }
    }
}
