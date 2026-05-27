package com.anod.appwatcher.tags

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import info.anodsplace.framework.content.ShowSnackbarData
import info.anodsplace.ktx.hashCodeOf

/**
 * @author Alex Gavrishev
 * *
 * @date 02/05/2017.
 */

data class TagSnackbarAppInfo(val app: App) : ShowSnackbarData

object TagSnackbar {
    private const val GREEN_BOOK = "📗"

    class Visuals(override val message: String, override val actionLabel: String, override val duration: SnackbarDuration = SnackbarDuration.Long, override val withDismissAction: Boolean = true,) :
        SnackbarVisuals {
        override fun hashCode(): Int = hashCodeOf(message, actionLabel, duration, withDismissAction)
        override fun equals(other: Any?): Boolean = other is Visuals &&
            other.message == message &&
            other.actionLabel == actionLabel &&
            other.duration == duration &&
            other.withDismissAction == withDismissAction

        constructor(info: TagSnackbarAppInfo, context: Context) : this(
            message = context.getString(R.string.app_stored, info.app.title),
            actionLabel = context.getString(R.string.action_tag, GREEN_BOOK)
        )
    }
}