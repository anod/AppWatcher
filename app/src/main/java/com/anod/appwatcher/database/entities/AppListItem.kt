package com.anod.appwatcher.database.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.anod.appwatcher.database.AppListTable
import com.anod.appwatcher.database.ChangelogTable
import info.anodsplace.framework.text.Html

private val newLineRegex = Regex("\n+")

/**
 * @author Alex Gavrishev
 * @date 25/05/2018
 */
data class AppListItem(
    @Embedded
    val app: App,

    @ColumnInfo(name = ChangelogTable.Columns.DETAILS)
    val changeDetails: String?,

    @ColumnInfo(name = ChangelogTable.Columns.NO_NEW_DETAILS)
    val noNewDetails: Boolean,

    @ColumnInfo(name = AppListTable.Columns.RECENT_FLAG)
    val recentFlag: Boolean
)

fun AppListItem.cleanChangeHtml(): String {
    return if (changeDetails?.isNotBlank() == true) {
        Html.parse(changeDetails).toString()
            .replace(newLineRegex, "\n")
            .removePrefix(app.versionName + "\n")
            .removePrefix(app.versionName + ":\n")
            .trim()
    } else {
        ""
    }
}