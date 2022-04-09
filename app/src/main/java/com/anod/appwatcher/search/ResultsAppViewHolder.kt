package com.anod.appwatcher.search

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.model.AppInfo
import com.anod.appwatcher.utils.EventFlow
import com.anod.appwatcher.utils.AppIconLoader
import finsky.api.model.Document
import info.anodsplace.framework.app.DialogMessage
import info.anodsplace.framework.content.InstalledApps
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class ResultsAppViewHolder(
        itemView: View,
        private val iconLoader: AppIconLoader,
        private val action: EventFlow<ResultAction>,
        private val packages: StateFlow<List<String>>,
        private val colorBgDisabled: Int,
        private val colorBgNormal: Int,
        private val installedApps: InstalledApps.MemoryCache)
    : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    var doc: Document? = null

    private val row: View = itemView.findViewById(R.id.content)
    private val updated: TextView = itemView.findViewById(R.id.updated)
    val title: TextView = itemView.findViewById(R.id.title)
    val creator: TextView = itemView.findViewById(R.id.creator)
    val price: TextView = itemView.findViewById(R.id.price)
    val icon: ImageView = itemView.findViewById(R.id.icon)

    private val installedText = itemView.resources.getString(R.string.installed).uppercase(Locale.getDefault())

    init {
        this.row.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (doc == null) {
            return
        }
        val info = AppInfo(doc!!)

        val packages = packages.value
        if (packages.contains(info.packageName)) {
            DialogMessage(itemView.context, R.style.AlertDialog, R.string.already_exist, R.string.delete_existing_item) { builder ->
                builder.setPositiveButton(R.string.delete) { _, _ ->
                    action.tryEmit(Delete(info))
                }
                builder.setNegativeButton(android.R.string.cancel) { _, _ ->

                }
            }.show()
        } else {
            action.tryEmit(Add(info))
        }
    }

    fun placeholder() {
        title.text = ""
        creator.text = ""
        updated.text = ""
        price.text = ""
        row.setBackgroundColor(colorBgDisabled)
        this.doc = null
        icon.setImageResource(R.drawable.ic_app_icon_placeholder)
    }

    fun bind(doc: Document) {
        val app = doc.appDetails
        val uploadDate = app.uploadDate
        val packageName = app.packageName

        this.doc = doc
        title.text = doc.title
        creator.text = doc.creator
        updated.text = uploadDate

        val packages = packages.value
        if (packages.contains(packageName)) {
            row.setBackgroundColor(colorBgDisabled)
        } else {
            row.setBackgroundColor(colorBgNormal)
        }

        iconLoader.retrieve(doc.iconUrl ?: "") {
            it.placeholder(R.drawable.ic_app_icon_placeholder)
            it.target(icon)
        }

        val isInstalled = installedApps.packageInfo(packageName).isInstalled
        if (isInstalled) {
            price.text = installedText
        } else {
            val offer = doc.offer
            when {
                offer.offerType == 0 -> price.text = ""
                offer.micros.toInt() == 0 -> price.setText(R.string.free)
                else -> price.text = offer.formattedAmount
            }
        }
    }
}