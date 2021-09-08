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
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.databinding.DialogEditTagBinding
import com.anod.appwatcher.utils.Theme
import info.anodsplace.colorpicker.ColorPickerDialog
import info.anodsplace.colorpicker.ColorPickerSwatch
import info.anodsplace.colorpicker.ColorStateDrawable
import info.anodsplace.framework.app.DialogMessage

/**
 * @author Alex Gavrishev
 * *
 * @date 14/04/2017.
 */
class EditTagDialog : DialogFragment(), ColorPickerSwatch.OnColorSelectedListener {

    private lateinit var tag: Tag

    private val viewModel: TagsViewModel by viewModels()

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

        tag = requireArguments().getParcelable("tag") ?: Tag("")

        binding.tagName.setText(tag.name)
        val colorDrawable = arrayOf(ResourcesCompat.getDrawable(resources, R.drawable.color_picker_swatch, null)!!)
        binding.colorPreview.setImageDrawable(ColorStateDrawable(colorDrawable, tag.color))
        binding.tagName.requestFocus()
        dialog?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        if (tag.id > 0) {
            binding.button3.visibility = View.VISIBLE
        } else {
            binding.button3.visibility = View.GONE
        }

        binding.colorPreview.setOnClickListener {
            val dialog = ColorPickerDialog.newInstance(tag.color, false, activity, Theme(requireActivity()).themeDialog)
            dialog.setStyle(STYLE_NORMAL, Theme(requireActivity()).themeDialogNoActionBar)
            dialog.setOnColorSelectedListener(this)
            dialog.show(parentFragmentManager, "color-picker")
        }

        binding.button1.setOnClickListener {
            tag = Tag(tag.id, binding.tagName.text.toString().trim { it <= ' ' }, tag.color)
            viewModel.saveTag(tag)
            dismiss()
        }

        binding.button2.setOnClickListener {
            dismiss()
        }

        binding.button3.setOnClickListener {
            DialogMessage(
                    requireContext(),
                    R.style.AppTheme_Dialog,
                    R.string.delete_tag,
                    getString(R.string.delete_tag_message, tag.name)
            ) {
                it.setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.deleteTag(tag)
                    dismiss()
                }
                it.setNegativeButton(android.R.string.cancel) { _, _ -> }
            }.show()
        }
    }

    override fun onColorSelected(color: Int) {
        tag = Tag(tag.id, tag.name, color)

        val colorDrawable = arrayOf(ResourcesCompat.getDrawable(resources, R.drawable.color_picker_swatch, null)!!)
        binding.colorPreview.setImageDrawable(ColorStateDrawable(colorDrawable, tag.color))
    }

    companion object {
        private fun newInstance(tag: Tag?, theme: Theme) = EditTagDialog().apply {
            arguments = Bundle().apply {
                if (tag != null) {
                    putParcelable("tag", tag)
                }
            }
            setStyle(STYLE_NO_TITLE, theme.themeDialog)
        }

        fun show(fragmentManager: FragmentManager, tag: Tag?, theme: Theme) {
            val dialog = newInstance(tag, theme)
            dialog.show(fragmentManager, "edit-tag-dialog")
        }
    }
}
