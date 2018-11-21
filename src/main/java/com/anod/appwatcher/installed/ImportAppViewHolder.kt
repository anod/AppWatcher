package com.anod.appwatcher.installed

import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import android.view.View
import android.widget.CheckedTextView
import android.widget.ImageView
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.watchlist.AppViewHolderBase

/**
 * @author alex
 * *
 * @date 2015-08-31
 */
internal class ImportAppViewHolder(
        itemView: View,
        dataProvider: ImportResourceProvider,
        iconLoader: PicassoAppIcon)
    : AppViewHolderBase(itemView, dataProvider, iconLoader), View.OnClickListener {

    private val importDataProvider = dataProvider
    private var item: AppListItem? = null
    private val title: CheckedTextView = itemView.findViewById(android.R.id.title)
    private val icon: ImageView = itemView.findViewById(android.R.id.icon)

    val themeAccent: Int = ResourcesCompat.getColor(itemView.resources, R.color.theme_accent, null)
    val materialRed: Int = ResourcesCompat.getColor(itemView.resources, R.color.material_red_800, null)

    override fun bindView(item: AppListItem) {
        this.item = item
        this.title.text = item.app.title
        val checked = importDataProvider.isPackageSelected(item.app.packageName)
        this.title.isChecked = checked
        title.isEnabled = !importDataProvider.isImportStarted

        itemView.findViewById<View>(android.R.id.content).setOnClickListener(this)

        iconLoader.loadAppIntoImageView(item.app, this.icon, R.drawable.ic_notifications_black_24dp)

        when {
            status() == ImportResourceProvider.STATUS_DONE -> itemView.setBackgroundColor(themeAccent)
            status() == ImportResourceProvider.STATUS_ERROR -> itemView.setBackgroundColor(materialRed)
            else -> itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    fun status(): Int {
        return importDataProvider.getPackageStatus(item!!.app.packageName)
    }

    override fun onClick(v: View) {
        this.title.toggle()
        importDataProvider.selectPackage(item!!.app.packageName, title.isChecked)
    }
}