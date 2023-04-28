// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.database.entities.cleanChangeHtml
import info.anodsplace.framework.content.InstalledApps
import info.anodsplace.ktx.hashCodeOf

sealed interface SectionHeader {
    object New : SectionHeader
    object RecentlyDiscovered : SectionHeader
    object Watching : SectionHeader
    object RecentlyInstalled : SectionHeader
    object OnDevice : SectionHeader
}

sealed interface SectionItem {
    override fun hashCode(): Int
    val sectionKey: String
    val contentType: String

    class Header(val type: SectionHeader) : SectionItem {
        override val sectionKey = "header:${hashCode()}"
        override val contentType = "Header"
        override fun hashCode(): Int {
            return hashCodeOf("SectionItem.Header", type)
        }

        override fun equals(other: Any?): Boolean {
            val item = other as? Header ?: return false
            return hashCode() == item.hashCode()
        }
    }

    object Recent : SectionItem {
        override val sectionKey = "recent:${hashCode()}"
        override val contentType = "Recent"
        override fun hashCode(): Int {
            return hashCodeOf("SectionItem.Recent")
        }

        override fun equals(other: Any?): Boolean {
            val item = other as? Recent ?: return false
            return hashCode() == item.hashCode()
        }
    }

    object Empty : SectionItem {
        override val sectionKey = "empty:${hashCode()}"
        override val contentType = "Empty"
        override fun hashCode(): Int {
            return hashCodeOf("SectionItem.Empty")
        }

        override fun equals(other: Any?): Boolean {
            val item = other as? Empty ?: return false
            return hashCode() == item.hashCode()
        }
    }

    class App(val appListItem: AppListItem, val isLocal: Boolean, val packageInfo: InstalledApps.Info) : SectionItem {
        override val sectionKey = "app-${appListItem.app.rowId}:${hashCode()}"
        override val contentType = "App"
        val changesHtml: String = appListItem.cleanChangeHtml()

        override fun hashCode(): Int {
            return hashCodeOf("SectionItem.App", appListItem, isLocal)
        }

        override fun equals(other: Any?): Boolean {
            val item = other as? App ?: return false
            return hashCode() == item.hashCode()
        }
    }

    class OnDevice(val appListItem: AppListItem, var showSelection: Boolean, val packageInfo: InstalledApps.Info) : SectionItem {
        override val sectionKey = "ondevice-{appListItem.app.rowId}:${hashCode()}"
        override val contentType = "OnDevice"
        val changesHtml: String = appListItem.cleanChangeHtml()

        override fun hashCode(): Int {
            return hashCodeOf("SectionItem.OnDevice", appListItem, showSelection)
        }

        override fun equals(other: Any?): Boolean {
            val item = other as? OnDevice ?: return false
            return hashCode() == item.hashCode()
        }
    }
}