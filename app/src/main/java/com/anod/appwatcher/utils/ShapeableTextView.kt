package com.anod.appwatcher.utils

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build.VERSION
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.TintTypedArray
import com.google.android.material.R
import com.google.android.material.resources.MaterialResources
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.Shapeable
import com.google.android.material.textview.MaterialTextView

class ShapeableTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : MaterialTextView(context, attrs, defStyleAttr), Shapeable {
    private var _shapeAppearanceModel: ShapeAppearanceModel
    private val DEF_STYLE_RES: Int = R.style.Widget_MaterialComponents_TextView

    init {
        _shapeAppearanceModel = ShapeAppearanceModel.builder(context, attrs, defStyleAttr, DEF_STYLE_RES).build()
        val a = TintTypedArray.obtainStyledAttributes(context, attrs, androidx.appcompat.R.styleable.AppCompatTheme, defStyleAttr, 0)
        val initialColorInt = MaterialResources.getColorStateList(context, a, R.styleable.AppCompatTheme_colorAccent)
        a.recycle()
        background = MaterialShapeDrawable(shapeAppearanceModel).also {
            it.fillColor = initialColorInt
        }
    }

    fun setFillColor(fillColor: ColorStateList) {
        (background as? MaterialShapeDrawable)?.fillColor = fillColor
    }

    fun setFillColor(@ColorInt fillColor: Int) {
        setFillColor(ColorStateList.valueOf(fillColor))
    }

    override fun setShapeAppearanceModel(shapeAppearanceModel: ShapeAppearanceModel) {
        _shapeAppearanceModel = shapeAppearanceModel
        invalidate()
        if (VERSION.SDK_INT >= 21) {
            invalidateOutline()
        }
    }

    override fun getShapeAppearanceModel(): ShapeAppearanceModel = _shapeAppearanceModel
}