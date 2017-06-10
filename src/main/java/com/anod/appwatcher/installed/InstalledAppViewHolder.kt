package com.anod.appwatcher.installed

import android.view.View

import com.anod.appwatcher.R
import com.anod.appwatcher.adapters.AppViewHolder
import com.anod.appwatcher.adapters.AppViewHolderBase
import com.anod.appwatcher.utils.AppIconLoader

/**
 * @author alex
 * *
 * @date 2015-08-31
 */
internal class InstalledAppViewHolder(
        itemView: View,
        dataProvider: AppViewHolderBase.DataProvider,
        iconLoader: AppIconLoader,
        listener: AppViewHolder.OnClickListener?)
    : AppViewHolder(itemView, dataProvider, iconLoader, listener) {

    override val isLocalApp: Boolean
        get() = true

    override fun bindSectionView() {
        if (location == 0) {
            sectionText.setText(R.string.downloaded)
            sectionCount.text = dataProvider.totalAppsCount.toString()
            section.visibility = View.VISIBLE
            actionButton.visibility = View.GONE
        } else {
            section.visibility = View.GONE
        }
    }
}
