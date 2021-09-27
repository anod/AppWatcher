package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import finsky.api.BulkDocId
import finsky.api.model.DfeBulkDetails
import finsky.api.model.Document
import okhttp3.OkHttpClient

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class BulkDetailsEndpoint(context: Context, http: OkHttpClient, deviceInfoProvider: DeviceInfoProvider, account: Account, private var docIds: List<BulkDocId>)
    : PlayStoreEndpointBase<DfeBulkDetails>(context, http, deviceInfoProvider, account) {

    val documents: List<Document>
        get() = data?.documents ?: emptyList()

    override fun beforeRequest(data: DfeBulkDetails) {
        data.docIds = docIds
    }

    override fun createDfeModel() = DfeBulkDetails(dfeApi, AppDetailsFilter.predicate)
}
