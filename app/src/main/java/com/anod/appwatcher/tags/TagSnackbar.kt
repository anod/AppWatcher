package com.anod.appwatcher.tags

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import com.anod.appwatcher.R
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.AppListFilter
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.utils.Theme
import com.google.android.material.snackbar.Snackbar
import info.anodsplace.ktx.hashCodeOf

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
            activity: Activity,
            prefs: Preferences
    ): Snackbar {
        val msg = activity.getString(R.string.app_stored, info.title)
        val tagText = activity.getString(R.string.action_tag, GREEN_BOOK)

        return Snackbar.make(parentView, msg, Snackbar.LENGTH_LONG)
                .setAction(tagText, TagAction(activity, info, prefs))
                .addCallback(TagCallback(activity, finishActivity))
    }

    class Visuals(
        override val message: String,
        override val actionLabel: String,
        override val duration: SnackbarDuration = SnackbarDuration.Long,
        override val withDismissAction: Boolean = true,
    ) : SnackbarVisuals {
        override fun hashCode(): Int = hashCodeOf(message, actionLabel, duration, withDismissAction)
        override fun equals(other: Any?): Boolean = (other as? AppListFilter.Installed)?.hashCode() == hashCode()

        constructor(info: AppInfo, context: Context) : this(
            message = context.getString(R.string.app_stored, info.title),
            actionLabel = context.getString(R.string.action_tag, GREEN_BOOK)
        )
    }

    internal class TagAction(private val activity: Activity, private val app: AppInfo, private val prefs: Preferences) : View.OnClickListener {
        override fun onClick(v: View) {
            this.activity.startActivity(TagsListFragment.intent(activity, prefs, app))
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