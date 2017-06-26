package com.anod.appwatcher.tags

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckedTextView
import android.widget.ImageView
import butterknife.ButterKnife
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import com.anod.appwatcher.content.AppListCursor
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.recyclerview.RecyclerViewCursorAdapter
import com.anod.appwatcher.utils.AppIconLoader

internal class TagAppsCursorAdapter(context: Context, private val mManager: TagAppsManager)
    : RecyclerViewCursorAdapter<TagAppsCursorAdapter.ItemViewHolder, AppListCursor>(context, R.layout.list_item_import_app) {
    private val mIconLoader: AppIconLoader = App.provide(context).iconLoader

    internal class ItemViewHolder(
            itemView: View,
            private val mIconLoader: AppIconLoader,
            private val mManager: TagAppsManager) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val title: CheckedTextView = ButterKnife.findById(itemView, android.R.id.title)
        val icon: ImageView = ButterKnife.findById(itemView, android.R.id.icon)

        private var app: AppInfo? = null

        fun bindView(position: Int, app: AppInfo) {
            this.app = app
            this.title.text = app.title
            this.title.isChecked = mManager.isSelected(app.appId)
            this.itemView.findViewById<View>(android.R.id.content).setOnClickListener(this)
            mIconLoader.loadAppIntoImageView(app, this.icon, R.drawable.ic_notifications_black_24dp)
        }

        override fun onClick(v: View) {
            this.title.toggle()
            mManager.updateApp(this.app!!.appId, title.isChecked)
        }
    }

    override fun onCreateViewHolder(itemView: View): ItemViewHolder {
        return ItemViewHolder(itemView, mIconLoader, mManager)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int, cursor: AppListCursor) {
        val app = cursor.appInfo
        holder.bindView(cursor.position, app)
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    fun selectAllApps(select: Boolean) {
        mManager.selectAll(select)
        this.notifyDataSetChanged()
    }
}