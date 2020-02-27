package com.anod.appwatcher.installed

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Alex Gavrishev
 * *
 * @date 24/04/2016.
 */
internal class ImportItemAnimator : DefaultItemAnimator() {

    override fun animateChange(oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        oldHolder.itemView.clearAnimation()
        newHolder.itemView.clearAnimation()

        if (newHolder is ImportAppViewHolder) {
            when (newHolder.status()) {
                ImportResourceProvider.STATUS_IMPORTING -> {
                    val anim = AlphaAnimation(0.2f, 1.0f)
                    anim.duration = 500
                    anim.repeatMode = Animation.REVERSE
                    anim.repeatCount = Animation.INFINITE
                    newHolder.itemView.startAnimation(anim)
                }
                ImportResourceProvider.STATUS_DONE -> animateColor(newHolder.itemView, Color.TRANSPARENT, newHolder.themeAccent)
                ImportResourceProvider.STATUS_ERROR -> animateColor(newHolder.itemView, Color.TRANSPARENT, newHolder.rowError)
            }
        }

        return false
    }

    private fun animateColor(view: View, @ColorInt startColor: Int, @ColorInt endColor: Int) {
        val animator = ObjectAnimator.ofObject(
                view, "backgroundColor", ArgbEvaluator(), startColor, endColor
        )
        animator.duration = 300
        animator.start()
    }

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder, payloads: List<Any>): Boolean {
        return true
    }
}