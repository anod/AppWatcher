package com.anod.appwatcher.tags

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.DialogFragment
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.android.colorpicker.ColorPickerSwatch
import com.android.colorpicker.ColorStateDrawable
import com.anod.appwatcher.R
import com.anod.appwatcher.model.Tag
import info.anodsplace.colorpicker.ColorPickerDialog

/**
 * @author algavris
 * *
 * @date 14/04/2017.
 */

class EditTagDialog : DialogFragment(), ColorPickerSwatch.OnColorSelectedListener {

    @BindView(R.id.tag_name)
    lateinit var mEditText: TextInputEditText
    @BindView(R.id.color_preview)
    lateinit var mColor: ImageView
    @BindView(android.R.id.button3)
    lateinit var mDeleteButton: Button

    private lateinit var mTag: Tag


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.dialog_edit_tag, container, false)
        ButterKnife.bind(this, view)

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tag = arguments.getParcelable<Tag>("tag")
        mTag = tag ?: Tag("")

        mEditText.setText(mTag.name)
        val colorDrawable = arrayOf<Drawable>(ResourcesCompat.getDrawable(resources, R.drawable.color_picker_swatch, null)!!)
        mColor.setImageDrawable(ColorStateDrawable(colorDrawable, mTag.color))
        mEditText.requestFocus()
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        if (mTag.id == -1) {
            mDeleteButton.visibility = View.GONE
        } else {
            mDeleteButton.visibility = View.VISIBLE
        }
    }


    @OnClick(R.id.color_preview)
    fun onColorClick() {
        val dialog = ColorPickerDialog.newInstance(mTag.color, false, activity)
        dialog.setOnColorSelectedListener(this)
        dialog.show(fragmentManager, "color-picker")
    }

    @OnClick(android.R.id.button1)
    fun onSaveClick() {
        mTag = Tag(mTag.id, mEditText.text.toString().trim { it <= ' ' }, mTag.color)
        (activity as TagsListActivity).saveTag(mTag)
        dismiss()
    }

    @OnClick(android.R.id.button2)
    fun onCancelClick() {
        dismiss()
    }

    @OnClick(android.R.id.button3)
    fun onDeleteClick() {
        (activity as TagsListActivity).deleteTag(mTag)
        dismiss()
    }

    override fun onColorSelected(color: Int) {
        mTag = Tag(mTag.id, mTag.name, color)

        val colorDrawable = arrayOf<Drawable>(ResourcesCompat.getDrawable(resources, R.drawable.color_picker_swatch, null)!!)
        mColor.setImageDrawable(ColorStateDrawable(colorDrawable, mTag.color))
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
