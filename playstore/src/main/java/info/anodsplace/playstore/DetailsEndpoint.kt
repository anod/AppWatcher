package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import com.android.volley.RequestQueue
import finsky.api.model.DfeDetails
import finsky.api.model.Document
import finsky.protos.nano.Messages.AppDetails

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class DetailsEndpoint(context: Context, requestQueue: RequestQueue, deviceInfoProvider: DeviceInfoProvider, account: Account, private val detailsUrl: String)
    : PlayStoreEndpointBase<DfeDetails>(context, requestQueue, deviceInfoProvider, account) {

    val appDetails: AppDetails?
        get() = data?.document?.appDetails

    val document: Document?
        get() = data?.document

    override fun beforeRequest(data: DfeDetails) {
        data.detailsUrl = detailsUrl
    }

    override fun createDfeModel(): DfeDetails = DfeDetails(dfeApi)
}
