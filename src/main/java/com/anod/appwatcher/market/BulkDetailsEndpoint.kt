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
    var docIds: List<String> = listOf()

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
