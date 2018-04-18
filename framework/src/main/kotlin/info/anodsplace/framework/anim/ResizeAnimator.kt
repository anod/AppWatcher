package info.anodsplace.framework.anim

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

/**
 * @author Alex Gavrishev
 * @date 2015-05-25
 */
object ResizeAnimator {

    fun height(from: Int, to: Int, view: View, duration: Int): ValueAnimator {
        val anim = ValueAnimator.ofInt(from, to)
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.height = `val`
            view.layoutParams = layoutParams
        }
        anim.duration = duration.toLong()
        return anim
    }

    fun margin(from: Int, to: Int, view: View, duration: Int): ValueAnimator {
        val anim = ValueAnimator.ofInt(from, to)
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = view.layoutParams as LinearLayout.LayoutParams
            layoutParams.topMargin = `val`
            view.layoutParams = layoutParams
        }
        anim.duration = duration.toLong()
        return anim
    }
}
