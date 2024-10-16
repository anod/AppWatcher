package com.anod.appwatcher.database.entities
import info.anodsplace.framework.text.Html

private val newLineRegex = Regex("\n+")

fun AppListItem.cleanChangeHtml(): String {
    return if (changeDetails?.isNotBlank() == true) {
        Html.parse(changeDetails).toString()
            .replace(newLineRegex, "\n")
            .removePrefix(app.versionName + "\n")
            .removePrefix(app.versionName + ":\n")
            .trim()
    } else ""
}