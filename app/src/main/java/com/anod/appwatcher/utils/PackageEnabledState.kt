package com.anod.appwatcher.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

fun PackageManager.isPackageEnabled(packageName: String): Boolean {
    val applicationInfo = try {
        getApplicationInfo(packageName, 0)
    } catch (_: PackageManager.NameNotFoundException) {
        return false
    }
    val enabledSetting = try {
        getApplicationEnabledSetting(packageName)
    } catch (_: IllegalArgumentException) {
        return false
    }
    return isPackageEnabled(applicationInfo.enabled, enabledSetting)
}

internal fun isPackageEnabled(manifestEnabled: Boolean, enabledSetting: Int): Boolean = when (enabledSetting) {
    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> manifestEnabled
    PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> false
    else -> manifestEnabled
}

data class PackageState(
    val packageInfo: InstalledApps.Info,
    val isEnabled: Boolean
) {
    val isInstalled: Boolean = packageInfo.isInstalled

    companion object {
        val NotInstalled = PackageState(
            packageInfo = InstalledApps.Info(versionCode = 0, versionName = ""),
            isEnabled = true
        )
    }
}

fun interface PackageStateProvider {
    suspend fun load(packageNames: Collection<String>): Map<String, PackageState>
}

data class PackageStateRefresh(
    val current: PackageState,
    val changed: Boolean
)

class PackageStateCache(private val packageManager: PackageManager) : PackageStateProvider {
    private val mutex = Mutex()

    @Volatile
    private var cachedStates: Map<String, PackageState>? = null

    override suspend fun load(packageNames: Collection<String>): Map<String, PackageState> {
        if (packageNames.isEmpty()) {
            return emptyMap()
        }
        cachedStates?.let { states ->
            if (packageNames.all(states::containsKey)) {
                return states
            }
        }
        return mutex.withLock {
            val currentStates = cachedStates
            if (currentStates == null) {
                loadInstalledPackageStates(packageNames).also {
                    cachedStates = it
                }
            } else {
                val missingPackageNames = packageNames.distinct().filterNot { it in currentStates }
                if (missingPackageNames.isEmpty()) {
                    currentStates
                } else {
                    withContext(Dispatchers.IO) {
                        val updatedStates = currentStates.toMutableMap()
                        for (packageName in missingPackageNames) {
                            updatedStates[packageName] = packageManager.loadPackageState(packageName)
                        }
                        updatedStates
                    }.also {
                        cachedStates = it
                    }
                }
            }
        }
    }

    suspend fun reload(packageNames: Collection<String>): Map<String, PackageState> = mutex.withLock {
        loadInstalledPackageStates(packageNames).also {
            cachedStates = it
        }
    }

    suspend fun refresh(packageName: String): PackageStateRefresh = mutex.withLock {
        val current = withContext(Dispatchers.IO) {
            packageManager.loadPackageState(packageName)
        }
        val states = cachedStates
        if (states == null) {
            return@withLock PackageStateRefresh(current = current, changed = true)
        }
        val previous = states[packageName] ?: PackageState.NotInstalled
        if (states[packageName] != current) {
            cachedStates = states.toMutableMap().apply {
                put(packageName, current)
            }
        }
        PackageStateRefresh(current = current, changed = previous != current)
    }

    suspend fun clear() {
        mutex.withLock {
            cachedStates = null
        }
    }

    private suspend fun loadInstalledPackageStates(
        packageNames: Collection<String> = emptyList()
    ): Map<String, PackageState> = withContext(Dispatchers.IO) {
        val installedStates = packageManager.installedPackages().associateTo(mutableMapOf()) { packageInfo ->
            packageInfo.packageName to packageInfo.toPackageState()
        }
        for (packageName in packageNames) {
            if (packageName !in installedStates) {
                installedStates[packageName] = packageManager.loadPackageState(packageName)
            }
        }
        installedStates
    }

    private fun PackageInfo.toPackageState(): PackageState {
        val applicationEnabled = applicationInfo?.enabled ?: packageManager.isPackageEnabled(packageName)
        return PackageState(
            packageInfo = InstalledApps.Info(
                versionCode = PackageInfoCompat.getLongVersionCode(this).toInt(),
                versionName = versionName.orEmpty()
            ),
            isEnabled = applicationEnabled
        )
    }

    private fun PackageManager.loadPackageState(packageName: String): PackageState {
        val packageInfo = try {
            @Suppress("DEPRECATION")
            getPackageInfo(packageName, PackageManager.MATCH_DISABLED_COMPONENTS)
        } catch (_: PackageManager.NameNotFoundException) {
            return PackageState.NotInstalled
        }
        val applicationInfo = packageInfo.applicationInfo
        val enabled = if (applicationInfo == null) {
            isPackageEnabled(packageName)
        } else {
            val enabledSetting = try {
                getApplicationEnabledSetting(packageName)
            } catch (_: IllegalArgumentException) {
                return PackageState.NotInstalled
            }
            isPackageEnabled(applicationInfo.enabled, enabledSetting)
        }
        return PackageState(
            packageInfo = InstalledApps.Info(
                versionCode = PackageInfoCompat.getLongVersionCode(packageInfo).toInt(),
                versionName = packageInfo.versionName.orEmpty()
            ),
            isEnabled = enabled
        )
    }

    @Suppress("DEPRECATION")
    private fun PackageManager.installedPackages(): List<PackageInfo> =
        getInstalledPackages(PackageManager.MATCH_DISABLED_COMPONENTS)
}

internal fun Map<String, PackageState>.stateFor(packageName: String): PackageState =
    get(packageName) ?: PackageState.NotInstalled