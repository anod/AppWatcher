package com.anod.appwatcher.tags

import android.app.Activity
import android.view.View
import com.anod.appwatcher.R
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.Theme
import com.google.android.material.snackbar.Snackbar

/**
 * @author Alex Gavrishev
 * *
 * @date 02/05/2017.
 */

object TagSnackbar {
    private const val GREEN_BOOK = "📗"

    fun make(
        parentView: View,
        info: AppInfo,
        finishActivity: Boolean,
        activity: Activity
    ): Snackbar {
        val msg = activity.getString(R.string.app_stored, info.title)
        val tagText = activity.getString(R.string.action_tag, GREEN_BOOK)

        return Snackbar.make(parentView, msg, Snackbar.LENGTH_LONG)
            .setAction(tagText, TagAction(activity, info))
            .addCallback(TagCallback(activity, finishActivity))
    }

    internal class TagAction(private val activity: Activity, private val app: AppInfo) : View.OnClickListener {
        override fun onClick(v: View) {
            val theme = Theme(activity)
            this.activity.startActivity(TagsListFragment.intent(activity, theme.theme, theme.colors, app))
        }
    }

    private class TagCallback(private val activity: Activity, private val finishActivity: Boolean) : Snackbar.Callback() {

        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            if (this.finishActivity) {
                this.activity.finish()
            }
        }
    }
}
