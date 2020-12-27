package com.anod.appwatcher.tags

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.databinding.FragmentTagSelectBinding
import com.anod.appwatcher.details.DetailsDialog
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.view.Keyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class AppsTagSelectDialog : DialogFragment(R.layout.fragment_tag_select) {

    private var isAllSelected: Boolean = false
    private val viewModel: AppsTagViewModel by viewModels()
    private val adapter: TagAppsAdapter by lazy { TagAppsAdapter(requireContext(), viewModel.tagAppsImport) }

    private var _binding: FragmentTagSelectBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            setStyle(STYLE_NORMAL, Theme(context).themeDialogNoActionBar)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentTagSelectBinding.bind(view)

        val tag: Tag = requireArguments().getParcelable(extraTag)!!
        viewModel.tagAppsImport = TagAppsImport(tag, ApplicationContext(requireContext()))
        viewModel.tag.value = tag

        binding.appBar.setBackgroundDrawable(ColorDrawable(tag.color))

        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.button3.setOnClickListener {
            val importAdapter = binding.list.adapter as TagAppsAdapter
            isAllSelected = !isAllSelected
            importAdapter.selectAllApps(isAllSelected)
        }

        binding.button2.setOnClickListener {
            dismiss()
        }

        binding.button1.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                viewModel.import()
                dismiss()
            }
        }

        lifecycleScope.launch {
            launch {
                viewModel.tags.collectLatest {
                    viewModel.tagAppsImport.initSelected(it)
                    binding.list.adapter = adapter
                }
            }

            viewModel.apps.collectLatest {
                binding.progress.visibility = View.GONE
                adapter.setData(it)
            }
        }

        createOptionsMenu()
    }

    private fun createOptionsMenu(): Boolean {
        val toolbar = binding.appBar as Toolbar
        toolbar.inflateMenu(R.menu.searchbox)
        val menu = toolbar.menu

        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_white_24)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }
        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setIconifiedByDefault(false)
        searchItem.expandActionView()

        searchView.setQuery(viewModel.titleFilter.value, true)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Keyboard.hide(searchView, requireContext())
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                viewModel.titleFilter.value = query
                return true
            }
        })

        Keyboard.hide(searchView, requireContext())
        return true
    }

    companion object {
        const val extraTag = "extra_tag"

        fun show(tag: Tag, fm: FragmentManager) {
            AppsTagSelectDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(extraTag, tag)
                }
                show(fm, DetailsDialog::class.java.simpleName)
            }
        }
    }
}
