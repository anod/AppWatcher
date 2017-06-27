package com.anod.appwatcher.backup.gdrive

/**
 * @author algavris
 * @date 26/06/2017
 */
object AppListFile: DriveIdFile.FileDescription {
    override val fileName = "applist.json"
    override val mimeType = "application/json"
}