package com.anod.appwatcher.tags

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckedTextView
import android.widget.ImageView
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.framework.RecyclerViewCursorAdapter
import com.anod.appwatcher.utils.PicassoAppIcon

internal class TagAppsCursorAdapter(context: Context, private val tagAppsImport: TagAppsImport)
    : RecyclerViewCursorAdapter<TagAppsCursorAdapter.ItemViewHolder, AppListCursor>(context, R.layout.list_item_import_app) {
    private val mIconLoader: PicassoAppIcon = App.provide(context).iconLoader

    internal class ItemViewHolder(
            itemView: View,
            private val mIconLoader: PicassoAppIcon,
            private val tagAppsImport: TagAppsImport) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val title: CheckedTextView = itemView.findViewById(android.R.id.title)
        val icon: ImageView = itemView.findViewById(android.R.id.icon)

        private var app: AppInfo? = null

        fun bindView(position: Int, app: AppInfo) {
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

    override fun onCreateViewHolder(itemView: View): ItemViewHolder {
        return ItemViewHolder(itemView, mIconLoader, tagAppsImport)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int, cursor: AppListCursor) {
        val app = cursor.appInfo
        holder.bindView(cursor.position, app)
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    fun selectAllApps(select: Boolean) {
        tagAppsImport.selectAll(select)
        this.notifyDataSetChanged()
    }
}