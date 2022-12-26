package com.anod.appwatcher.watchlist

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.anod.appwatcher.R
import com.google.android.material.card.MaterialCardView

/**
 * @author Alex Gavrishev
 * @date 16/07/2017
 */
class RecentAppView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : MaterialCardView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null, 0)

    val icon: ImageView by lazy { findViewById(R.id.icon) }
    val title: TextView by lazy { findViewById(R.id.title) }
    val watched: ImageView by lazy { findViewById(R.id.watched) }
    val content: View by lazy { findViewById(R.id.content) }
}