package com.anod.appwatcher.tags

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.model.AppListFilter
import info.anodsplace.ktx.hashCodeOf

/**
 * @author Alex Gavrishev
 * *
 * @date 02/05/2017.
 */

object TagSnackbar {
    private const val GREEN_BOOK = "📗"

    class Visuals(
        override val message: String,
        override val actionLabel: String,
        override val duration: SnackbarDuration = SnackbarDuration.Long,
        override val withDismissAction: Boolean = true,
    ) : SnackbarVisuals {
        override fun hashCode(): Int = hashCodeOf(message, actionLabel, duration, withDismissAction)
        override fun equals(other: Any?): Boolean = (other as? AppListFilter.Installed)?.hashCode() == hashCode()

        constructor(info: App, context: Context) : this(
            message = context.getString(R.string.app_stored, info.title),
            actionLabel = context.getString(R.string.action_tag, GREEN_BOOK)
        )
    }
}