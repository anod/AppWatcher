package com.anod.appwatcher.installed

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import com.anod.appwatcher.R

/**
 * @author algavris
 * @date 16/07/2017
 */
class RecentAppView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
    : android.support.v7.widget.CardView(context, attrs, defStyleAttr)
{
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?): this(context, null, 0)

    val icon: ImageView by lazy { findViewById<ImageView>(android.R.id.icon) }
    val title: TextView by lazy { findViewById<TextView>(android.R.id.title) }
    val watched: ImageView by lazy { findViewById<ImageView>(R.id.watched) }
}