package com.anod.appwatcher.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PackageEnabledStateTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun defaultStateUsesManifestState() {
        assertTrue(isPackageEnabled(true, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT))
        assertFalse(isPackageEnabled(false, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT))
    }

    @Test
    fun explicitEnabledStateOverridesManifestState() {
        assertTrue(isPackageEnabled(false, PackageManager.COMPONENT_ENABLED_STATE_ENABLED))
    }

    @Test
    fun disabledStatesAreNotEnabled() {
        assertFalse(isPackageEnabled(true, PackageManager.COMPONENT_ENABLED_STATE_DISABLED))
        assertFalse(isPackageEnabled(true, PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER))
        assertFalse(isPackageEnabled(true, PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED))
    }

    @Test
    fun unknownStateUsesResolvedApplicationState() {
        assertTrue(isPackageEnabled(true, Int.MAX_VALUE))
        assertFalse(isPackageEnabled(false, Int.MAX_VALUE))
    }

    @Test
    fun packageManagerReadsEnabledSetting() {
        val packageName = "enabled.state.test"
        installPackage(packageName, manifestEnabled = true)

        assertTrue(context.packageManager.isPackageEnabled(packageName))

        context.packageManager.setApplicationEnabledSetting(
            packageName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED,
            PackageManager.DONT_KILL_APP
        )
        assertFalse(context.packageManager.isPackageEnabled(packageName))

        context.packageManager.setApplicationEnabledSetting(
            packageName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        assertTrue(context.packageManager.isPackageEnabled(packageName))
    }

    @Test
    fun packageManagerUsesManifestStateByDefault() {
        val packageName = "manifest.disabled.test"
        installPackage(packageName, manifestEnabled = false)

        assertFalse(context.packageManager.isPackageEnabled(packageName))
    }

    @Test
    fun packageStateCacheRefreshesOnlyChangedState() = runBlocking {
        val packageName = context.packageName
        val cache = PackageStateCache(context.packageManager)

        val initialStates = cache.load(listOf(packageName))
        assertTrue(initialStates.getValue(packageName).isEnabled)
        assertFalse(cache.refresh(packageName).changed)

        context.packageManager.setApplicationEnabledSetting(
            packageName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED,
            PackageManager.DONT_KILL_APP
        )
        val refresh = cache.refresh(packageName)

        assertTrue(refresh.changed)
        assertFalse(refresh.current.isEnabled)
        assertFalse(cache.load(listOf(packageName)).getValue(packageName).isEnabled)
    }

    @Test
    fun packageStateCacheLoadsRequestedPackagesMissingFromInitialSnapshot() = runBlocking {
        val initialPackageName = context.packageName
        val additionalPackageName = "cached.additional.state.test"
        installPackage(additionalPackageName, manifestEnabled = true)
        val cache = PackageStateCache(context.packageManager)

        cache.load(listOf(initialPackageName))
        val updatedStates = cache.load(listOf(additionalPackageName))

        assertTrue(updatedStates.getValue(initialPackageName).isEnabled)
        assertTrue(updatedStates.getValue(additionalPackageName).isEnabled)
    }

    @Test
    fun packageStateCacheStoresNotInstalledRequestedPackages() = runBlocking {
        val packageName = "cached.not.installed.test"
        val cache = PackageStateCache(context.packageManager)

        val states = cache.load(listOf(packageName))

        assertFalse(states.getValue(packageName).isInstalled)
    }

    private fun installPackage(packageName: String, manifestEnabled: Boolean) {
        shadowOf(context.packageManager).installPackage(
            PackageInfo().apply {
                this.packageName = packageName
                applicationInfo = ApplicationInfo().apply {
                    this.packageName = packageName
                    enabled = manifestEnabled
                }
            }
        )
    }
}