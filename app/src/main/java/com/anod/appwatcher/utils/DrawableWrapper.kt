// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.utils

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat

/**
 * Drawable which delegates all calls to its wrapped [Drawable].
 *
 *
 * The wrapped [Drawable] *must* be fully released from any [View]
 * before wrapping, otherwise internal [Callback] may be dropped.
 *
 */
open class DrawableWrapper(private val drawable: Drawable) : Drawable(), Drawable.Callback {

    init {
        drawable.callback = this
    }

    override fun draw(canvas: Canvas) {
        drawable.draw(canvas)
    }

    override fun onBoundsChange(bounds: Rect) {
        drawable.bounds = bounds
    }

    override fun setChangingConfigurations(configs: Int) {
        drawable.changingConfigurations = configs
    }

    override fun getChangingConfigurations(): Int {
        return drawable.changingConfigurations
    }

    override fun setDither(dither: Boolean) {
        drawable.setDither(dither)
    }

    override fun setFilterBitmap(filter: Boolean) {
        drawable.isFilterBitmap = filter
    }

    override fun setAlpha(alpha: Int) {
        drawable.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        drawable.colorFilter = cf
    }

    override fun isStateful(): Boolean {
        return drawable.isStateful
    }

    override fun setState(stateSet: IntArray): Boolean {
        return drawable.setState(stateSet)
    }

    override fun getState(): IntArray {
        return drawable.state
    }

    override fun jumpToCurrentState() {
        drawable.jumpToCurrentState()
    }

    override fun getCurrent(): Drawable {
        return drawable.current
    }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        return super.setVisible(visible, restart) || drawable.setVisible(visible, restart)
    }

    override fun getOpacity(): Int {
        return drawable.opacity
    }

    override fun getTransparentRegion(): Region? {
        return drawable.transparentRegion
    }

    override fun getIntrinsicWidth(): Int {
        return drawable.intrinsicWidth
    }

    override fun getIntrinsicHeight(): Int {
        return drawable.intrinsicHeight
    }

    override fun getMinimumWidth(): Int {
        return drawable.minimumWidth
    }

    override fun getMinimumHeight(): Int {
        return drawable.minimumHeight
    }

    override fun getPadding(padding: Rect): Boolean {
        return drawable.getPadding(padding)
    }

    /**
     * {@inheritDoc}
     */
    override fun invalidateDrawable(who: Drawable) {
        invalidateSelf()
    }

    /**
     * {@inheritDoc}
     */
    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        scheduleSelf(what, `when`)
    }

    /**
     * {@inheritDoc}
     */
    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        unscheduleSelf(what)
    }

    override fun onLevelChange(level: Int): Boolean {
        return drawable.setLevel(level)
    }

    override fun setAutoMirrored(mirrored: Boolean) {
        DrawableCompat.setAutoMirrored(drawable, mirrored)
    }

    override fun isAutoMirrored(): Boolean {
        return DrawableCompat.isAutoMirrored(drawable)
    }

    override fun setTint(tint: Int) {
        DrawableCompat.setTint(drawable, tint)
    }

    override fun setTintList(tint: ColorStateList?) {
        DrawableCompat.setTintList(drawable, tint)
    }

    override fun setTintMode(tintMode: PorterDuff.Mode?) {
        DrawableCompat.setTintMode(drawable, tintMode!!)
    }

    override fun setHotspot(x: Float, y: Float) {
        DrawableCompat.setHotspot(drawable, x, y)
    }

    override fun setHotspotBounds(left: Int, top: Int, right: Int, bottom: Int) {
        DrawableCompat.setHotspotBounds(drawable, left, top, right, bottom)
    }

}