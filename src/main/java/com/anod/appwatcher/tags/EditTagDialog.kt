package com.anod.appwatcher.tags

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.core.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.ViewModelProviders
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.Tag
import com.anod.appwatcher.utils.Theme
import info.anodsplace.colorpicker.ColorPickerDialog
import info.anodsplace.colorpicker.ColorPickerSwatch
import info.anodsplace.colorpicker.ColorStateDrawable
import kotlinx.android.synthetic.main.dialog_edit_tag.*

/**
 * @author Alex Gavrishev
 * *
 * @date 14/04/2017.
 */

class EditTagDialog : DialogFragment(), ColorPickerSwatch.OnColorSelectedListener {

    private lateinit var tag: Tag

    private val viewModel: TagsListViewModel by lazy { ViewModelProviders.of(this).get(TagsListViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_edit_tag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tag = arguments!!.getParcelable("tag") ?: Tag("")

        tagName.setText(tag.name)
        val colorDrawable = arrayOf(ResourcesCompat.getDrawable(resources, R.drawable.color_picker_swatch, null)!!)
        colorPreview.setImageDrawable(ColorStateDrawable(colorDrawable, tag.color))
        tagName.requestFocus()
        dialog?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        if (tag.id == -1) {
            button3.visibility = View.GONE
        } else {
            button3.visibility = View.VISIBLE
        }

        colorPreview.setOnClickListener {
            val dialog = ColorPickerDialog.newInstance(tag.color, false, activity)
            dialog.setStyle(STYLE_NORMAL, Theme(activity!!).themeDialogNoActionBar)
            dialog.setOnColorSelectedListener(this)
            dialog.show(fragmentManager!!, "color-picker")
        }


        button1.setOnClickListener {
            tag = Tag(tag.id, tagName.text.toString().trim { it <= ' ' }, tag.color)
            viewModel.saveTag(tag)
            dismiss()
        }

        button2.setOnClickListener {
            dismiss()
        }

        button3.setOnClickListener {
            viewModel.deleteTag(tag)
            dismiss()
        }
    }

    override fun onColorSelected(color: Int) {
        tag = Tag(tag.id, tag.name, color)

        val colorDrawable = arrayOf(ResourcesCompat.getDrawable(resources, R.drawable.color_picker_swatch, null)!!)
        colorPreview.setImageDrawable(ColorStateDrawable(colorDrawable, tag.color))
    }

    companion object {
        fun newInstance(tag: Tag?, theme: Theme) = EditTagDialog().apply {
            arguments = Bundle().apply {
                if (tag != null) {
                    putParcelable("tag", tag)
                }
            }
            setStyle(STYLE_NO_TITLE, theme.themeDialog)
        }
    }
}
