package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import com.android.volley.RequestQueue
import finsky.api.BulkDocId
import finsky.api.model.DfeBulkDetails
import finsky.api.model.Document

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class BulkDetailsEndpoint(context: Context, requestQueue: RequestQueue, deviceInfoProvider: DeviceInfoProvider, account: Account, private var docIds: List<BulkDocId>)
    : PlayStoreEndpointBase<DfeBulkDetails>(context, requestQueue, deviceInfoProvider, account) {

    val documents: List<Document>
        get() = data?.documents ?: emptyList()

    override fun beforeRequest(data: DfeBulkDetails) {
        data.docIds = docIds
    }

    override fun createDfeModel() = DfeBulkDetails(dfeApi, AppDetailsFilter.predicate)
}
