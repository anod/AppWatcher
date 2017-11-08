package com.anod.appwatcher.installed

import android.graphics.Color
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import android.widget.CheckedTextView
import android.widget.ImageView
import com.anod.appwatcher.R
import com.anod.appwatcher.watchlist.AppViewHolderBase
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.PicassoAppIcon

/**
 * @author alex
 * *
 * @date 2015-08-31
 */
internal class ImportAppViewHolder(
        itemView: View,
        dataProvider: ImportDataProvider,
        iconLoader: PicassoAppIcon)
    : AppViewHolderBase(itemView, dataProvider, iconLoader), View.OnClickListener {

    val importDataProvider = dataProvider
    var app: AppInfo? = null
    val title: CheckedTextView = itemView.findViewById(android.R.id.title)
    val icon: ImageView = itemView.findViewById(android.R.id.icon)

    val themeAccent: Int = ResourcesCompat.getColor(itemView.resources, R.color.theme_accent, null)
    val materialRed: Int = ResourcesCompat.getColor(itemView.resources, R.color.material_red_800, null)

    override fun bindView(location: Int, app: AppInfo) {
        this.app = app
        this.title.text = app.title
        val checked = importDataProvider.isPackageSelected(app.packageName)
        this.title.isChecked = checked
        title.isEnabled = !importDataProvider.isImportStarted

        itemView.findViewById<View>(android.R.id.content).setOnClickListener(this)

        iconLoader.loadAppIntoImageView(app, this.icon, R.drawable.ic_notifications_black_24dp)

        if (status() == ImportDataProvider.STATUS_DONE) {
            itemView.setBackgroundColor(themeAccent)
        } else if (status() == ImportDataProvider.STATUS_ERROR) {
            itemView.setBackgroundColor(materialRed)
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    fun status(): Int {
        return importDataProvider.getPackageStatus(app!!.packageName)
    }

    override fun onClick(v: View) {
        this.title.toggle()
        importDataProvider.selectPackage(this.app!!.packageName, title.isChecked)
    }
}