package info.anodsplace.playstore

import android.accounts.Account
import android.content.Context
import com.android.volley.RequestQueue
import finsky.api.BulkDocId
import finsky.api.model.DfeBulkDetails
import finsky.api.model.DfeModel
import finsky.api.model.DfeSync
import finsky.api.model.Document

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class BulkDetailsEndpoint(context: Context, requestQueue: RequestQueue, deviceInfoProvider: DeviceInfoProvider, account: Account, private var docIds: List<BulkDocId>)
    : PlayStoreEndpointBase(context, requestQueue, deviceInfoProvider, account) {

    private var bulkData: DfeBulkDetails?
        get() = data as? DfeBulkDetails
        set(value) {
            data = value
        }

    val documents: List<Document>
        get() = bulkData?.documents ?: emptyList()

    override fun executeAsync() {
        bulkData?.docIds = docIds
        bulkData?.execute()
    }

    override fun executeSync() {
        val data = bulkData ?: return
        data.docIds = docIds
        DfeSync(data).execute()
    }

    override fun createDfeModel(): DfeModel {
        return DfeBulkDetails(dfeApi, AppDetailsFilter.predicate)
    }
}
