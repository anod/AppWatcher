package com.anod.appwatcher.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * @author algavris
 * @date 08/05/2016.
 */
public class AppPermissions {
    public static final int REQUEST_STORAGE_WRITE = 300;
    public static final int REQUEST_STORAGE_READ = 301;

    public static boolean isGranted(Activity activity, String permissionName) {
        return ContextCompat.checkSelfPermission(activity, permissionName) == PackageManager.PERMISSION_GRANTED;
    }

    public static void request(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public interface PermissionResult {
        void granted();

        void denied();
    }

    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, int checkPermission, PermissionResult result) {
        if (requestCode == checkPermission) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                result.granted();
            } else {
                result.denied();
            }
        }
    }
}
