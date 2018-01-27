package info.anodsplace.playstore

import android.accounts.Account
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
class DetailsEndpoint(context: Context, requestQueue: RequestQueue, deviceInfoProvider: DeviceInfoProvider, account: Account, private val detailsUrl: String)
    : PlayStoreEndpointBase(context, requestQueue, deviceInfoProvider, account) {

    var detailsData: DfeDetails?
        get() = data as? DfeDetails
        set(value) {
            super.data = value
        }

    val appDetails: AppDetails?
        get() = detailsData?.document?.appDetails

    val document: Document?
        get() = detailsData?.document

    override fun executeAsync() {
        detailsData?.detailsUrl = detailsUrl
        detailsData?.startAsync()
    }

    override fun executeSync() {
        detailsData?.detailsUrl = detailsUrl
        detailsData?.startSync()
    }

    override fun createDfeModel(): DfeModel {
        return DfeDetails(dfeApi)
    }

}
