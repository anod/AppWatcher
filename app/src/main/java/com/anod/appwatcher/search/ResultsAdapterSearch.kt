package com.anod.appwatcher.search

import android.content.Context
import finsky.api.model.Document

/**
 * @author Alex Gavrishev
 * *
 * @date 26/08/2016.
 */

class ResultsAdapterSearch(context: Context, viewModel: SearchViewModel)
    : ResultsAdapter(context, viewModel) {

    private val endpoint = viewModel.endpointSearch

    override fun document(position: Int): Document {
        val isLastPosition = endpoint.count - 1 == position
        return endpoint.data!!.getItem(position, isLastPosition)!!
    }

    override fun getItemCount(): Int {
        return endpoint.count
    }
}
