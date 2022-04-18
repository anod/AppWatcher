package com.anod.appwatcher.tags

import android.app.Activity
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
        if (context is Activity) {
            setStyle(STYLE_NORMAL, Theme(context, prefs).themeDialogNoActionBar)
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
        viewModel.tagAppsImport = TagAppsImport(tag, get(), get())
        viewModel.tag.value = tag

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

        binding.searchField.editText?.setText(viewModel.titleFilter.value)
        binding.searchField.editText?.doOnTextChanged { inputText, _, _, _ ->
            viewModel.titleFilter.value = inputText.toString()
        }
        Keyboard.hide(binding.searchField.editText!!, requireContext())

        applyTagColor(tag)
    }

    private fun applyTagColor(tag: Tag) {
        binding.appBar.setBackgroundDrawable(ColorDrawable(tag.color))
        val isLightColor = tag.isLightColor
        val iconsColor = if (isLightColor) Color.BLACK else Color.WHITE
        val colorStates = ColorStateList.valueOf(iconsColor)
        binding.back.imageTintList = colorStates
        binding.searchField.setStartIconTintList(colorStates)
        binding.searchField.boxStrokeColor = iconsColor
        binding.searchField.hintTextColor = colorStates
        binding.searchField.editText?.setTextColor(colorStates)
        binding.searchField.editText?.setHintTextColor(colorStates)
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