package info.anodsplace.framework.app

import android.content.Context
import androidx.annotation.AttrRes
import android.util.TypedValue

/**
 * @author Alex Gavrishev
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