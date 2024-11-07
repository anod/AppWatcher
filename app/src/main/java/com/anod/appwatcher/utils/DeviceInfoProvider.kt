package com.anod.appwatcher.utils

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.opengl.EGL14
import android.opengl.GLES20
import android.os.Build
import finsky.api.DfeDeviceBuild
import finsky.api.DfeDeviceConfiguration
import finsky.api.DfeDeviceInfoProvider
import finsky.api.DfeLocale
import finsky.api.utils.GFS_VERSION_CODE
import finsky.api.utils.PLAY_VERSION_CODE
import finsky.api.utils.PLAY_VERSION_NAME
import info.anodsplace.applog.AppLog
import info.anodsplace.playstore.AndroidDeviceId
import java.util.Locale
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

class DeviceConfiguration(
    private val context: Context
) : DfeDeviceConfiguration {
    private val metrics = context.resources.displayMetrics
    private val config = context.resources.configuration
    private val activityManager: ActivityManager? = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val glExtensionsList: List<String> by lazy {
        getGLExtensionsWithEGL()
    }
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
        get() = glExtensionsList
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

private fun getGLExtensionsWithEGL(): List<String> {
    // Step 3: Choose an EGLConfig
    val attribList = intArrayOf(
        EGL10.EGL_RED_SIZE, 8,
        EGL10.EGL_GREEN_SIZE, 8,
        EGL10.EGL_BLUE_SIZE, 8,
        EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
        EGL10.EGL_NONE
    )
    val numConfig = IntArray(1)
    var extensions: List<String> = emptyList()
    runInGlContext { egl, display ->
        if (egl.eglGetConfigs(display, null, 0, numConfig)) {
            val configs = arrayOfNulls<EGLConfig>(numConfig[0])
            egl.eglChooseConfig(display, attribList, configs, numConfig[0], numConfig)

            // Step 4: Create an OpenGL ES 2.0 context
            val attrib_list = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
            val context = egl.eglCreateContext(display, configs[0], EGL10.EGL_NO_CONTEXT, attrib_list)

            // Step 5: Create a 1x1 pixel surface to make the context current
            val surfaceAttribs = intArrayOf(EGL10.EGL_WIDTH, 1, EGL10.EGL_HEIGHT, 1, EGL10.EGL_NONE)
            val surface = egl.eglCreatePbufferSurface(display, configs[0], surfaceAttribs)

            // Step 6: Make the context current
            egl.eglMakeCurrent(display, surface, surface, context)

            // Step 7: Now that the OpenGL context is active, query for the extensions
            val extensionsString = GLES20.glGetString(GLES20.GL_EXTENSIONS)
            extensions = extensionsString?.split(" ") ?: emptyList()

            // Step 8: Cleanup: Destroy context and terminate EGL
            egl.eglDestroySurface(display, surface)
            egl.eglDestroyContext(display, context)
        }
    }

    // Return the list of extensions
    return extensions
}

private fun runInGlContext(action: (egl: EGL10, display: EGLDisplay) -> Unit) {
    var egl: EGL10? = null
    var display: EGLDisplay? = null
    try {
        // Step 1: Initialize the EGL display connection
        egl = (EGLContext.getEGL() as EGL10)
        display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

        // Step 2: Initialize the EGL display
        val version = IntArray(2)
        egl.eglInitialize(display, version)

        action(egl, display)

        egl.eglTerminate(display)
        egl = null
        display = null
    } catch (e: Exception) {
        AppLog.e("runOnGLThread exception ${e.message}")
        try {
            if (egl != null && display != null) {
                egl.eglTerminate(display)
            }
        } catch (e: Exception) {
            AppLog.e("runOnGLThread terminater ${e.message}")
        }
    }
}