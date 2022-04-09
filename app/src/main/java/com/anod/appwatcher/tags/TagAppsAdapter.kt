package com.anod.appwatcher.tags

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.utils.AppIconLoader

internal class TagAppsAdapter(private val context: Context, private val tagAppsImport: TagAppsImport)
    : RecyclerView.Adapter<TagAppsAdapter.ItemViewHolder>() {

    private var apps: List<AppListItem> = emptyList()

    fun setData(apps: List<AppListItem>) {
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

    private val mIconLoader: AppIconLoader = Application.provide(context).iconLoader

    internal class ItemViewHolder(
            itemView: View,
            private val mIconLoader: AppIconLoader,
            private val tagAppsImport: TagAppsImport) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val title: CheckedTextView = itemView.findViewById(R.id.title)
        val icon: ImageView = itemView.findViewById(R.id.icon)

        private var app: App? = null

        fun bindView(item: AppListItem) {
            this.app = item.app
            this.title.text = item.app.title
            this.title.isChecked = tagAppsImport.isSelected(item.app.appId)
            this.itemView.findViewById<View>(R.id.content).setOnClickListener(this)
            mIconLoader.loadAppIntoImageView(item.app, this.icon, R.drawable.ic_app_icon_placeholder)
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