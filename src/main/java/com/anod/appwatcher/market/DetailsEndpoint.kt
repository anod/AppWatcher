package com.anod.appwatcher.market

import android.content.Context

import com.google.android.finsky.api.model.DfeDetails
import com.google.android.finsky.api.model.DfeModel
import com.google.android.finsky.api.model.Document
import com.google.android.finsky.protos.nano.Messages.AppDetails

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class DetailsEndpoint(context: Context) : PlayStoreEndpointBase(context) {
    var url: String = ""

    var detailsData: DfeDetails?
        get() = data as? DfeDetails
        set(value) {
            super.data = value
        }

    val appDetails: AppDetails?
        get() = detailsData?.document?.appDetails

    val document: Document?
        get() = detailsData?.document

    val recentChanges: String
        get() = appDetails?.recentChangesHtml ?: ""

    override fun executeAsync() {
        detailsData?.detailsUrl = url
        detailsData?.startAsync()
    }

    override fun executeSync() {
        detailsData?.detailsUrl = url
        detailsData?.startSync()
    }

    override fun createDfeModel(): DfeModel {
        return DfeDetails(dfeApi)
    }

}
