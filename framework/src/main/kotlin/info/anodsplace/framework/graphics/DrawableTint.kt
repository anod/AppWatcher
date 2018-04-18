package info.anodsplace.framework.graphics

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat

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
