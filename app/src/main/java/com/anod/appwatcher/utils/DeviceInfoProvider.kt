package com.anod.appwatcher.utils

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.anod.appwatcher.preferences.Preferences
import com.google.android.gms.common.util.ClientLibraryUtils.getPackageInfo
import finsky.api.DfeDeviceBuild
import finsky.api.DfeDeviceConfiguration
import finsky.api.DfeDeviceInfoProvider
import finsky.api.DfeLocale
import finsky.api.utils.GFS_VERSION_CODE
import finsky.api.utils.PLAY_VERSION_CODE
import finsky.api.utils.PLAY_VERSION_NAME
import info.anodsplace.playstore.AndroidDeviceId
import java.util.Locale

class DeviceConfiguration(
    private val context: Context
) : DfeDeviceConfiguration {
    private val metrics = context.resources.displayMetrics
    private val config = context.resources.configuration
    private val activityManager: ActivityManager? = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    override val touchScreen: Int
        get() = config.touchscreen
    override val keyboard: Int
        get() = config.keyboard
    override val navigation: Int
        get() = config.navigation
    override val screenLayout: Int
        get() = config.screenLayout and 15
    override val hasHardKeyboard: Boolean
        get() = config.keyboard == Configuration.KEYBOARD_QWERTY
    override val hasFiveWayNavigation: Boolean
        get() = config.navigation == Configuration.NAVIGATIONHIDDEN_YES
    override val lowRamDevice: Int
        get() = 0
    override val maxNumOfCPUCores: Int
        get() = 8
    override val totalMemoryBytes: Long
        get() = 8589935000
    override val deviceClass: Int
        get() = 0
    override val screenDensity: Int
        get() = metrics.densityDpi
    override val screenWidth: Int
        get() = metrics.widthPixels
    override val screenHeight: Int
        get() = metrics.heightPixels
    override val sharedLibraries: List<String>
        get() = context.packageManager.systemSharedLibraryNames?.toList() ?: emptyList()
    override val features: List<String> = context
        .packageManager
        .systemAvailableFeatures
        .mapNotNull { it.name }
    override val locales: List<String>
        get() = context.assets.locales.mapNotNull { it.replace("-", "_") }
    override val glEsVersion: Int
        get() = activityManager?.deviceConfigurationInfo?.reqGlEsVersion ?: 0
    override val glExtensions: List<String>
        get() = emptyList()
    override val isWideScreen: Boolean = false
}

class DeviceBuild : DfeDeviceBuild {
    override val id: String = Build.ID
    override val fingerprint: String = Build.FINGERPRINT
    override val hardware: String = Build.HARDWARE
    override val brand: String = Build.BRAND
    override val radio: String = Build.getRadioVersion() ?: "unknown"
    override val bootloader: String = Build.BOOTLOADER
    override val device: String = Build.DEVICE
    override val sdkVersion: Int = Build.VERSION.SDK_INT
    override val releaseVersion: String = Build.VERSION.RELEASE
    override val model: String = Build.MODEL
    override val manifacturer: String = Build.MANUFACTURER
    override val product: String = Build.PRODUCT
    override val abis: Array<String> = Build.SUPPORTED_ABIS
}

class DeviceInfoProvider(
    private val context: Context
): DfeDeviceInfoProvider {
    private val GOOGLE_SERVICES_PACKAGE_ID = "com.google.android.gms"
    private val GOOGLE_VENDING_PACKAGE_ID = "com.android.vending"

    private val androidDeviceId: String by lazy {
        AndroidDeviceId(context).load()
    }
    private val localGsfVersion: Long = GFS_VERSION_CODE /* by lazy {
        getPackageInfo(context, GOOGLE_SERVICES_PACKAGE_ID)?.let {
            PackageInfoCompat.getLongVersionCode(it)
        } ?: GFS_VERSION_CODE
    } */
    private val playVersion: Pair<Long, String> = Pair(PLAY_VERSION_CODE, PLAY_VERSION_NAME) /* by lazy {
        getPackageInfo(context, GOOGLE_VENDING_PACKAGE_ID)?.let {
            Pair(PackageInfoCompat.getLongVersionCode(it), it.versionName ?: PLAY_VERSION_NAME)
        } ?: Pair(PLAY_VERSION_CODE, PLAY_VERSION_NAME)
    } */

    override val deviceId: String
        get() = androidDeviceId
    override val simOperator: String
        get() = "38"
    override val cellOperator: String
        get() = "310"
    override val roaming: String
        get() = "mobile-notroaming"
    override val build: DfeDeviceBuild = DeviceBuild()
    override val client: String
        get() = "android-google"
    override val gsfVersion: Long
        get() = localGsfVersion
    override val otaInstalled: Boolean
        get() = false
    override val locale: DfeLocale
        get() = Locale.getDefault().let {
            DfeLocale(it.language, it.country, it.toString())
        }
    override val timeZone: String
        get() = "UTC-10"
    override val configuration: DfeDeviceConfiguration = DeviceConfiguration(context)
    override val playVersionCode: Long
        get() = playVersion.first
    override val playVersionName: String
        get() = playVersion.second
}