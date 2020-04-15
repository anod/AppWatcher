// Copyright (c) 2020. Alex Gavrishev
package info.anodsplace.framework.view

import android.os.SystemClock
import android.view.View

inline fun View.setOnSafeClickListener(crossinline onSafeClick: ((View) -> Unit)) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}


class SafeClickListener(
        private inline val onSafeClick: (View) -> Unit
) : View.OnClickListener {
    private var lastClickTime = 0L
    private val clickTimeInterval = 700L

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < clickTimeInterval) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        onSafeClick(v)
    }
}