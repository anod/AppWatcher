package com.anod.appwatcher.search

import android.content.Context

import finsky.api.model.Document

/**
 * @author Alex Gavrishev
 * *
 * @date 26/08/2016.
 */

class ResultsAdapterDetails(context: Context, viewModel: SearchViewModel)
    : ResultsAdapter(context, viewModel) {

    private val endpoint = viewModel.endpointDetails

    override fun document(position: Int): Document {
        return endpoint.document!!
    }

    override fun getItemCount(): Int {
        return if (endpoint.document != null) 1 else 0
    }
}
