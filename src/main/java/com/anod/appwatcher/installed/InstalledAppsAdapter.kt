package com.anod.appwatcher.installed

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.model.packageToApp
import com.anod.appwatcher.utils.PicassoAppIcon
import com.anod.appwatcher.watchlist.AppViewHolder
import com.anod.appwatcher.watchlist.AppViewHolderBase
import com.anod.appwatcher.watchlist.AppViewHolderDataProvider
import info.anodsplace.framework.widget.recyclerview.ArrayAdapter
import java.util.*

/**
 * @author alex
 * *
 * @date 2015-08-30
 */
open class InstalledAppsAdapter(
        protected val context: Context,
        private val packageManager: PackageManager,
        private val dataProvider: AppViewHolderDataProvider,
        protected val listener: AppViewHolder.OnClickListener?)
    : ArrayAdapter<String, AppViewHolderBase>(ArrayList()) {

    internal val mIconLoader: PicassoAppIcon = App.provide(context).iconLoader

    override fun getItemViewType(position: Int): Int {
        return 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolderBase {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_app, parent, false)
        v.isClickable = true
        v.isFocusable = true

        return InstalledAppViewHolder(v, dataProvider, mIconLoader, listener)

    }

    override fun onBindViewHolder(holder: AppViewHolderBase, position: Int) {
        val packageName = getItem(position)
        val app = packageManager.packageToApp(-1, packageName)
        /**

         * int rowId, String appId, String pname, int versionNumber, String versionName,
         * String title, String creator, Bitmap icon, int status, String uploadDate, String priceText, String priceCur, Integer priceMicros, String detailsUrl) {
         */
        holder.bindView(position, app)
    }

    override fun addAll(objects: List<String>) {
        super.addAll(objects)
        dataProvider.totalAppsCount = itemCount
    }
}
