package info.anodsplace.playstore

import android.content.Context
import com.android.volley.RequestQueue
import android.accounts.Account

import finsky.api.model.DfeBulkDetails
import finsky.api.model.DfeModel
import finsky.api.model.Document

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class BulkDetailsEndpoint(context: Context, requestQueue: RequestQueue, deviceInfoProvider: DeviceInfoProvider, account: Account, private var docIds: List<String>)
    : PlayStoreEndpointBase(context, requestQueue, deviceInfoProvider, account) {

    var bulkData: DfeBulkDetails?
        get() = data as? DfeBulkDetails
        set(value) {
            data = value
        }

    val documents: List<Document>
        get() = bulkData?.documents ?: emptyList()

    override fun executeAsync() {
        bulkData?.docIds = docIds
        bulkData?.startAsync()
    }

    override fun executeSync() {
        bulkData?.docIds = docIds
        bulkData?.startSync()
    }

    override fun createDfeModel(): DfeModel {
        return DfeBulkDetails(dfeApi, AppDetailsFilter.predicate)
    }
}
