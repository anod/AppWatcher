package info.anodsplace.appwatcher.framework

import android.content.Context
import android.support.annotation.AttrRes
import android.util.TypedValue

/**
 * @author algavris
 * @date 04/12/2017
 */
object ThemeCompat {

    fun getColor(context: Context, @AttrRes resId: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(resId, typedValue, true)
        return typedValue.data
    }
}