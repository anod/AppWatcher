package info.anodsplace.framework.anim

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet

import java.util.ArrayList

/**
 * @author Alex Gavrishev
 * @date 2015-05-25
 */
class AnimatorCollection {
    private val animators = ArrayList<Animator>()
    private val set: AnimatorSet by lazy { AnimatorSet() }

    val isEmpty: Boolean
        get() = animators.isEmpty()

    fun add(anim: Animator?) {
        if (anim != null) {
            animators.add(anim)
        }
    }

    fun clear() {
        set.cancel()
        animators.clear()
    }

    fun sequential(): AnimatorSet {
        set.playSequentially(animators)
        return set
    }

    fun together(): AnimatorSet {
        set.playTogether(animators)
        return set
    }

    fun addListener(listener: AnimatorListenerAdapter) {
        set.addListener(listener)
    }
}
