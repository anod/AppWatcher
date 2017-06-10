package com.anod.appwatcher.installed

import android.graphics.Color
import android.view.View
import android.widget.CheckedTextView
import android.widget.ImageView
import butterknife.BindColor
import butterknife.BindView
import butterknife.ButterKnife
import com.anod.appwatcher.R
import com.anod.appwatcher.adapters.AppViewHolderBase
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.AppIconLoader

/**
 * @author alex
 * *
 * @date 2015-08-31
 */
internal class ImportAppViewHolder(
        itemView: View,
        dataProvider: ImportDataProvider,
        iconLoader: AppIconLoader)
    : AppViewHolderBase(itemView, dataProvider, iconLoader), View.OnClickListener {

    val importDataProvider = dataProvider
    var app: AppInfo? = null
    @BindView(android.R.id.title)
    lateinit var title: CheckedTextView
    @BindView(android.R.id.icon)
    lateinit var icon: ImageView

    @BindColor(R.color.theme_accent)
    @JvmField var themeAccent: Int = 0
    @BindColor(R.color.material_red_800)
    @JvmField var materialRed: Int = 0

    init {
        ButterKnife.bind(this, itemView)
    }

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