package info.anodsplace.framework.view

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import com.google.android.material.appbar.AppBarLayout
import info.anodsplace.framework.R

//--------------------------------------------------------------------------------------------------
// Copyright (c) Microsoft Corporation. All rights reserved.
//--------------------------------------------------------------------------------------------------

class ThemableAppBarLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : AppBarLayout(context, attrs, overrideStyle(defStyleAttr)) {

    companion object {
        var forceNightMode = false

        private fun overrideStyle(style: Int): Int {
            if (forceNightMode) {
                return R.style.ActionBarNight
            }
            return style
        }
    }
}