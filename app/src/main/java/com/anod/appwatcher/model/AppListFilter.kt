package com.anod.appwatcher.model

import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.ktx.hashCodeOf

/**
 * @author alex
 * *
 * @date 8/4/14.
 */

interface AppListFilter {
    val filterId: Int
    fun filterRecord(
        versionCode: Int,
        packageInfo: InstalledApps.Info,
        isPackageEnabled: Boolean
    ): Boolean

    class All : AppListFilter {
        override val filterId = Filters.ALL
        override fun filterRecord(
            versionCode: Int,
            packageInfo: InstalledApps.Info,
            isPackageEnabled: Boolean
        ): Boolean = false
        override fun hashCode(): Int = hashCodeOf(filterId)
        override fun equals(other: Any?): Boolean = (other as? Installed)?.hashCode() == hashCode()
    }

    class Installed : AppListFilter {
        override val filterId = Filters.INSTALLED
        override fun filterRecord(
            versionCode: Int,
            packageInfo: InstalledApps.Info,
            isPackageEnabled: Boolean
        ): Boolean = !packageInfo.isInstalled

        override fun hashCode(): Int = hashCodeOf(filterId)
        override fun equals(other: Any?): Boolean = (other as? Installed)?.hashCode() == hashCode()
    }

    class Uninstalled : AppListFilter {
        override val filterId = Filters.UNINSTALLED
        override fun filterRecord(
            versionCode: Int,
            packageInfo: InstalledApps.Info,
            isPackageEnabled: Boolean
        ): Boolean = packageInfo.isInstalled

        override fun hashCode(): Int = hashCodeOf(filterId)
        override fun equals(other: Any?): Boolean = (other as? Installed)?.hashCode() == hashCode()
    }

    class Updatable : AppListFilter {
        override val filterId = Filters.UPDATABLE
        override fun filterRecord(
            versionCode: Int,
            packageInfo: InstalledApps.Info,
            isPackageEnabled: Boolean
        ): Boolean = !(
            packageInfo.isInstalled &&
                isPackageEnabled &&
                packageInfo.isUpdatable(versionCode)
            )

        override fun hashCode(): Int = hashCodeOf(filterId)
        override fun equals(other: Any?): Boolean = (other as? Installed)?.hashCode() == hashCode()
    }
}