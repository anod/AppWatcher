package com.anod.appwatcher.tags

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.android.colorpicker.ColorPickerSwatch
import com.android.colorpicker.ColorStateDrawable
import com.anod.appwatcher.R
import com.anod.appwatcher.model.Tag
import info.anodsplace.colorpicker.ColorPickerDialog
import kotlinx.android.synthetic.main.dialog_edit_tag.*

/**
 * @author algavris
 * *
 * @date 14/04/2017.
 */

class EditTagDialog : DialogFragment(), ColorPickerSwatch.OnColorSelectedListener {

    private lateinit var tag: Tag

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_edit_tag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tag = arguments!!.getParcelable("tag") ?: Tag("")

        tagName.setText(tag.name)
        val colorDrawable = arrayOf<Drawable>(ResourcesCompat.getDrawable(resources, R.drawable.color_picker_swatch, null)!!)
        colorPreview.setImageDrawable(ColorStateDrawable(colorDrawable, tag.color))
        tagName.requestFocus()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        if (tag.id == -1) {
            button3.visibility = View.GONE
        } else {
            button3.visibility = View.VISIBLE
        }

        colorPreview.setOnClickListener {
            val dialog = ColorPickerDialog.newInstance(tag.color, false, activity)
            dialog.setOnColorSelectedListener(this)
            dialog.show(fragmentManager, "color-picker")
        }

        button1.setOnClickListener {
            tag = Tag(tag.id, tagName.text.toString().trim { it <= ' ' }, tag.color)
            (activity as TagsListActivity).saveTag(tag)
            dismiss()
        }

        button2.setOnClickListener {
            dismiss()
        }

        button3.setOnClickListener {
            (activity as TagsListActivity).deleteTag(tag)
            dismiss()
        }
    }

    override fun onColorSelected(color: Int) {
        tag = Tag(tag.id, tag.name, color)

        val colorDrawable = arrayOf<Drawable>(ResourcesCompat.getDrawable(resources, R.drawable.color_picker_swatch, null)!!)
        colorPreview.setImageDrawable(ColorStateDrawable(colorDrawable, tag.color))
    }

    companion object {
        fun newInstance(tag: Tag?): EditTagDialog {
            val frag = EditTagDialog()
            val args = Bundle()
            if (tag != null) {
                args.putParcelable("tag", tag)
            }
            frag.arguments = args
            frag.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme_Dialog)
            return frag
        }
    }
}
