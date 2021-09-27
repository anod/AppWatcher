package finsky.api.model

import finsky.api.BulkDocId
import finsky.api.DfeApi
import finsky.protos.Details
import finsky.protos.ResponseWrapper

class DfeBulkDetails(private val api: DfeApi, private val filter: FilterPredicate) : DfeModel() {
    private var bulkDetailsResponse: Details.BulkDetailsResponse? = null
    var docIds: List<BulkDocId> = listOf()

    override suspend fun makeRequest(): ResponseWrapper {
        return api.details(docIds, true)
    }

    val documents: List<Document>
        get() {
            val response = this.bulkDetailsResponse ?: return emptyList()

            val list = mutableListOf<Document>()
            for (i in response.entryList.indices) {
                response.getEntry(i).doc?.let {
                    list.add(Document(it))
                }
            }

            return if (list.isEmpty()) {
                list
            } else {
                list.filter(filter)
            }
        }

    override val isReady: Boolean
        get() = this.bulkDetailsResponse != null

    override fun onResponse(responseWrapper: ResponseWrapper) {
        this.bulkDetailsResponse = responseWrapper.payload.bulkDetailsResponse
    }
}