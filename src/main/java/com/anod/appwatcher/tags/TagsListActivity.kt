package com.anod.appwatcher.tags

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.anod.appwatcher.R
import com.anod.appwatcher.content.DbContentProvider
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.content.TagsContentProviderClient
import com.anod.appwatcher.content.TagsCursor
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.model.Tag
import com.anod.appwatcher.model.schema.TagsTable
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.app.ToolbarActivity
import info.anodsplace.framework.graphics.DrawableTint
import info.anodsplace.framework.widget.recyclerview.RecyclerViewCursorAdapter
import kotlinx.android.synthetic.main.activity_tags_editor.*

/**
 * @author algavris
 * *
 * @date 10/03/2017.
 */

class TagsListActivity : ToolbarActivity(), LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    override val themeRes: Int
        get() =  Theme(this).theme

    private var appInfo: AppInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tags_editor)
        setupToolbar()

        if (intentExtras.containsKey(EXTRA_APP)) {
            appInfo = intentExtras.getParcelable(EXTRA_APP)
            title = getString(R.string.tag_app, appInfo!!.title)
        }
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = TagAdapter(this, this)
        list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL))

        supportLoaderManager.initLoader(0, null, this).forceLoad()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.tags_editor, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_tag) {
            val dialog = EditTagDialog.newInstance(null)
            dialog.show(supportFragmentManager, "edit-tag-dialog")
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return TagsCursorLoader(this)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        (list.adapter as TagAdapter).swapData(TagsCursor(data))
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        (list.adapter as TagAdapter).swapData(null)
    }

    fun saveTag(tag: Tag) {
        val cr = DbContentProviderClient(this)
        if (tag.id == -1) {
            cr.createTag(tag)
        } else {
            cr.saveTag(tag)
        }
        cr.close()
    }

    fun deleteTag(tag: Tag) {
        val cr = DbContentProviderClient(this)
        cr.deleteTag(tag)
        cr.close()
    }

    override fun onClick(v: View) {
        val holder = v.tag as TagHolder
        if (appInfo == null) {
            val dialog = EditTagDialog.newInstance(holder.tag)
            dialog.show(supportFragmentManager, "edit-tag-dialog")
        } else {
            val client = DbContentProviderClient(this)
            val tags = client.queryAppTags(appInfo!!.rowId)
            if (tags.contains(holder.tag.id)) {
                if (client.removeAppFromTag(appInfo!!.appId, holder.tag.id)) {
                    holder.name.isSelected = false
                    holder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            } else {
                if (client.addAppToTag(appInfo!!.appId, holder.tag.id)) {
                    holder.name.isSelected = true
                    val d = DrawableTint(resources, R.drawable.ic_check_black_24dp, theme).apply(R.color.control_tint)
                    holder.name.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null)
                }
            }
            client.close()
        }
    }

    private class TagsCursorLoader internal constructor(context: Context) : CursorLoader(context, TagsListActivity.TagsCursorLoader.CONTENT_URI, TagsTable.projection, null, null, TagsListActivity.TagsCursorLoader.ORDER_DEFAULT) {
        companion object {
            private val CONTENT_URI = DbContentProvider.tagsUri
            private val ORDER_DEFAULT = TagsContentProviderClient.DEFAULT_SORT_ORDER
        }
    }

    private class TagAdapter internal constructor(context: Context, private val mListener: View.OnClickListener) : RecyclerViewCursorAdapter<TagHolder, TagsCursor>(context, R.layout.list_item_tag) {

        override fun onCreateViewHolder(itemView: View): TagHolder {
            return TagHolder(itemView, mListener)
        }

        override fun onBindViewHolder(holder: TagHolder, position: Int, cursor: TagsCursor) {
            holder.bindView(position, cursor.tag)
        }
    }

    internal class TagHolder(itemView: View, listener: View.OnClickListener) : RecyclerView.ViewHolder(itemView) {
        lateinit var tag: Tag

        val name: TextView = itemView.findViewById(android.R.id.text1)
        val color: ImageView = itemView.findViewById(android.R.id.icon)

        init {
            itemView.tag = this
            itemView.setOnClickListener(listener)
        }

        fun bindView(position: Int, tag: Tag) {
            this.tag = tag
            name.text = tag.name
            val d = color.drawable.mutate()
            DrawableCompat.setTint(DrawableCompat.wrap(d), tag.color)
            color.setImageDrawable(d)
        }
    }

    companion object {
        const val EXTRA_APP = "app"

        fun intent(context: Context, app: AppInfo): Intent {
            val intent = Intent(context, TagsListActivity::class.java)
            intent.putExtra(EXTRA_APP, app)
            return intent
        }
    }
}
