package com.anod.appwatcher.market

import android.content.Context

import com.google.android.finsky.api.model.DfeBulkDetails
import com.google.android.finsky.api.model.DfeModel
import com.google.android.finsky.api.model.Document

/**
 * @author alex
 * *
 * @date 2015-02-22
 */
class BulkDetailsEndpoint(context: Context) : PlayStoreEndpointBase(context) {
    private var mDocIds: List<String>? = null

    var bulkData: DfeBulkDetails?
        get() = data as? DfeBulkDetails
        set(value) {
            data = value
        }

    override fun executeAsync() {
        bulkData?.docIds = mDocIds
        bulkData?.startAsync()
    }

    override fun executeSync() {
        bulkData?.docIds = mDocIds
        bulkData?.startSync()
    }

    fun setDocIds(docIds: List<String>) {
        mDocIds = docIds
    }

    val documents: List<Document>
        get() = bulkData?.documents ?: emptyList()

    override fun createDfeModel(): DfeModel {
        return DfeBulkDetails(dfeApi, AppDetailsFilter.predicate)
    }
}
