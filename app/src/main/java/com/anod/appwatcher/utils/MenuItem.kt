// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

import android.view.MenuItem

/** Fixes checked state being ignored by injecting checked state directly into drawable */
class CheckDrawableWrapper(private val menuItem: MenuItem) : DrawableWrapper(menuItem.icon) {
    // inject checked state into drawable state set
    override fun setState(stateSet: IntArray) = super.setState(
            if (menuItem.isChecked) stateSet + android.R.attr.state_checked else stateSet
    )
}

/** Wrap icon drawable with [CheckDrawableWrapper]. */
fun MenuItem.wrapCheckStateIcon() = apply { icon = CheckDrawableWrapper(this) }