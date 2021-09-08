package com.anod.appwatcher.tags

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.databinding.ActivityTagsEditorBinding
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.FragmentFactory
import info.anodsplace.framework.app.FragmentToolbarActivity
import info.anodsplace.graphics.DrawableTint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author Alex Gavrishev
 * *
 * @date 10/03/2017.
 */

class TagsListFragment : Fragment(), View.OnClickListener {

    private var _binding: ActivityTagsEditorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TagsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityTagsEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        viewModel.appInfo.value = requireArguments().getParcelable(EXTRA_APP)
        requireActivity().title = getString(R.string.tag_app, viewModel.appInfo.value!!.title)

        binding.emptyView.isVisible = false
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = TagAdapter(requireContext(), this)
        binding.list.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.HORIZONTAL))

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tagsAppItems.collectLatest {
                (binding.list.adapter as TagAdapter).update(it)
                if (it.isEmpty()) {
                    binding.emptyView.isVisible = true
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tags_editor, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_tag) {
            EditTagDialog.show(parentFragmentManager, null, Theme(requireActivity()))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        val holder = v.tag as TagHolder
        if (viewModel.appInfo.value == null) {
            EditTagDialog.show(parentFragmentManager, holder.tag, Theme(requireActivity()))
        } else {
            if (holder.name.isSelected) {
                viewModel.removeAppTag(holder.tag)
            } else {
                viewModel.addAppTag(holder.tag)
            }
        }
    }

    private class TagAdapter(
            private val context: Context,
            private val listener: View.OnClickListener) : RecyclerView.Adapter<TagHolder>() {

        private var items: List<TagAppItem> = emptyList()

        fun update(tags: List<TagAppItem>) {
            items = tags
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.list_item_tag, parent, false)
            return TagHolder(itemView, listener)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: TagHolder, position: Int) {
            holder.bindView(items[position].first, items[position].second)
        }
    }

    internal class TagHolder(itemView: View, listener: View.OnClickListener) : RecyclerView.ViewHolder(itemView) {
        lateinit var tag: Tag

        val name: TextView = itemView.findViewById(R.id.tagName)
        val color: ImageView = itemView.findViewById(R.id.icon)

        init {
            itemView.tag = this
            itemView.setOnClickListener(listener)
        }

        fun bindView(tag: Tag, checked: Boolean) {
            this.tag = tag
            name.text = tag.name
            val d = color.drawable.mutate()
            DrawableCompat.setTint(DrawableCompat.wrap(d), tag.color)
            color.setImageDrawable(d)

            if (checked) {
                name.isSelected = true
                val checkmark = DrawableTint(itemView.resources, R.drawable.ic_check_black_24dp, itemView.context.theme)
                    .apply(R.color.black)
                name.setCompoundDrawablesWithIntrinsicBounds(null, null, checkmark, null)
            } else {
                name.isSelected = false
                name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }
    }

    companion object {
        const val EXTRA_APP = "app"

        class Factory : FragmentFactory("tags_list") {
            override fun create() = TagsListFragment()
        }

        fun intent(context: Context, themeRes: Int, themeColors: CustomThemeColors, app: AppInfo) = FragmentToolbarActivity.intent(
                context,
                Factory(),
                Bundle().apply {
                    putParcelable(EXTRA_APP, app)
                },
                themeRes,
                themeColors)
    }
}
