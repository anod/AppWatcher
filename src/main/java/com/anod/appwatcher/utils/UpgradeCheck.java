package com.anod.appwatcher.utils;

import com.anod.appwatcher.BuildConfig;
import com.anod.appwatcher.Preferences;

/**
 * @author algavris
 * @date 08/10/2016.
 */

public class UpgradeCheck {

    public static boolean isNewVersion(Preferences preferences)
    {
        if (preferences.getVersionCode() > BuildConfig.VERSION_CODE)
        {
            preferences.saveVersionCode(BuildConfig.VERSION_CODE);
            return true;
        }
        return false;
    }
}
