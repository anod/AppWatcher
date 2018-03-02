package com.anod.appwatcher.upgrade

/**
 * @author algavris
 * @date 02-Mar-18
 */
interface UpgradeTask {
    fun onUpgrade(upgrade: UpgradeCheck.Result)
}