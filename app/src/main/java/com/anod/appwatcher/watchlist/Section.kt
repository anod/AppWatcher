// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.watchlist

import com.anod.appwatcher.database.entities.AppListItem
import info.anodsplace.ktx.hashCodeOf

sealed interface SectionHeader {
    object New : SectionHeader
    object RecentlyUpdated : SectionHeader
    object Watching : SectionHeader
    object RecentlyInstalled : SectionHeader
    object OnDevice : SectionHeader
}

sealed interface SectionItem {
    override fun hashCode(): Int

    class Header(val type: SectionHeader) : SectionItem {
        override fun hashCode(): Int {
            return hashCodeOf(javaClass.canonicalName, type)
        }

        override fun equals(other: Any?): Boolean {
            val item = other as? Header ?: return false
            return hashCode() == item.hashCode()
        }
    }

    object Recent : SectionItem {
        override fun hashCode(): Int {
            return hashCodeOf(javaClass.canonicalName)
        }

        override fun equals(other: Any?): Boolean {
            val item = other as? Recent ?: return false
            return hashCode() == item.hashCode()
        }
    }

    object Empty : SectionItem {
        override fun hashCode(): Int {
            return hashCodeOf(javaClass.canonicalName)
        }

        override fun equals(other: Any?): Boolean {
            val item = other as? Empty ?: return false
            return hashCode() == item.hashCode()
        }
    }

    class App(val appListItem: AppListItem, val isLocal: Boolean) : SectionItem {
        override fun hashCode(): Int {
            return hashCodeOf(javaClass.canonicalName, appListItem, isLocal)
        }

        override fun equals(other: Any?): Boolean {
            val item = other as? App ?: return false
            return hashCode() == item.hashCode()
        }
    }

    class OnDevice(val appListItem: AppListItem, var showSelection: Boolean) : SectionItem {
        override fun hashCode(): Int {
            return hashCodeOf(javaClass.canonicalName, appListItem, showSelection)
        }

        override fun equals(other: Any?): Boolean {
            val item = other as? OnDevice ?: return false
            return hashCode() == item.hashCode()
        }
    }
}