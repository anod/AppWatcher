package com.anod.appwatcher.tags

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.content.DbContentProviderClient
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.FragmentToolbarActivity
import info.anodsplace.framework.graphics.DrawableTint
import kotlinx.android.synthetic.main.activity_tags_editor.*

/**
 * @author Alex Gavrishev
 * *
 * @date 10/03/2017.
 */

class TagsListFragment : Fragment(), View.OnClickListener {

    private var appInfo: AppInfo? = null

    private val viewModel: TagsListViewModel by lazy { ViewModelProviders.of(this).get(TagsListViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_tags_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        if (arguments!!.containsKey(EXTRA_APP)) {
            appInfo = arguments!!.getParcelable(EXTRA_APP)
            activity!!.title = getString(R.string.tag_app, appInfo!!.title)
        }
        list.layoutManager = LinearLayoutManager(context!!)
        list.adapter = TagAdapter(context!!, this)
        list.addItemDecoration(DividerItemDecoration(context!!, DividerItemDecoration.HORIZONTAL))

        viewModel.tags.observe(this, Observer {
            (list.adapter as TagAdapter).update(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tags_editor, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_tag) {
            val dialog = EditTagDialog.newInstance(null, Theme(requireActivity()))
            dialog.show(fragmentManager!!, "edit-tag-dialog")
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        val holder = v.tag as TagHolder
        if (appInfo == null) {
            val dialog = EditTagDialog.newInstance(holder.tag, Theme(requireActivity()))
            dialog.show(fragmentManager!!, "edit-tag-dialog")
        } else {
            val client = DbContentProviderClient(context!!)
            val tags = client.queryAppTags(appInfo!!.rowId)
            if (tags.contains(holder.tag.id)) {
                if (client.removeAppFromTag(appInfo!!.appId, holder.tag.id)) {
                    holder.name.isSelected = false
                    holder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            } else {
                if (client.addAppToTag(appInfo!!.appId, holder.tag.id)) {
                    holder.name.isSelected = true
                    val d = DrawableTint(resources, R.drawable.ic_check_black_24dp, activity!!.theme).apply(R.color.control_tint)
                    holder.name.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null)
                }
            }
            client.close()
        }
    }

    private class TagAdapter(
            private val context: Context,
            private val listener: View.OnClickListener) : RecyclerView.Adapter<TagHolder>() {

        private var tags: List<Tag> = emptyList()

        fun update(tags: List<Tag>) {
            this.tags = tags
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.list_item_tag, parent, false)
            return TagHolder(itemView, listener)
        }

        override fun getItemCount(): Int = tags.size

        override fun onBindViewHolder(holder: TagHolder, position: Int) {
            holder.bindView(tags[position])
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

        fun bindView(tag: Tag) {
            this.tag = tag
            name.text = tag.name
            val d = color.drawable.mutate()
            DrawableCompat.setTint(DrawableCompat.wrap(d), tag.color)
            color.setImageDrawable(d)
        }
    }

    companion object {
        private const val TAG = "tags_list"
        const val EXTRA_APP = "app"

        fun intent(context: Context, themeRes: Int, themeColors: CustomThemeColors, app: AppInfo?) = FragmentToolbarActivity.intent(
                TAG,
                { TagsListFragment() },
                themeRes,
                themeColors,
                Bundle().apply { if (app != null) { putParcelable(EXTRA_APP, app) } },
                context)
    }
}
