package com.anod.appwatcher.sync

import com.anod.appwatcher.preferences.Preferences

class SyncNotificationFilter(
    private val filterInstalled: Boolean,
    private val filterInstalledUpToDate: Boolean,
    private val filterNoChanges: Boolean
) {

    constructor(prefs: Preferences)
            : this(!prefs.isNotifyInstalled, !prefs.isNotifyInstalledUpToDate, !prefs.isNotifyNoChanges)

    val hasFilters: Boolean
        get() = (filterInstalled || filterInstalledUpToDate || filterNoChanges)

    fun apply(updatedApps: List<UpdatedApp>): List<UpdatedApp> {
        return updatedApps.filter append@{
            if (filterInstalled) {
                if (it.installedVersionCode > 0) {
                    return@append false
                }
            } else if (filterInstalledUpToDate) {
                if (it.installedVersionCode > 0 && it.versionNumber <= it.installedVersionCode) {
                    return@append false
                }
            }
            if (filterNoChanges) {
                if (it.noNewDetails) {
                    return@append false
                }
            }
            true
        }
    }
}