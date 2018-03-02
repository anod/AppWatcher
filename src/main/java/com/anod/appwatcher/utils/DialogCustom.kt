package com.anod.appwatcher.utils

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import com.anod.appwatcher.R

/**
 * @author algavris
 * @date 02-Mar-18
 */
class DialogCustom(val context: Context,
                   @StringRes private val titleRes: Int,
                   @LayoutRes private val layoutRes: Int,
                   private val config: (view: View, builder: AlertDialog.Builder) -> Unit) {

    fun show() {
        val builder = AlertDialog.Builder(context, R.style.AlertDialog)
        val view: View = LayoutInflater.from(context).inflate(this.layoutRes, null)

        builder.setTitle(this.titleRes)
                .setView(view)

        this.config(view, builder)

        builder.create().show();
    }

}