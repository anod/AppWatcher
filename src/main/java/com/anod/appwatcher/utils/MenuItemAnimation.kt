package com.anod.appwatcher.utils

import android.content.Context
import android.support.v4.view.MenuItemCompat
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.anod.appwatcher.R

/**
 * @author alex
 * *
 * @date 2014-11-15
 */

class MenuItemAnimation(private val mContext: Context, private val mAnimResource: Int) {

    var menuItem: MenuItem? = null
    var isInvisibleMode: Boolean = false


    /**
     * stop refresh button animation
     */
    fun stop() {
        if (menuItem == null) {
            return
        }
        val actionView = menuItem?.actionView
        if (actionView != null) {
            actionView.clearAnimation()
            menuItem?.actionView = null
        }
        if (isInvisibleMode) {
            menuItem!!.isVisible = false
        }
    }

    /**
     * Animate refresh button
     */
    fun start() {
        if (menuItem == null) {
            return
        }
        //already animating
        if (menuItem?.actionView != null) {
            return
        }
        if (isInvisibleMode) {
            menuItem!!.isVisible = true
        }

        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val iv = inflater.inflate(R.layout.widget_refresh_action, null) as ImageView

        val rotation = AnimationUtils.loadAnimation(mContext, mAnimResource)
        rotation.repeatCount = Animation.INFINITE
        iv.startAnimation(rotation)

        MenuItemCompat.setActionView(menuItem, iv)

    }
}
