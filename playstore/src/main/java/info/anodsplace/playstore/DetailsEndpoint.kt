package info.anodsplace.playstore

import android.content.Context
import com.android.volley.RequestQueue

import finsky.api.model.DfeDetails
import finsky.api.model.DfeModel
import finsky.api.model.Document
import finsky.protos.nano.Messages.AppDetails

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class DetailsEndpoint(context: Context, requestQueue: RequestQueue, deviceInfoProvider: DeviceInfoProvider)
    : PlayStoreEndpointBase(context, requestQueue, deviceInfoProvider) {
    var url: String = ""

    var detailsData: DfeDetails?
        get() = data as? DfeDetails
        set(value) {
            super.data = value
        }

    private val appDetails: AppDetails?
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
        return DfeDetails(dfeApi!!)
    }

}
