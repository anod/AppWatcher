package com.anod.appwatcher.installed

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.adapters.AppViewHolder
import com.anod.appwatcher.adapters.AppViewHolderBase
import com.anod.appwatcher.adapters.AppViewHolderDataProvider
import com.anod.appwatcher.utils.AppIconLoader
import com.anod.appwatcher.utils.PackageManagerUtils
import info.anodsplace.android.widget.recyclerview.ArrayAdapter
import java.util.*

/**
 * @author alex
 * *
 * @date 2015-08-30
 */
open class InstalledAppsAdapter(
        protected val mContext: Context,
        private val mPackageManager: PackageManager,
        private val mDataProvider: AppViewHolderDataProvider,
        protected val mListener: AppViewHolder.OnClickListener?)
    : ArrayAdapter<String, AppViewHolderBase>(ArrayList<String>()) {

    internal val mIconLoader: AppIconLoader = App.provide(mContext).iconLoader()

    override fun getItemViewType(position: Int): Int {
        return 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolderBase {
        val v = LayoutInflater.from(mContext).inflate(R.layout.list_item_app, parent, false)
        v.isClickable = true
        v.isFocusable = true

        return InstalledAppViewHolder(v, mDataProvider, mIconLoader, mListener)

    }

    override fun onBindViewHolder(holder: AppViewHolderBase, position: Int) {
        val packageName = getItem(position)
        val app = PackageManagerUtils.packageToApp(packageName, mPackageManager)
        /**

         * int rowId, String appId, String pname, int versionNumber, String versionName,
         * String title, String creator, Bitmap icon, int status, String uploadDate, String priceText, String priceCur, Integer priceMicros, String detailsUrl) {
         */
        holder.bindView(position, app)
    }

    override fun addAll(objects: List<String>) {
        super.addAll(objects)
        mDataProvider.totalAppsCount = itemCount
    }
}
