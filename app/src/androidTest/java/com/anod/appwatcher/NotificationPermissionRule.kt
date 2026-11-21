package com.anod.appwatcher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource

/**
 * Lets the app post notifications on Android 13 and later, which deny them to every fresh install.
 *
 * Without the permission the update check is never scheduled and the sample notification is never
 * shown, so the settings that drive them cannot be observed. Apply it outside the Compose rule, so
 * the settings screen reads the allowed state when it is first composed.
 *
 * A device that already allows notifications is left alone. The grant is not undone afterwards,
 * because revoking a runtime permission kills the process that holds it, which is the one running
 * the tests.
 */
class NotificationPermissionRule : ExternalResource() {
    override fun before() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}