package com.anod.appwatcher.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.databinding.DialogEditTagBinding
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.utils.prefs
import com.google.android.material.color.DynamicColors
import info.anodsplace.colorpicker.ColorPickerDialog
import info.anodsplace.colorpicker.ColorPickerSwatch
import info.anodsplace.colorpicker.ColorStateDrawable
import info.anodsplace.framework.app.DialogMessage
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

/**
 * @author Alex Gavrishev
 * *
 * @date 14/04/2017.
 */
class EditTagDialogLegacy : DialogFragment(), ColorPickerSwatch.OnColorSelectedListener, KoinComponent {

    private val viewModel: EditTagViewModel by viewModels(factoryProducer = { EditTagViewModel.Factory(tag = requireArguments().getParcelable("tag") ?: Tag("")) })

    private var _binding: DialogEditTagBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogEditTagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tag = viewModel.viewState.tag
        binding.tagName.setText(tag.name)
        val colorDrawable = arrayOf(ResourcesCompat.getDrawable(resources, info.anodsplace.colorpicker.R.drawable.color_picker_swatch, null)!!)
        binding.colorPreview.setImageDrawable(ColorStateDrawable(colorDrawable, tag.color))
        binding.tagName.requestFocus()
        dialog?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        if (tag.id > 0) {
            binding.button3.visibility = View.VISIBLE
        } else {
            binding.button3.visibility = View.GONE
        }

        binding.colorPreview.setOnClickListener {
            val dialog = ColorPickerDialog.newInstance(tag.color, false, activity, Theme(requireActivity(), prefs).themeDialog)
            dialog.setStyle(STYLE_NORMAL, Theme(requireActivity(), prefs).themeDialogNoActionBar)
            dialog.setOnColorSelectedListener(this)
            dialog.show(parentFragmentManager, "color-picker")
        }

        binding.button1.setOnClickListener {
            viewModel.handleEvent(EditTagEvent.SaveAndDismiss(binding.tagName.text.toString().trim { it <= ' ' }))
        }

        binding.button2.setOnClickListener {
            viewModel.handleEvent(EditTagEvent.Dismiss)
        }

        binding.button3.setOnClickListener {
            DialogMessage(
                    requireContext(),
                    R.style.AppTheme_Dialog,
                    R.string.delete_tag,
                    getString(R.string.delete_tag_message, tag.name)
            ) {
                it.setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.handleEvent(EditTagEvent.Delete)
                    dismiss()
                }
                it.setNegativeButton(android.R.string.cancel) { _, _ -> }
            }.show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.viewActions.collect { action ->
                when (action) {
                    EditTagAction.Dismiss -> dismiss()
                }
            }
        }
    }

    override fun onColorSelected(color: Int) {
        viewModel.handleEvent(EditTagEvent.UpdateColor(color))
        val colorDrawable = arrayOf(ResourcesCompat.getDrawable(resources, info.anodsplace.colorpicker.R.drawable.color_picker_swatch, null)!!)
        binding.colorPreview.setImageDrawable(ColorStateDrawable(colorDrawable, color))
    }

    companion object {
        private fun newInstance(tag: Tag?, theme: Theme) = EditTagDialogLegacy().apply {
            arguments = Bundle().apply {
                if (tag != null) {
                    putParcelable("tag", tag)
                }
            }
            if (!DynamicColors.isDynamicColorAvailable()) {
                setStyle(STYLE_NO_TITLE, theme.themeDialog)
            }
        }

        fun show(fragmentManager: FragmentManager, tag: Tag?, theme: Theme) {
            val dialog = newInstance(tag, theme)
            dialog.show(fragmentManager, "edit-tag-dialog")
        }
    }
}