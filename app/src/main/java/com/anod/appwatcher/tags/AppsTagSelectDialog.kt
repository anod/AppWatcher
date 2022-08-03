package com.anod.appwatcher.tags

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
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
import com.anod.appwatcher.utils.appScope
import com.anod.appwatcher.utils.prefs
import info.anodsplace.framework.view.Keyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class AppsTagSelectDialog : DialogFragment(R.layout.fragment_tag_select), KoinComponent {

    private var isAllSelected: Boolean = false
    private val viewModel: AppsTagViewModel by viewModels()
    private val adapter: TagAppsAdapter by lazy { TagAppsAdapter(requireContext(), viewModel.tagAppsImport, get()) }

    private var _binding: FragmentTagSelectBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setStyle(STYLE_NORMAL, Theme(context, prefs).themeDialogNoActionBar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentTagSelectBinding.bind(view)

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
            appScope.launch(Dispatchers.Main) {
                viewModel.import()
                dismiss()
            }
        }

        binding.back.setOnClickListener {
            dismiss()
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

        binding.searchField.editText?.setText(viewModel.viewState.titleFilter)
        binding.searchField.editText?.doOnTextChanged { inputText, _, _, _ ->
            viewModel.handleEvent(AppsTagScreenEvent.FilterByTitle(inputText.toString()))
        }
        Keyboard.hide(binding.searchField.editText!!, requireContext())

        applyTagColor(viewModel.viewState.tag)
    }

    private fun applyTagColor(tag: Tag) {
        binding.appBar.setBackgroundDrawable(ColorDrawable(tag.color))
        val isLightColor = tag.isLightColor
        val onTagColor = if (isLightColor) Color.BLACK else Color.WHITE
        val colorStates = ColorStateList.valueOf(onTagColor)
        binding.back.imageTintList = colorStates
        binding.searchField.setStartIconTintList(colorStates)
        binding.searchField.boxStrokeColor = onTagColor
        binding.searchField.hintTextColor = colorStates
        binding.searchField.editText?.setTextColor(colorStates)
        binding.searchField.editText?.setHintTextColor(colorStates)
        binding.button3.setTextColor(onTagColor)
        binding.button2.setTextColor(onTagColor)
        binding.button1.setTextColor(onTagColor)
        binding.button3.setBackgroundColor(tag.color)
        binding.button2.setBackgroundColor(tag.color)
        binding.button1.setBackgroundColor(tag.color)
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