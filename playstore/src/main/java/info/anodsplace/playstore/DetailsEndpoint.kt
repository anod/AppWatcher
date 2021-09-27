package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import finsky.api.model.DfeDetails
import finsky.api.model.Document
import finsky.protos.AppDetails
import okhttp3.OkHttpClient

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class DetailsEndpoint(context: Context, http: OkHttpClient, deviceInfoProvider: DeviceInfoProvider, account: Account, private val detailsUrl: String)
    : PlayStoreEndpointBase<DfeDetails>(context, http, deviceInfoProvider, account) {

    val appDetails: AppDetails?
        get() = data?.document?.appDetails

    val document: Document?
        get() = data?.document

    override fun beforeRequest(data: DfeDetails) {
        data.detailsUrl = detailsUrl
    }

    override fun createDfeModel(): DfeDetails = DfeDetails(dfeApi)
}
