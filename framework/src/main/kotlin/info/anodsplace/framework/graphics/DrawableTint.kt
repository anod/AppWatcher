package info.anodsplace.framework.graphics

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat

/**
 * @author Alex Gavrishev
 * *
 * @date 06/05/2017.
 */
class DrawableTint(private val res: Resources, @DrawableRes private val drawableRes: Int,private val theme: Resources.Theme) {
    fun apply(@ColorRes tint: Int): Drawable {
        val d = ResourcesCompat.getDrawable(res, drawableRes, theme)
        val wrapped = DrawableCompat.wrap(d!!)
        val color = ResourcesCompat.getColor(res, tint, theme)

        DrawableCompat.setTint(wrapped, color)
        return d
    }
}
