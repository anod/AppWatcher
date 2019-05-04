package com.anod.appwatcher.search

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.anod.appwatcher.R
import com.anod.appwatcher.model.AppInfo
import info.anodsplace.framework.app.DialogMessage
import finsky.api.model.Document

class ResultsAppViewHolder(itemView: View, private val viewModel: ResultsViewModel)
    : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    var doc: Document? = null

    val row: View = itemView.findViewById(android.R.id.content)
    val title: TextView = itemView.findViewById(android.R.id.title)
    val creator: TextView = itemView.findViewById(R.id.creator)
    val updated: TextView = itemView.findViewById(R.id.updated)
    val price: TextView = itemView.findViewById(R.id.price)
    val icon: ImageView = itemView.findViewById(android.R.id.icon)

    init {
        this.row.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val info = AppInfo(doc!!)

        val pacakges = viewModel.packages.value ?: emptyList()
        if (pacakges.contains(info.packageName)) {
            DialogMessage(itemView.context, R.style.AlertDialog, R.string.already_exist, R.string.delete_existing_item) { builder ->
                builder.setPositiveButton(R.string.delete) { _, _ ->
                    viewModel.delete(info)
                }
                builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                }
            }.show()
        } else {
            viewModel.add(info)
        }
    }
}
