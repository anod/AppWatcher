package com.anod.appwatcher.details

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.Amber800
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.model.AppInfoMetadata
import info.anodsplace.framework.content.InstalledApps

private fun formatVersionText(versionName: String, versionNumber: Int, newVersionNumber: Int, context: Context): String {
    if (newVersionNumber > 0) {
        return context.getString(R.string.version_updated_text, versionName, versionNumber, newVersionNumber)
    }
    return context.getString(R.string.version_text, versionName, versionNumber)
}

data class AppItemState(
    val color: Color,
    val text: String,
    val installed: Boolean,
    val showRecent: Boolean
)

@Composable
fun rememberAppItemState(
    app: App,
    recentFlag: Boolean,
    installedApps: InstalledApps,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    primaryColor: Color = MaterialTheme.colorScheme.primary
): AppItemState {
    val context = LocalContext.current
    return remember(app) {
        val packageInfo = installedApps.packageInfo(app.packageName)
        calcAppItemState(
            app, recentFlag, textColor, primaryColor, packageInfo, context
        )
    }
}

private fun calcAppItemState(
    app: App,
    recentFlag: Boolean,
    textColor: Color,
    primaryColor: Color,
    packageInfo: InstalledApps.Info,
    context: Context
): AppItemState {
    var color = textColor
    var installed = false
    val text = when {
        app.versionNumber == 0 -> {
            color = Amber800
            context.getString(R.string.updates_not_available)
        }

        packageInfo.isInstalled -> {
            installed = true
            when {
                app.versionNumber > packageInfo.versionCode -> {
                    color = Amber800
                    formatVersionText(
                        packageInfo.versionName, packageInfo.versionCode, app.versionNumber, context
                    )
                }

                app.status == AppInfoMetadata.STATUS_UPDATED || recentFlag -> {
                    color = primaryColor
                    formatVersionText(packageInfo.versionName, packageInfo.versionCode, 0, context)
                }

                else -> {
                    formatVersionText(packageInfo.versionName, packageInfo.versionCode, 0, context)
                }
            }
        } else -> {
            if (app.status == AppInfoMetadata.STATUS_UPDATED || recentFlag) {
                color = primaryColor
            }
            if (app.price.isFree) {
                context.getString(R.string.free)
            } else {
                app.price.text
            }
        }
    }

    val showRecent = when {
        app.status == AppInfoMetadata.STATUS_UPDATED || recentFlag -> true
        else -> false
    }

    return AppItemState(color, text, installed, showRecent)
}
