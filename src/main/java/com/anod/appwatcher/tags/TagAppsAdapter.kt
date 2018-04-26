package com.anod.appwatcher.tags

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.PicassoAppIcon

internal class TagAppsAdapter(private val context: Context, private val tagAppsImport: TagAppsImport)
    : RecyclerView.Adapter<TagAppsAdapter.ItemViewHolder>() {

    private var apps: List<AppInfo> = emptyList()

    fun setData(apps: List<AppInfo>) {
        this.apps = apps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_item_import_app, parent, false)
        return ItemViewHolder(itemView, mIconLoader, tagAppsImport)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindView(apps[position])
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    private val mIconLoader: PicassoAppIcon = App.provide(context).iconLoader

    internal class ItemViewHolder(
            itemView: View,
            private val mIconLoader: PicassoAppIcon,
            private val tagAppsImport: TagAppsImport) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val title: CheckedTextView = itemView.findViewById(android.R.id.title)
        val icon: ImageView = itemView.findViewById(android.R.id.icon)

        private var app: AppInfo? = null

        fun bindView(app: AppInfo) {
            this.app = app
            this.title.text = app.title
            this.title.isChecked = tagAppsImport.isSelected(app.appId)
            this.itemView.findViewById<View>(android.R.id.content).setOnClickListener(this)
            mIconLoader.loadAppIntoImageView(app, this.icon, R.drawable.ic_notifications_black_24dp)
        }

        override fun onClick(v: View) {
            this.title.toggle()
            tagAppsImport.updateApp(this.app!!.appId, title.isChecked)
        }
    }

    fun selectAllApps(select: Boolean) {
        tagAppsImport.selectAll(select)
        notifyDataSetChanged()
    }
}